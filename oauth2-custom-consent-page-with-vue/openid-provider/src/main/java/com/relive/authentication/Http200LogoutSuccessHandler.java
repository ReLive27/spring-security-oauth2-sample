package com.relive.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义登出成功处理器：返回 HTTP 200 状态码与 JSON 响应。
 * <p>
 * 该类实现 Spring Security 的 {@link LogoutSuccessHandler} 接口，
 * 用于在用户登出成功后返回自定义格式的 JSON 响应，而不是重定向或空响应。
 * </p>
 * <p>
 * 返回格式示例：
 * <pre>
 * {
 *     "code": 200,
 *     "message": "success"
 * }
 * </pre>
 * <p>
 * 适用于前后端分离场景，便于前端接收和处理退出结果。
 *
 * @author: ReLive
 * @date: 2023/3/14 19:54
 */
public class Http200LogoutSuccessHandler implements LogoutSuccessHandler {

    // Jackson JSON 序列化器
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 登出成功后的回调方法。
     *
     * @param request        HTTP 请求对象
     * @param response       HTTP 响应对象
     * @param authentication 当前认证信息，可能为 null（比如 token 已失效或无认证信息）
     * @throws IOException      写入响应时抛出的异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 设置响应状态为 200 OK
        response.setStatus(HttpStatus.OK.value());
        // 设置响应类型为 application/json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // 构造响应体
        Map<String, Object> responseClaims = new LinkedHashMap<>();
        responseClaims.put("code", HttpServletResponse.SC_OK);
        responseClaims.put("message", "success");

        // 写入 JSON 响应
        try (Writer writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(responseClaims));
        }
    }
}

