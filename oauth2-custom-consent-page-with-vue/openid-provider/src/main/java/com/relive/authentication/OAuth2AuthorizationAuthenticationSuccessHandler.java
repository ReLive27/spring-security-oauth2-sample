package com.relive.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 授权请求认证成功处理器。
 * <p>
 * 用于处理 OAuth2 授权码请求阶段认证成功的情况（即用户同意授权），
 * 返回 JSON 格式的重定向地址，供前端执行跳转操作。
 * </p>
 *
 * 响应格式：
 * <pre>
 * {
 *   "code": 302,
 *   "data": "http://client.example.com/callback?code=SplxlOBeZQQYbYS6WxSbIA&state=xyz"
 * }
 * </pre>
 *
 * @author: ReLive
 * @date: 2023/3/18 19:17
 */
public class OAuth2AuthorizationAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * 用于序列化 JSON 响应的消息转换器，默认使用 Jackson
     */
    private HttpMessageConverter<Object> httpMessageConverter = new MappingJackson2HttpMessageConverter();

    /**
     * 授权请求认证成功时调用，构建包含授权码的 redirect_uri 并返回 JSON。
     *
     * @param request        当前请求对象
     * @param response       当前响应对象
     * @param authentication 授权码认证成功的结果对象
     * @throws IOException      写入响应时异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication =
                (OAuth2AuthorizationCodeRequestAuthenticationToken) authentication;

        // 构建 redirect_uri，附带 code 和 state 参数
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(authorizationCodeRequestAuthentication.getRedirectUri())
                .queryParam(OAuth2ParameterNames.CODE, authorizationCodeRequestAuthentication.getAuthorizationCode().getTokenValue());

        if (StringUtils.hasText(authorizationCodeRequestAuthentication.getState())) {
            uriBuilder.queryParam(OAuth2ParameterNames.STATE, authorizationCodeRequestAuthentication.getState());
        }

        // 统一包装 JSON 格式响应
        Map<String, Object> responseClaims = new HashMap<>();
        responseClaims.put("code", HttpServletResponse.SC_MOVED_TEMPORARILY);
        responseClaims.put("data", uriBuilder.toUriString());

        // 写入响应
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        this.httpMessageConverter.write(responseClaims, null, httpResponse);
    }
}
