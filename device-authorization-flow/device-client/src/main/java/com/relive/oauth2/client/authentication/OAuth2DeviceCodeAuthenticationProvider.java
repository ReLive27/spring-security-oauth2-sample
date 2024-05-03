package com.relive.oauth2.client.authentication;

import com.relive.oauth2.client.endpoint.OAuth2DeviceCodeGrantRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.Assert;

/**
 * @author: ReLive27
 * @date: 2024/5/2 11:43
 */
public class OAuth2DeviceCodeAuthenticationProvider implements AuthenticationProvider {
    private final OAuth2AccessTokenResponseClient<OAuth2DeviceCodeGrantRequest> accessTokenResponseClient;

    public OAuth2DeviceCodeAuthenticationProvider(
            OAuth2AccessTokenResponseClient<OAuth2DeviceCodeGrantRequest> accessTokenResponseClient) {
        Assert.notNull(accessTokenResponseClient, "accessTokenResponseClient cannot be null");
        this.accessTokenResponseClient = accessTokenResponseClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2DeviceCodeAuthenticationToken deviceCodeAuthenticationToken = (OAuth2DeviceCodeAuthenticationToken) authentication;

        OAuth2AccessTokenResponse accessTokenResponse = this.accessTokenResponseClient.getTokenResponse(
                new OAuth2DeviceCodeGrantRequest(deviceCodeAuthenticationToken.getClientRegistration(),
                        deviceCodeAuthenticationToken.getDeviceAuthorizationRequest()));
        OAuth2DeviceCodeAuthenticationToken authenticationResult = new OAuth2DeviceCodeAuthenticationToken(
                deviceCodeAuthenticationToken.getClientRegistration(),
                deviceCodeAuthenticationToken.getDeviceAuthorizationRequest(), accessTokenResponse.getAccessToken(),
                accessTokenResponse.getRefreshToken(), accessTokenResponse.getAdditionalParameters());
        authenticationResult.setDetails(deviceCodeAuthenticationToken.getDetails());
        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2DeviceCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
