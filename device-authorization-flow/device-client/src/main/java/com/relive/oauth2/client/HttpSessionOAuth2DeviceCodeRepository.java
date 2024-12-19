package com.relive.oauth2.client;

import com.relive.oauth2.client.endpoint.OAuth2DeviceAuthorizationRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.Assert;

/**
 * 基于 HTTP 会话的设备授权请求存储库实现。
 * 该实现将设备授权请求存储在 HTTP 会话中，适用于需要在用户会话期间存储和访问设备授权请求的场景。
 *
 * @author: ReLive27
 * @date: 2024/5/1 22:57
 * @see DeviceAuthorizationRequestRepository
 */
public class HttpSessionOAuth2DeviceCodeRepository implements DeviceAuthorizationRequestRepository<OAuth2DeviceAuthorizationRequest> {

    /**
     * 默认的设备授权请求属性名。
     * 该属性名用于将设备授权请求存储在 HTTP 会话中。
     */
    private static final String DEFAULT_DEVICE_CODE_ATTR_NAME = HttpSessionOAuth2DeviceCodeRepository.class
            .getName() + ".DEVICE_CODE";

    /**
     * 会话属性名，用于存储设备授权请求。
     */
    private final String sessionAttributeName = DEFAULT_DEVICE_CODE_ATTR_NAME;

    /**
     * 从 HTTP 请求中加载设备授权请求。
     *
     * @param request 当前的 HTTP 请求
     * @return 返回设备授权请求对象，如果会话中没有找到相关请求则返回 null
     */
    @Override
    public OAuth2DeviceAuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        OAuth2DeviceAuthorizationRequest authorizationRequest = getAuthorizationRequest(request);
        return authorizationRequest;
    }

    /**
     * 将设备授权请求保存到 HTTP 会话中。
     *
     * @param authorizationRequest 需要保存的设备授权请求对象
     * @param request              当前的 HTTP 请求
     * @param response             当前的 HTTP 响应
     */
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

    /**
     * 从 HTTP 会话中删除设备授权请求。
     *
     * @param request  当前的 HTTP 请求
     * @param response 当前的 HTTP 响应
     * @return 返回被删除的设备授权请求对象，如果会话中没有找到相关请求则返回 null
     */
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

    /**
     * 从 HTTP 会话中获取设备授权请求。
     *
     * @param request 当前的 HTTP 请求
     * @return 返回设备授权请求对象，如果会话中没有找到相关请求则返回 null
     */
    private OAuth2DeviceAuthorizationRequest getAuthorizationRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (OAuth2DeviceAuthorizationRequest) session.getAttribute(this.sessionAttributeName) : null;
    }
}
