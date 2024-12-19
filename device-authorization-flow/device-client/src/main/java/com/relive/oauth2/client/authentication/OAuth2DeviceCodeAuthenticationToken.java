package com.relive.oauth2.client.authentication;

import com.relive.oauth2.client.endpoint.OAuth2DeviceAuthorizationRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 此类用于在 OAuth2 设备代码授权流程中封装认证信息。它包含了客户端注册信息、设备授权请求、访问令牌、刷新令牌以及附加参数。
 * </p>
 *
 * @author: ReLive27
 * @date: 2024/5/2 11:47
 */
public class OAuth2DeviceCodeAuthenticationToken extends AbstractAuthenticationToken {

    private Map<String, Object> additionalParameters;
    private ClientRegistration clientRegistration;
    private OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest;
    private OAuth2AccessToken accessToken;
    private OAuth2RefreshToken refreshToken;

    /**
     * 构造函数，用于初始化 OAuth2DeviceCodeAuthenticationToken。
     *
     * @param clientRegistration         客户端注册信息
     * @param deviceAuthorizationRequest 设备授权请求
     * @throws IllegalArgumentException 如果 {@code clientRegistration} 或 {@code deviceAuthorizationRequest} 为 {@code null}
     */
    public OAuth2DeviceCodeAuthenticationToken(ClientRegistration clientRegistration, OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest) {
        super(Collections.emptyList());
        this.additionalParameters = new HashMap<>();
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        Assert.notNull(deviceAuthorizationRequest, "deviceAuthorizationRequest cannot be null");
        this.clientRegistration = clientRegistration;
        this.deviceAuthorizationRequest = deviceAuthorizationRequest;
    }

    /**
     * 构造函数，用于初始化 OAuth2DeviceCodeAuthenticationToken，且只有访问令牌。
     *
     * @param clientRegistration         客户端注册信息
     * @param deviceAuthorizationRequest 设备授权请求
     * @param accessToken                访问令牌
     * @throws IllegalArgumentException 如果 {@code clientRegistration} 或 {@code deviceAuthorizationRequest} 或 {@code accessToken} 为 {@code null}
     */
    public OAuth2DeviceCodeAuthenticationToken(ClientRegistration clientRegistration, OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest, OAuth2AccessToken accessToken) {
        this(clientRegistration, deviceAuthorizationRequest, accessToken, null);
    }

    /**
     * 构造函数，用于初始化 OAuth2DeviceCodeAuthenticationToken，包含访问令牌和刷新令牌。
     *
     * @param clientRegistration         客户端注册信息
     * @param deviceAuthorizationRequest 设备授权请求
     * @param accessToken                访问令牌
     * @param refreshToken               刷新令牌
     * @throws IllegalArgumentException 如果 {@code clientRegistration} 或 {@code deviceAuthorizationRequest} 或 {@code accessToken} 为 {@code null}
     */
    public OAuth2DeviceCodeAuthenticationToken(ClientRegistration clientRegistration, OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest, OAuth2AccessToken accessToken, @Nullable OAuth2RefreshToken refreshToken) {
        this(clientRegistration, deviceAuthorizationRequest, accessToken, refreshToken, Collections.emptyMap());
    }

    /**
     * 构造函数，用于初始化 OAuth2DeviceCodeAuthenticationToken，包含访问令牌、刷新令牌和附加参数。
     *
     * @param clientRegistration         客户端注册信息
     * @param deviceAuthorizationRequest 设备授权请求
     * @param accessToken                访问令牌
     * @param refreshToken               刷新令牌
     * @param additionalParameters       附加参数
     * @throws IllegalArgumentException 如果 {@code clientRegistration} 或 {@code deviceAuthorizationRequest} 或 {@code accessToken} 为 {@code null}
     */
    public OAuth2DeviceCodeAuthenticationToken(ClientRegistration clientRegistration, OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest, OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken, Map<String, Object> additionalParameters) {
        this(clientRegistration, deviceAuthorizationRequest);
        Assert.notNull(accessToken, "accessToken cannot be null");
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.setAuthenticated(true);
        this.additionalParameters.putAll(additionalParameters);
    }

    /**
     * 获取认证主体信息（即客户端 ID）。
     *
     * @return 客户端 ID
     */
    @Override
    public Object getPrincipal() {
        return this.clientRegistration.getClientId();
    }

    /**
     * 获取认证凭据（即访问令牌的值或设备代码）。
     *
     * @return 访问令牌的值或设备代码
     */
    @Override
    public Object getCredentials() {
        return this.accessToken != null ? this.accessToken.getTokenValue() : this.deviceAuthorizationRequest.getDeviceCode();
    }

    /**
     * 获取客户端注册信息。
     *
     * @return {@link ClientRegistration} 对象
     */
    public ClientRegistration getClientRegistration() {
        return this.clientRegistration;
    }

    /**
     * 获取设备授权请求。
     *
     * @return {@link OAuth2DeviceAuthorizationRequest} 对象
     */
    public OAuth2DeviceAuthorizationRequest getDeviceAuthorizationRequest() {
        return deviceAuthorizationRequest;
    }

    /**
     * 获取访问令牌。
     *
     * @return {@link OAuth2AccessToken} 对象
     */
    public OAuth2AccessToken getAccessToken() {
        return this.accessToken;
    }

    /**
     * 获取刷新令牌（如果有）。
     *
     * @return {@link OAuth2RefreshToken} 对象，可能为 {@code null}
     */
    @Nullable
    public OAuth2RefreshToken getRefreshToken() {
        return this.refreshToken;
    }

    /**
     * 获取附加的参数。
     *
     * @return 附加的参数映射
     */
    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }
}
