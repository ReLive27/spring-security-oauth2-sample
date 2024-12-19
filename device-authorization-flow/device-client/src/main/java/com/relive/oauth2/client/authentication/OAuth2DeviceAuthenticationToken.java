package com.relive.oauth2.client.authentication;

import com.relive.oauth2.client.endpoint.OAuth2DeviceAuthorizationRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;


/**
 * 用于在设备认证流程中存储认证相关信息。
 * <p>
 * 该类扩展了 {@link AbstractAuthenticationToken}，并封装了与设备授权请求、用户信息、访问令牌等相关的数据。
 *
 * @author: ReLive27
 * @date: 2024/5/2 13:32
 */
public class OAuth2DeviceAuthenticationToken extends AbstractAuthenticationToken {

    private OAuth2User principal;

    private ClientRegistration clientRegistration;

    private OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest;

    private OAuth2AccessToken accessToken;

    private OAuth2RefreshToken refreshToken;

    /**
     * @param clientRegistration         OAuth 2.0 客户端注册信息，不能为空
     * @param deviceAuthorizationRequest 设备授权请求信息，不能为空
     * @throws IllegalArgumentException 如果参数为 null
     */
    public OAuth2DeviceAuthenticationToken(ClientRegistration clientRegistration,
                                           OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest) {
        super(Collections.emptyList());
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        Assert.notNull(deviceAuthorizationRequest, "deviceAuthorizationRequest cannot be null");
        this.clientRegistration = clientRegistration;
        this.deviceAuthorizationRequest = deviceAuthorizationRequest;
        this.setAuthenticated(false);
    }

    /**
     * @param clientRegistration         OAuth 2.0 客户端注册信息
     * @param deviceAuthorizationRequest 设备授权请求信息
     * @param principal                  已认证的用户主体
     * @param authorities                用户权限集合
     * @param accessToken                OAuth 2.0 访问令牌
     */
    public OAuth2DeviceAuthenticationToken(ClientRegistration clientRegistration,
                                           OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest, OAuth2User principal,
                                           Collection<? extends GrantedAuthority> authorities, OAuth2AccessToken accessToken) {
        this(clientRegistration, deviceAuthorizationRequest, principal, authorities, accessToken, null);
    }

    /**
     * @param clientRegistration         OAuth 2.0 客户端注册信息
     * @param deviceAuthorizationRequest 设备授权请求信息
     * @param principal                  已认证的用户主体
     * @param authorities                用户权限集合
     * @param accessToken                OAuth 2.0 访问令牌
     * @param refreshToken               可选的 OAuth 2.0 刷新令牌
     * @throws IllegalArgumentException 如果必需参数为 null
     */
    public OAuth2DeviceAuthenticationToken(ClientRegistration clientRegistration,
                                           OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest, OAuth2User principal,
                                           Collection<? extends GrantedAuthority> authorities, OAuth2AccessToken accessToken,
                                           @Nullable OAuth2RefreshToken refreshToken) {
        super(authorities);
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        Assert.notNull(deviceAuthorizationRequest, "deviceAuthorizationRequest cannot be null");
        Assert.notNull(principal, "principal cannot be null");
        Assert.notNull(accessToken, "accessToken cannot be null");
        this.clientRegistration = clientRegistration;
        this.deviceAuthorizationRequest = deviceAuthorizationRequest;
        this.principal = principal;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.setAuthenticated(true);
    }

    /**
     * 获取已认证的用户主体。
     *
     * @return 认证后的用户信息 {@link OAuth2User}
     */
    @Override
    public OAuth2User getPrincipal() {
        return this.principal;
    }

    /**
     * 获取凭据信息。
     * <p>
     * 返回空字符串。
     *
     * @return 空字符串
     */
    @Override
    public Object getCredentials() {
        return "";
    }

    /**
     * 获取 OAuth 2.0 客户端注册信息。
     *
     * @return 客户端注册信息 {@link ClientRegistration}
     */
    public ClientRegistration getClientRegistration() {
        return this.clientRegistration;
    }

    /**
     * 获取设备授权请求信息。
     *
     * @return 设备授权请求 {@link OAuth2DeviceAuthorizationRequest}
     */
    public OAuth2DeviceAuthorizationRequest getDeviceAuthorizationRequest() {
        return this.deviceAuthorizationRequest;
    }

    /**
     * 获取 OAuth 2.0 访问令牌。
     *
     * @return 访问令牌 {@link OAuth2AccessToken}
     */
    public OAuth2AccessToken getAccessToken() {
        return this.accessToken;
    }

    /**
     * 获取 OAuth 2.0 刷新令牌（如果存在）。
     *
     * @return 刷新令牌 {@link OAuth2RefreshToken}，或者 null
     */
    public @Nullable
    OAuth2RefreshToken getRefreshToken() {
        return this.refreshToken;
    }
}
