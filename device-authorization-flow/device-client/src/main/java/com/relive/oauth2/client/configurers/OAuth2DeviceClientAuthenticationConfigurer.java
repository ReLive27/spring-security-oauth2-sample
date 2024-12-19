package com.relive.oauth2.client.configurers;

import com.relive.oauth2.client.OAuth2DeviceAuthenticationFilter;
import com.relive.oauth2.client.OAuth2DeviceAuthorizationRequestFilter;
import com.relive.oauth2.client.authentication.OAuth2DeviceAuthenticationProvider;
import com.relive.oauth2.client.authentication.OAuth2DeviceAuthorizationRequestProvider;
import com.relive.oauth2.client.authentication.OAuth2DeviceCodeAuthenticationProvider;
import com.relive.oauth2.client.endpoint.DefaultDeviceCodeTokenResponseClient;
import com.relive.oauth2.client.endpoint.OAuth2DeviceCodeGrantRequest;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 配置器，用于配置 OAuth2 设备客户端认证流程的 HTTP 安全设置。
 * <p>
 * 本配置器为 OAuth2 设备代码认证提供认证处理器、过滤器、客户端服务等相关组件的配置。
 * </p>
 *
 * @author: ReLive27
 * @date: 2024/5/2 14:27
 */
public class OAuth2DeviceClientAuthenticationConfigurer extends AbstractHttpConfigurer<OAuth2DeviceClientAuthenticationConfigurer, HttpSecurity> {

    /**
     * 初始化配置，注册 OAuth2 设备授权请求和设备代码认证处理器。
     *
     * @param http {@link HttpSecurity} 对象
     * @throws Exception 配置过程中的异常
     */
    @Override
    public void init(HttpSecurity http) throws Exception {
        // 注册设备授权请求处理器
        OAuth2DeviceAuthorizationRequestProvider deviceAuthorizationRequestProvider = new OAuth2DeviceAuthorizationRequestProvider();
        http.authenticationProvider(this.postProcess(deviceAuthorizationRequestProvider));

        // 注册设备代码认证处理器
        OAuth2AccessTokenResponseClient<OAuth2DeviceCodeGrantRequest> accessTokenResponseClient = new DefaultDeviceCodeTokenResponseClient();
        OAuth2DeviceCodeAuthenticationProvider deviceCodeAuthenticationProvider = new OAuth2DeviceCodeAuthenticationProvider(accessTokenResponseClient);
        http.authenticationProvider(this.postProcess(deviceCodeAuthenticationProvider));

        // 注册设备认证处理器
        OAuth2UserService<OAuth2UserRequest, OAuth2User> userService = new DefaultOAuth2UserService();
        OAuth2DeviceAuthenticationProvider deviceAuthenticationProvider = new OAuth2DeviceAuthenticationProvider(accessTokenResponseClient, userService);
        http.authenticationProvider(this.postProcess(deviceAuthenticationProvider));

        super.init(http);
    }

    /**
     * 配置 HTTP 安全设置，添加设备认证过滤器和授权客户端服务。
     *
     * @param http {@link HttpSecurity} 对象
     * @throws Exception 配置过程中的异常
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);

        // 获取客户端注册仓库
        ClientRegistrationRepository clientRegistrationRepository = getClientRegistrationRepository(http);
        OAuth2DeviceAuthorizationRequestFilter deviceAuthorizationRequestFilter = new OAuth2DeviceAuthorizationRequestFilter(authenticationManager, clientRegistrationRepository);

        // 在用户名密码认证过滤器之前添加设备授权请求过滤器
        http.addFilterBefore(this.postProcess(deviceAuthorizationRequestFilter), UsernamePasswordAuthenticationFilter.class);

        // 获取授权客户端服务
        OAuth2AuthorizedClientService oAuth2AuthorizedClientService = getOAuth2AuthorizedClientService(http);
        OAuth2DeviceAuthenticationFilter deviceAuthenticationFilter = new OAuth2DeviceAuthenticationFilter(clientRegistrationRepository, oAuth2AuthorizedClientService);
        deviceAuthenticationFilter.setAuthenticationManager(authenticationManager);

        // 设置安全上下文仓库为会话存储
        deviceAuthenticationFilter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());

        // 设置认证成功处理器
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler("/success");
        deviceAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);

        // 在用户名密码认证过滤器之前添加设备认证过滤器
        http.addFilterBefore(this.postProcess(deviceAuthenticationFilter), UsernamePasswordAuthenticationFilter.class);
        super.configure(http);
    }

    /**
     * 获取客户端注册仓库。
     *
     * @param httpSecurity {@link HttpSecurity} 对象
     * @return {@link ClientRegistrationRepository} 客户端注册仓库
     */
    static ClientRegistrationRepository getClientRegistrationRepository(HttpSecurity httpSecurity) {
        return getOptionalBean(httpSecurity, ClientRegistrationRepository.class);
    }

    /**
     * 获取授权客户端服务。
     *
     * @param httpSecurity {@link HttpSecurity} 对象
     * @return {@link OAuth2AuthorizedClientService} 授权客户端服务
     */
    static OAuth2AuthorizedClientService getOAuth2AuthorizedClientService(HttpSecurity httpSecurity) {
        return getOptionalBean(httpSecurity, OAuth2AuthorizedClientService.class);
    }

    /**
     * 从 Spring 上下文中获取一个可选的 Bean。如果有多个匹配的 Bean，则抛出异常。
     *
     * @param httpSecurity {@link HttpSecurity} 对象
     * @param type         {@link Class} 类型
     * @param <T>          返回类型
     * @return 返回匹配类型的 Bean 或 {@code null} 如果没有找到
     * @throws NoUniqueBeanDefinitionException 如果找到多个匹配的 Bean，则抛出此异常
     */
    public static <T> T getOptionalBean(HttpSecurity httpSecurity, Class<T> type) {
        Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                httpSecurity.getSharedObject(ApplicationContext.class), type);
        if (beansMap.size() > 1) {
            throw new NoUniqueBeanDefinitionException(type, beansMap.size(),
                    "Expected single matching bean of type '" + type.getName() + "' but found " +
                            beansMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(beansMap.keySet()));
        }
        return (!beansMap.isEmpty() ? beansMap.values().iterator().next() : null);
    }
}
