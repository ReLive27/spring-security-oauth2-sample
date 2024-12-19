package com.relive.oauth2.client;

import com.relive.oauth2.client.endpoint.OAuth2DeviceAuthorizationRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 设备授权请求存储库接口。
 * 该接口定义了设备授权请求的加载、保存和删除操作。
 * 用于处理 OAuth2 设备授权流程中的授权请求，并支持将授权请求存储在某个存储介质中，如数据库、内存或会话。
 *
 * @param <T> 设备授权请求类型，必须继承自 {@link OAuth2DeviceAuthorizationRequest}
 *
 * @author: ReLive27
 * @date: 2024/5/2 09:29
 */
public interface DeviceAuthorizationRequestRepository<T extends OAuth2DeviceAuthorizationRequest> {

    /**
     * 加载设备授权请求。
     *
     * @param request 当前的 HTTP 请求
     * @return 返回设备授权请求对象，若没有找到相关请求，则返回 null
     */
    T loadAuthorizationRequest(HttpServletRequest request);

    /**
     * 保存设备授权请求。
     *
     * @param authorizationRequest 需要保存的设备授权请求对象
     * @param request              当前的 HTTP 请求
     * @param response             当前的 HTTP 响应
     */
    void saveAuthorizationRequest(T authorizationRequest, HttpServletRequest request, HttpServletResponse response);

    /**
     * 删除设备授权请求。
     *
     * @param request  当前的 HTTP 请求
     * @param response 当前的 HTTP 响应
     * @return 返回被删除的设备授权请求对象，若请求未找到，则返回 null
     */
    T removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response);
}
