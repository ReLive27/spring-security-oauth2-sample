package com.relive.oauth2.client;

import com.relive.oauth2.client.endpoint.OAuth2DeviceAuthorizationRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.Assert;

/**
 * @author: ReLive27
 * @date: 2024/5/1 22:57
 */
public class HttpSessionOAuth2DeviceCodeRepository implements DeviceAuthorizationRequestRepository<OAuth2DeviceAuthorizationRequest> {
    private static final String DEFAULT_DEVICE_CODE_ATTR_NAME = HttpSessionOAuth2DeviceCodeRepository.class
            .getName() + ".DEVICE_CODE";

    private final String sessionAttributeName = DEFAULT_DEVICE_CODE_ATTR_NAME;

    @Override
    public OAuth2DeviceAuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        OAuth2DeviceAuthorizationRequest authorizationRequest = getAuthorizationRequest(request);
        return authorizationRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2DeviceAuthorizationRequest authorizationRequest, HttpServletRequest request,
                                         HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }
        request.getSession().setAttribute(this.sessionAttributeName, authorizationRequest);
    }

    @Override
    public OAuth2DeviceAuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                       HttpServletResponse response) {
        Assert.notNull(response, "response cannot be null");
        OAuth2DeviceAuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        if (authorizationRequest != null) {
            request.getSession().removeAttribute(this.sessionAttributeName);
        }
        return authorizationRequest;
    }

    private OAuth2DeviceAuthorizationRequest getAuthorizationRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (OAuth2DeviceAuthorizationRequest) session.getAttribute(this.sessionAttributeName) : null;
    }
}
