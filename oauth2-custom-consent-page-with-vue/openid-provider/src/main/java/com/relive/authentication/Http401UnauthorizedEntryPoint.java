package com.relive.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 自定义未认证处理器：用于处理未登录访问受保护资源时的响应。
 * <p>
 * 实现了 {@link AuthenticationEntryPoint} 接口，在用户未通过身份认证时调用，
 * 返回 401 Unauthorized 状态码，并携带简单提示信息。
 * </p>
 * <p>
 * 常用于前后端分离系统，当用户未登录访问需要认证的接口时，
 * 后端不会重定向至登录页面，而是返回 JSON 或状态码提示前端跳转登录。
 * </p>
 * <p>
 * 示例响应：
 * <pre>
 * HTTP/1.1 401 Unauthorized
 * Content-Type: text/html
 *
 * Unauthorized
 * </pre>
 *
 * @author: ReLive
 * @date: 2023/3/14 19:14
 */
public class Http401UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    /**
     * 处理未认证请求的方法。
     * <p>
     * 该方法会向客户端发送一个 HTTP 401 状态码和提示信息。
     *
     * @param request       HTTP 请求对象
     * @param response      HTTP 响应对象
     * @param authException 认证异常信息
     * @throws IOException 写响应时可能抛出异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
