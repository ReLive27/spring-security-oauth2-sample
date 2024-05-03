package com.relive.oauth2.client.authentication;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.Assert;

import java.util.Collections;

/**
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


    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return null;
    }


    public String getUserCode() {
        return userCode;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public String getVerificationUriComplete() {
        return verificationUriComplete;
    }

    public String getVerificationUri() {
        return verificationUri;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public ClientRegistration getClientRegistration() {
        return clientRegistration;
    }
}
