package com.relive.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 授权请求认证失败处理器。
 * <p>
 * 用于处理 OAuth2 授权码请求阶段发生异常的情况（如参数错误、客户端未注册等），
 * 返回 JSON 格式的错误信息，或提供重定向 URI 供前端跳转显示错误信息。
 * </p>
 *
 * 响应格式：
 * - 若未提供 redirect_uri，则响应如下：
 * <pre>
 * {
 *   "code": 400,
 *   "message": "invalid_request: Missing required parameter: client_id"
 * }
 * </pre>
 *
 * - 若提供了 redirect_uri，则响应如下：
 * <pre>
 * {
 *   "code": 302,
 *   "data": "http://client.example.com/callback?error=invalid_request&error_description=..."
 * }
 * </pre>
 *
 * @author: ReLive
 * @date: 2023/3/20 18:33
 */
public class OAuth2AuthorizationAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * 用于序列化 JSON 响应的消息转换器，默认使用 Jackson
     */
    private HttpMessageConverter<Object> httpMessageConverter = new MappingJackson2HttpMessageConverter();

    /**
     * 认证失败时调用，处理授权请求异常并生成响应
     *
     * @param request   当前请求对象
     * @param response  当前响应对象
     * @param exception 认证失败抛出的异常
     * @throws IOException      写入响应时异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        OAuth2AuthorizationCodeRequestAuthenticationException authorizationCodeRequestAuthenticationException =
                (OAuth2AuthorizationCodeRequestAuthenticationException) exception;

        // 获取错误详情和原始授权请求信息
        OAuth2Error error = authorizationCodeRequestAuthenticationException.getError();
        OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication =
                authorizationCodeRequestAuthenticationException.getAuthorizationCodeRequestAuthentication();

        Map<String, Object> responseClaims = new HashMap<>();

        // 情况一：redirect_uri 缺失或为空，直接返回 JSON 错误信息
        if (authorizationCodeRequestAuthentication == null ||
                !StringUtils.hasText(authorizationCodeRequestAuthentication.getRedirectUri())) {

            responseClaims.put("code", HttpServletResponse.SC_BAD_REQUEST);
            responseClaims.put("message", error.toString());
            ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
            this.httpMessageConverter.write(responseClaims, null, httpResponse);
            return;
        }

        // 情况二：redirect_uri 存在，构建 redirect 错误 URI，并作为 JSON 返回
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(authorizationCodeRequestAuthentication.getRedirectUri())
                .queryParam(OAuth2ParameterNames.ERROR, error.getErrorCode());

        if (StringUtils.hasText(error.getDescription())) {
            uriBuilder.queryParam(OAuth2ParameterNames.ERROR_DESCRIPTION, error.getDescription());
        }

        if (StringUtils.hasText(error.getUri())) {
            uriBuilder.queryParam(OAuth2ParameterNames.ERROR_URI, error.getUri());
        }

        if (StringUtils.hasText(authorizationCodeRequestAuthentication.getState())) {
            uriBuilder.queryParam(OAuth2ParameterNames.STATE, authorizationCodeRequestAuthentication.getState());
        }

        responseClaims.put("code", HttpServletResponse.SC_MOVED_TEMPORARILY);
        responseClaims.put("data", uriBuilder.toUriString());

        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        this.httpMessageConverter.write(responseClaims, null, httpResponse);
    }
}
