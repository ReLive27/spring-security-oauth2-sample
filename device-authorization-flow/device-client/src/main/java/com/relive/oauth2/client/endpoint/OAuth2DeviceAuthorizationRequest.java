package com.relive.oauth2.client.endpoint;

import java.io.Serializable;

/**
 * @author: ReLive27
 * @date: 2024/5/2 09:27
 */
public class OAuth2DeviceAuthorizationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String registrationId;
    private String deviceCode;

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }
}
