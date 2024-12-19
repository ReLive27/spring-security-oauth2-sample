package com.relive.oauth2.client.authentication;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * OAuth2 设备授权请求令牌。
 * <p>
 * 此类代表 OAuth2 设备授权请求过程中的令牌，它存储与设备授权请求相关的信息，如设备代码、用户代码、验证 URI 等。
 * 它继承自 {@link AbstractAuthenticationToken}，用于 Spring Security 的认证流程。
 * </p>
 *
 * @author: ReLive27
 * @date: 2024/5/1 11:55
 */
public class OAuth2DeviceAuthorizationRequestToken extends AbstractAuthenticationToken {

    private final String userCode;
    private final String deviceCode;
    private final String verificationUriComplete;
    private final String verificationUri;
    private final Integer expiresIn;
    private final ClientRegistration clientRegistration;

    /**
     * @param clientRegistration 与此设备授权请求相关联的 {@link ClientRegistration}
     * @throws IllegalArgumentException 如果 {@code clientRegistration} 为 {@code null}
     */
    public OAuth2DeviceAuthorizationRequestToken(ClientRegistration clientRegistration) {
        super(Collections.emptyList());
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        this.clientRegistration = clientRegistration;
        this.userCode = null;
        this.deviceCode = null;
        this.verificationUri = null;
        this.verificationUriComplete = null;
        this.expiresIn = null;
    }

    /**
     * @param userCode                完整的用户代码
     * @param deviceCode              完整的设备代码
     * @param verificationUri         设备授权的验证 URI
     * @param expiresIn               设备授权的有效期（秒）
     * @param clientRegistration      与此设备授权请求相关联的 {@link ClientRegistration}
     * @param verificationUriComplete 完整的验证 URI（可选）
     * @throws IllegalArgumentException 如果任一必填字段为空
     */
    public OAuth2DeviceAuthorizationRequestToken(String userCode, String deviceCode,
                                                 String verificationUri, Integer expiresIn, ClientRegistration clientRegistration, @Nullable String verificationUriComplete) {
        super(Collections.emptyList());
        Assert.hasText(userCode, "userCode cannot be empty");
        Assert.hasText(deviceCode, "deviceCode cannot be empty");
        Assert.hasText(verificationUri, "verificationUri cannot be empty");
        Assert.notNull(expiresIn, "expiresIn cannot be null");
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        this.userCode = userCode;
        this.deviceCode = deviceCode;
        this.verificationUri = verificationUri;
        this.expiresIn = expiresIn;
        this.verificationUriComplete = verificationUriComplete;
        this.clientRegistration = clientRegistration;
        setAuthenticated(true);
    }

    /**
     * 返回凭证信息。
     *
     * @return 返回空字符串
     */
    @Override
    public Object getCredentials() {
        return "";
    }

    /**
     * 返回该令牌的主体，OAuth2 设备授权请求令牌的主体为空。
     *
     * @return 返回 {@code null}
     */
    @Override
    public Object getPrincipal() {
        return null;
    }

    /**
     * 获取用户代码。
     *
     * @return 用户代码
     */
    public String getUserCode() {
        return userCode;
    }

    /**
     * 获取设备代码。
     *
     * @return 设备代码
     */
    public String getDeviceCode() {
        return deviceCode;
    }

    /**
     * 获取完整的验证 URI（可选）。
     *
     * @return 完整的验证 URI，可能为 {@code null}
     */
    public String getVerificationUriComplete() {
        return verificationUriComplete;
    }

    /**
     * 获取验证 URI。
     *
     * @return 验证 URI
     */
    public String getVerificationUri() {
        return verificationUri;
    }

    /**
     * 获取过期时间，单位为秒。
     *
     * @return 过期时间
     */
    public Integer getExpiresIn() {
        return expiresIn;
    }

    /**
     * 获取与此设备授权请求相关联的 {@link ClientRegistration}。
     *
     * @return {@link ClientRegistration}
     */
    public ClientRegistration getClientRegistration() {
        return clientRegistration;
    }
}
