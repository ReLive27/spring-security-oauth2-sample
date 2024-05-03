package com.relive.oauth2.client;

import com.relive.oauth2.client.endpoint.OAuth2DeviceAuthorizationRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author: ReLive27
 * @date: 2024/5/2 09:29
 */
public interface DeviceAuthorizationRequestRepository<T extends OAuth2DeviceAuthorizationRequest> {

    T loadAuthorizationRequest(HttpServletRequest request);

    void saveAuthorizationRequest(T authorizationRequest, HttpServletRequest request, HttpServletResponse response);

    T removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response);
}
