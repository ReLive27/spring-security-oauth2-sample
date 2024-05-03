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
 * @author: ReLive27
 * @date: 2024/5/2 13:32
 */
public class OAuth2DeviceAuthenticationToken extends AbstractAuthenticationToken {

    private OAuth2User principal;

    private ClientRegistration clientRegistration;

    private OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest;

    private OAuth2AccessToken accessToken;

    private OAuth2RefreshToken refreshToken;

    public OAuth2DeviceAuthenticationToken(ClientRegistration clientRegistration,
                                           OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest) {
        super(Collections.emptyList());
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        Assert.notNull(deviceAuthorizationRequest, "deviceAuthorizationRequest cannot be null");
        this.clientRegistration = clientRegistration;
        this.deviceAuthorizationRequest = deviceAuthorizationRequest;
        this.setAuthenticated(false);
    }


    public OAuth2DeviceAuthenticationToken(ClientRegistration clientRegistration,
                                           OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest, OAuth2User principal,
                                           Collection<? extends GrantedAuthority> authorities, OAuth2AccessToken accessToken) {
        this(clientRegistration, deviceAuthorizationRequest, principal, authorities, accessToken, null);
    }


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

    @Override
    public OAuth2User getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return "";
    }


    public ClientRegistration getClientRegistration() {
        return this.clientRegistration;
    }


    public OAuth2DeviceAuthorizationRequest getDeviceAuthorizationRequest() {
        return this.deviceAuthorizationRequest;
    }


    public OAuth2AccessToken getAccessToken() {
        return this.accessToken;
    }

    public @Nullable
    OAuth2RefreshToken getRefreshToken() {
        return this.refreshToken;
    }

}

