package com.relive.config;

import com.relive.authentication.DeviceClientAuthenticationConverter;
import com.relive.authentication.DeviceClientAuthenticationProvider;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 配置自定义设备客户端认证的 HttpSecurity 配置器。
 * <p>
 * 该配置器允许开发者为 OAuth2 授权服务器添加设备客户端认证的转换器和提供者，
 * 以实现自定义的设备授权流程。
 *
 * <p>可通过设置 {@link AuthenticationConverter} 和 {@link AuthenticationProvider}
 * 来控制认证请求的解析和处理行为。
 *
 * @author: ReLive27
 * @date: 2024/1/25 16:01
 */
public class DeviceClientAuthenticationConfigurer
        extends AbstractHttpConfigurer<DeviceClientAuthenticationConfigurer, HttpSecurity> {

    private AuthenticationConverter deviceClientAuthenticationConverter;

    private AuthenticationProvider deviceClientAuthenticationProvider;

    /**
     * 设置设备客户端认证的 {@link AuthenticationConverter}。
     * <p>
     * 该转换器负责将 HTTP 请求转换为设备客户端认证的 {@link Authentication} 对象。
     *
     * @param deviceClientAuthenticationConverter 自定义的 {@link AuthenticationConverter} 实例
     * @return 当前 {@link DeviceClientAuthenticationConfigurer} 实例
     * @throws IllegalArgumentException 如果 {@code deviceClientAuthenticationConverter} 为空
     */
    public DeviceClientAuthenticationConfigurer deviceClientAuthenticationConverter(AuthenticationConverter deviceClientAuthenticationConverter) {
        Assert.notNull(deviceClientAuthenticationConverter, "deviceClientAuthenticationConverter can not be null");
        this.deviceClientAuthenticationConverter = deviceClientAuthenticationConverter;
        return this;
    }

    /**
     * 设置设备客户端认证的 {@link AuthenticationProvider}。
     * <p>
     * 该提供者负责处理设备客户端认证请求，并验证认证信息的有效性。
     *
     * @param deviceClientAuthenticationProvider 自定义的 {@link AuthenticationProvider} 实例
     * @return 当前 {@link DeviceClientAuthenticationConfigurer} 实例，用于链式调用
     * @throws IllegalArgumentException 如果 {@code deviceClientAuthenticationProvider} 为空
     */
    public DeviceClientAuthenticationConfigurer deviceClientAuthenticationProvider(AuthenticationProvider deviceClientAuthenticationProvider) {
        Assert.notNull(deviceClientAuthenticationProvider, "deviceClientAuthenticationProvider can not be null");
        this.deviceClientAuthenticationProvider = deviceClientAuthenticationProvider;
        return this;
    }

    /**
     * 配置设备客户端认证所需的转换器和提供者。
     * <p>
     * 如果未显式设置 {@link AuthenticationConverter} 和 {@link AuthenticationProvider}，
     * 则使用默认实现。
     *
     * @param http {@link HttpSecurity} 实例，用于添加自定义的设备客户端认证配置
     * @throws Exception 如果配置过程中出现错误
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        AuthorizationServerSettings authorizationServerSettings = getAuthorizationServerSettings(http);

        // 如果未提供自定义 AuthenticationConverter，则使用默认实现
        if (this.deviceClientAuthenticationConverter == null) {
            this.deviceClientAuthenticationConverter = new DeviceClientAuthenticationConverter(
                    authorizationServerSettings.getDeviceAuthorizationEndpoint());
        }

        // 如果未提供自定义 AuthenticationProvider，则使用默认实现
        if (this.deviceClientAuthenticationProvider == null) {
            RegisteredClientRepository registeredClientRepository = getRegisteredClientRepository(http);
            this.deviceClientAuthenticationProvider = new DeviceClientAuthenticationProvider(registeredClientRepository);
        }

        // 将自定义的转换器和提供者添加到 OAuth2AuthorizationServerConfigurer
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .clientAuthentication(clientAuthentication ->
                        clientAuthentication
                                .authenticationConverter(deviceClientAuthenticationConverter)
                                .authenticationProvider(deviceClientAuthenticationProvider)
                );
    }

    /**
     * 获取 {@link AuthorizationServerSettings} 配置。
     * 如果不存在共享对象或 Bean，则返回默认设置。
     *
     * @param httpSecurity {@link HttpSecurity} 实例
     * @return {@link AuthorizationServerSettings} 实例
     */
    private static AuthorizationServerSettings getAuthorizationServerSettings(HttpSecurity httpSecurity) {
        AuthorizationServerSettings authorizationServerSettings = httpSecurity.getSharedObject(AuthorizationServerSettings.class);
        if (authorizationServerSettings == null) {
            authorizationServerSettings = getOptionalBean(httpSecurity, AuthorizationServerSettings.class);
            if (authorizationServerSettings == null) {
                authorizationServerSettings = AuthorizationServerSettings.builder().build();
            }
        }
        return authorizationServerSettings;
    }

    /**
     * 获取 {@link RegisteredClientRepository} 实例。
     * 如果不存在共享对象或 Bean，则返回默认的内存实现。
     *
     * @param httpSecurity {@link HttpSecurity} 实例
     * @return {@link RegisteredClientRepository} 实例
     */
    private static RegisteredClientRepository getRegisteredClientRepository(HttpSecurity httpSecurity) {
        RegisteredClientRepository registeredClientRepository = httpSecurity.getSharedObject(RegisteredClientRepository.class);
        if (registeredClientRepository == null) {
            registeredClientRepository = getOptionalBean(httpSecurity, RegisteredClientRepository.class);
            if (registeredClientRepository == null) {
                registeredClientRepository = new InMemoryRegisteredClientRepository();
            }
        }
        return registeredClientRepository;
    }

    /**
     * 获取指定类型的 Bean，如果不存在则返回 null。
     *
     * @param httpSecurity {@link HttpSecurity} 实例
     * @param type         Bean 的类型
     * @param <T>          Bean 的泛型类型
     * @return 匹配的 Bean 实例或 null
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
