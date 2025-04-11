package com.relive.authentication;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义认证成功处理器：用于登录成功后生成并返回 JWT 令牌。
 * <p>
 * 适用于前后端分离的场景，用户登录成功后不会跳转页面，而是通过响应体返回 token 数据。
 * </p>
 * <p>
 * 该类实现了 {@link AuthenticationSuccessHandler} 接口，结合 Spring Security 的认证流程，
 * 当用户认证成功时将调用此处理器生成 JWT 并返回 JSON 响应。
 * </p>
 *
 * 示例响应格式：
 * <pre>
 * {
 *   "code": 200,
 *   "data": {
 *     "token": "eyJraWQiOiJrZXktaWQ...签名后的JWT"
 *   }
 * }
 * </pre>
 *
 * @author: ReLive
 * @date: 2023/3/14 19:27
 */
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * 用于生成 JWT 的编码器
     */
    private final JwtEncoder jwtEncoder;

    /**
     * 用于将响应数据写入 HTTP 响应体的消息转换器，默认使用 Jackson
     */
    private HttpMessageConverter<Object> httpMessageConverter = new MappingJackson2HttpMessageConverter();

    /**
     * 构造方法，基于 JWKSource 创建 JWT 编码器
     *
     * @param jwkSource 用于签名 JWT 的密钥源
     */
    public JwtAuthenticationSuccessHandler(JWKSource<SecurityContext> jwkSource) {
        Assert.notNull(jwkSource, "jwkSource can not be null");
        this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
    }

    /**
     * 认证成功处理方法，生成 JWT 并返回给客户端
     *
     * @param request        当前请求对象
     * @param response       当前响应对象
     * @param authentication 当前认证信息，包含用户名等信息
     * @throws IOException      写入响应时异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        // 设置签发时间和过期时间
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(30, ChronoUnit.MINUTES);
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
        claimsBuilder.subject(authentication.getName())
                .issuer("http://127.0.0.1:8080")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .notBefore(issuedAt);

        JwsHeader.Builder headersBuilder = JwsHeader.with(SignatureAlgorithm.RS256);

        JwsHeader headers = headersBuilder.build();
        JwtClaimsSet claims = claimsBuilder.build();

        // 编码生成 JWT
        Jwt jwt = this.jwtEncoder.encode(JwtEncoderParameters.from(headers, claims));

        // 构建返回响应体
        Map<String, Object> responseClaims = new LinkedHashMap<>();
        responseClaims.put("code", HttpServletResponse.SC_OK);
        responseClaims.put("data", Collections.singletonMap("token", jwt.getTokenValue()));

        // 将响应写入客户端
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        this.httpMessageConverter.write(responseClaims, null, httpResponse);
    }

    /**
     * 设置自定义的 HttpMessageConverter
     *
     * @param httpMessageConverter 可自定义的响应体转换器
     */
    public void setHttpMessageConverter(HttpMessageConverter<Object> httpMessageConverter) {
        this.httpMessageConverter = httpMessageConverter;
    }
}
