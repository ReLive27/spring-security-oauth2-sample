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
 * @author: ReLive27
 * @date: 2024/5/2 11:47
 */
public class OAuth2DeviceCodeAuthenticationToken extends AbstractAuthenticationToken {
    private Map<String, Object> additionalParameters;
    private ClientRegistration clientRegistration;
    private OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest;
    private OAuth2AccessToken accessToken;
    private OAuth2RefreshToken refreshToken;

    public OAuth2DeviceCodeAuthenticationToken(ClientRegistration clientRegistration, OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest) {
        super(Collections.emptyList());
        this.additionalParameters = new HashMap<>();
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        Assert.notNull(deviceAuthorizationRequest, "deviceAuthorizationRequest cannot be null");
        this.clientRegistration = clientRegistration;
        this.deviceAuthorizationRequest = deviceAuthorizationRequest;
    }

    public OAuth2DeviceCodeAuthenticationToken(ClientRegistration clientRegistration, OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest, OAuth2AccessToken accessToken) {
        this(clientRegistration, deviceAuthorizationRequest, accessToken, null);
    }

    public OAuth2DeviceCodeAuthenticationToken(ClientRegistration clientRegistration, OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest, OAuth2AccessToken accessToken, @Nullable OAuth2RefreshToken refreshToken) {
        this(clientRegistration, deviceAuthorizationRequest, accessToken, refreshToken, Collections.emptyMap());
    }

    public OAuth2DeviceCodeAuthenticationToken(ClientRegistration clientRegistration, OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest, OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken, Map<String, Object> additionalParameters) {
        this(clientRegistration, deviceAuthorizationRequest);
        Assert.notNull(accessToken, "accessToken cannot be null");
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.setAuthenticated(true);
        this.additionalParameters.putAll(additionalParameters);
    }

    public Object getPrincipal() {
        return this.clientRegistration.getClientId();
    }

    public Object getCredentials() {
        return this.accessToken != null ? this.accessToken.getTokenValue() : this.deviceAuthorizationRequest.getDeviceCode();
    }

    public ClientRegistration getClientRegistration() {
        return this.clientRegistration;
    }

    public OAuth2DeviceAuthorizationRequest getDeviceAuthorizationRequest() {
        return deviceAuthorizationRequest;
    }

    public OAuth2AccessToken getAccessToken() {
        return this.accessToken;
    }

    @Nullable
    public OAuth2RefreshToken getRefreshToken() {
        return this.refreshToken;
    }

    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }
}
