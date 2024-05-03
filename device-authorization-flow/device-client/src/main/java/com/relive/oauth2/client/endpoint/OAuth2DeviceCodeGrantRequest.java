package com.relive.oauth2.client.endpoint;

import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

/**
 * @author: ReLive27
 * @date: 2024/5/2 13:13
 */
public class OAuth2DeviceCodeGrantRequest extends AbstractOAuth2AuthorizationGrantRequest {
    private final OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest;

    public OAuth2DeviceCodeGrantRequest(ClientRegistration clientRegistration, OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest) {
        super(AuthorizationGrantType.DEVICE_CODE, clientRegistration);
        Assert.notNull(deviceAuthorizationRequest, "deviceAuthorizationRequest cannot be null");
        this.deviceAuthorizationRequest = deviceAuthorizationRequest;
    }

    public OAuth2DeviceAuthorizationRequest getDeviceAuthorizationRequest() {
        return deviceAuthorizationRequest;
    }
}
