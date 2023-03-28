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
 * @author: ReLive
 * @date: 2023/3/14 19:27
 */
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtEncoder jwtEncoder;
    private HttpMessageConverter<Object> httpMessageConverter = new MappingJackson2HttpMessageConverter();

    public JwtAuthenticationSuccessHandler(JWKSource<SecurityContext> jwkSource) {
        Assert.notNull(jwkSource, "jwkSource can not be null");
        this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
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

        Jwt jwt = this.jwtEncoder.encode(JwtEncoderParameters.from(headers, claims));
        Map<String, Object> responseClaims = new LinkedHashMap<>();
        responseClaims.put("code", HttpServletResponse.SC_OK);
        responseClaims.put("data", Collections.singletonMap("token", jwt.getTokenValue()));

        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        this.httpMessageConverter.write(responseClaims, null, httpResponse);
    }

    public void setHttpMessageConverter(HttpMessageConverter<Object> httpMessageConverter) {
        this.httpMessageConverter = httpMessageConverter;
    }
}
