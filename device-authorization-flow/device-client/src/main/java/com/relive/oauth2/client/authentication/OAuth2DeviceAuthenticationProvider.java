package com.relive.oauth2.client.authentication;

import com.relive.oauth2.client.endpoint.OAuth2DeviceCodeGrantRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;


/**
 * 自定义 OAuth 2.0 设备认证提供者，用于处理设备认证请求。
 * <p>
 * 该类负责与设备授权端点交互，验证设备授权码并加载用户信息。
 * 它整合了 {@link OAuth2DeviceCodeAuthenticationProvider} 和 {@link OAuth2UserService}，
 * 以完成设备授权码的交换和用户详情的加载。
 *
 * @author: ReLive27
 * @date: 2024/5/2 10:48
 */
public class OAuth2DeviceAuthenticationProvider implements AuthenticationProvider {

    private final OAuth2DeviceCodeAuthenticationProvider deviceCodeAuthenticationProvider;

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> userService;

    private GrantedAuthoritiesMapper authoritiesMapper = ((authorities) -> authorities);

    /**
     * 构造方法，初始化设备认证提供者。
     *
     * @param accessTokenResponseClient 用于获取访问令牌的客户端
     * @param userService               用于加载用户信息的服务
     * @throws IllegalArgumentException 如果 {@code userService} 为 null
     */
    public OAuth2DeviceAuthenticationProvider(
            OAuth2AccessTokenResponseClient<OAuth2DeviceCodeGrantRequest> accessTokenResponseClient,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> userService) {
        Assert.notNull(userService, "userService cannot be null");
        this.deviceCodeAuthenticationProvider = new OAuth2DeviceCodeAuthenticationProvider(
                accessTokenResponseClient);
        this.userService = userService;
    }

    /**
     * 处理设备认证逻辑。
     * <p>
     * 该方法执行以下操作：
     * - 通过设备授权码获取访问令牌。
     * - 使用访问令牌加载用户信息。
     * - 映射用户的授权信息。
     * - 返回认证结果。
     *
     * @param authentication 包含设备授权请求的认证对象
     * @return 完成认证的 {@link OAuth2DeviceAuthenticationToken}
     * @throws OAuth2AuthenticationException 如果设备认证失败
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2DeviceAuthenticationToken deviceAuthenticationToken = (OAuth2DeviceAuthenticationToken) authentication;

        OAuth2DeviceCodeAuthenticationToken deviceCodeAuthenticationToken;
        try {
            // 通过设备授权码验证获取访问令牌
            deviceCodeAuthenticationToken = (OAuth2DeviceCodeAuthenticationToken) this.deviceCodeAuthenticationProvider.authenticate(
                    new OAuth2DeviceCodeAuthenticationToken(deviceAuthenticationToken.getClientRegistration(),
                            deviceAuthenticationToken.getDeviceAuthorizationRequest()));
        } catch (OAuth2AuthorizationException ex) {
            OAuth2Error oauth2Error = ex.getError();
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
        }

        // 使用访问令牌加载用户信息
        OAuth2AccessToken accessToken = deviceCodeAuthenticationToken.getAccessToken();
        Map<String, Object> additionalParameters = deviceCodeAuthenticationToken.getAdditionalParameters();
        OAuth2User oauth2User = this.userService.loadUser(new OAuth2UserRequest(
                deviceAuthenticationToken.getClientRegistration(), accessToken, additionalParameters));

        // 映射用户权限
        Collection<? extends GrantedAuthority> mappedAuthorities = this.authoritiesMapper
                .mapAuthorities(oauth2User.getAuthorities());

        // 构建认证结果对象
        OAuth2DeviceAuthenticationToken authenticationResult = new OAuth2DeviceAuthenticationToken(
                deviceAuthenticationToken.getClientRegistration(), deviceAuthenticationToken.getDeviceAuthorizationRequest(),
                oauth2User, mappedAuthorities, accessToken, deviceCodeAuthenticationToken.getRefreshToken());
        authenticationResult.setDetails(deviceAuthenticationToken.getDetails());
        return authenticationResult;
    }

    /**
     * 判断当前提供者是否支持指定的认证类型。
     *
     * @param authentication 认证对象的类型
     * @return 如果支持 {@link OAuth2DeviceAuthenticationToken}，返回 true；否则返回 false
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2DeviceAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
