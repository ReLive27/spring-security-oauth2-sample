package com.relive.oauth2.client.endpoint;

import java.io.Serializable;

/**
 * 表示 OAuth2 设备授权请求的数据结构。
 * 该类包含与设备代码授权流程相关的基本信息，如注册 ID 和设备代码。
 * 它用于表示设备向授权服务器发起的授权请求。
 *
 * @author: ReLive27
 * @date: 2024/5/2 09:27
 */
public class OAuth2DeviceAuthorizationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户端注册 ID，标识该设备授权请求关联的客户端应用程序。
     */
    private String registrationId;

    /**
     * 设备代码，在设备代码授权流程中用于唯一标识设备。
     */
    private String deviceCode;

    /**
     * 获取客户端注册 ID。
     *
     * @return 客户端注册 ID
     */
    public String getRegistrationId() {
        return registrationId;
    }

    /**
     * 设置客户端注册 ID。
     *
     * @param registrationId 客户端注册 ID
     */
    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
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
     * 设置设备代码。
     *
     * @param deviceCode 设备代码
     */
    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }
}
