package com.relive.oauth2.client.authentication;

import com.relive.oauth2.client.endpoint.OAuth2DeviceCodeGrantRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.Assert;

/**
 * OAuth2 设备代码认证提供者。
 * <p>
 * 此类提供了 OAuth2 设备代码授权流程中的认证功能。它使用 {@link OAuth2AccessTokenResponseClient}
 * 获取设备代码授权请求的访问令牌，并构造一个 {@link OAuth2DeviceCodeAuthenticationToken}
 * 作为认证结果返回。
 * </p>
 *
 * @author: ReLive27
 * @date: 2024/5/2 11:43
 */
public class OAuth2DeviceCodeAuthenticationProvider implements AuthenticationProvider {

    private final OAuth2AccessTokenResponseClient<OAuth2DeviceCodeGrantRequest> accessTokenResponseClient;

    /**
     * 构造函数，初始化 {@link OAuth2DeviceCodeAuthenticationProvider}。
     *
     * @param accessTokenResponseClient 用于获取设备代码授权访问令牌的客户端
     * @throws IllegalArgumentException 如果 {@code accessTokenResponseClient} 为 {@code null}
     */
    public OAuth2DeviceCodeAuthenticationProvider(
            OAuth2AccessTokenResponseClient<OAuth2DeviceCodeGrantRequest> accessTokenResponseClient) {
        Assert.notNull(accessTokenResponseClient, "accessTokenResponseClient cannot be null");
        this.accessTokenResponseClient = accessTokenResponseClient;
    }

    /**
     * 对 {@link OAuth2DeviceCodeAuthenticationToken} 进行认证。
     * <p>
     * 此方法调用 {@link OAuth2AccessTokenResponseClient} 获取设备代码授权请求的访问令牌，
     * 并将其封装在一个 {@link OAuth2DeviceCodeAuthenticationToken} 中返回。
     * </p>
     *
     * @param authentication 要认证的 {@link Authentication} 对象，必须是 {@link OAuth2DeviceCodeAuthenticationToken}
     * @return 返回认证后的 {@link OAuth2DeviceCodeAuthenticationToken} 对象
     * @throws AuthenticationException 如果认证失败，抛出异常
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2DeviceCodeAuthenticationToken deviceCodeAuthenticationToken = (OAuth2DeviceCodeAuthenticationToken) authentication;

        // 获取设备授权请求的访问令牌
        OAuth2AccessTokenResponse accessTokenResponse = this.accessTokenResponseClient.getTokenResponse(
                new OAuth2DeviceCodeGrantRequest(deviceCodeAuthenticationToken.getClientRegistration(),
                        deviceCodeAuthenticationToken.getDeviceAuthorizationRequest()));

        // 构造认证结果
        OAuth2DeviceCodeAuthenticationToken authenticationResult = new OAuth2DeviceCodeAuthenticationToken(
                deviceCodeAuthenticationToken.getClientRegistration(),
                deviceCodeAuthenticationToken.getDeviceAuthorizationRequest(),
                accessTokenResponse.getAccessToken(),
                accessTokenResponse.getRefreshToken(),
                accessTokenResponse.getAdditionalParameters());

        // 设置认证详细信息
        authenticationResult.setDetails(deviceCodeAuthenticationToken.getDetails());
        return authenticationResult;
    }

    /**
     * 检查是否支持 {@link OAuth2DeviceCodeAuthenticationToken} 类型的认证。
     *
     * @param authentication 认证类型
     * @return 如果支持，返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2DeviceCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
