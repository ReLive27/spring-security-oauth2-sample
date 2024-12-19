package com.relive.oauth2.client.endpoint;

import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

/**
 * 表示 OAuth2 设备代码授权请求。
 * 该类扩展了 {@link AbstractOAuth2AuthorizationGrantRequest}，用于处理 OAuth2 设备代码授权流程中的授权请求。
 * 它包含设备授权请求的相关信息，且用于获取授权服务器的设备代码授权响应。
 *
 * @author: ReLive27
 * @date: 2024/5/2 13:13
 */
public class OAuth2DeviceCodeGrantRequest extends AbstractOAuth2AuthorizationGrantRequest {

    /**
     * 设备授权请求对象，包含与设备授权流程相关的信息。
     */
    private final OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest;

    /**
     * @param clientRegistration         客户端注册信息，包含 OAuth2 客户端的配置
     * @param deviceAuthorizationRequest 设备授权请求对象，包含设备授权流程中的相关数据
     * @throws IllegalArgumentException 如果 deviceAuthorizationRequest 为 null
     */
    public OAuth2DeviceCodeGrantRequest(ClientRegistration clientRegistration, OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest) {
        super(AuthorizationGrantType.DEVICE_CODE, clientRegistration);
        Assert.notNull(deviceAuthorizationRequest, "deviceAuthorizationRequest cannot be null");
        this.deviceAuthorizationRequest = deviceAuthorizationRequest;
    }

    /**
     * 获取设备授权请求对象。
     *
     * @return 设备授权请求对象
     */
    public OAuth2DeviceAuthorizationRequest getDeviceAuthorizationRequest() {
        return deviceAuthorizationRequest;
    }
}
