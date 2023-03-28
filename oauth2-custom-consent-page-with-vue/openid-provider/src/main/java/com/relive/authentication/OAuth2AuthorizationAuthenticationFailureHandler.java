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
 * @author: ReLive
 * @date: 2023/3/20 18:33
 */
public class OAuth2AuthorizationAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private HttpMessageConverter<Object> httpMessageConverter = new MappingJackson2HttpMessageConverter();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        OAuth2AuthorizationCodeRequestAuthenticationException authorizationCodeRequestAuthenticationException =
                (OAuth2AuthorizationCodeRequestAuthenticationException) exception;
        OAuth2Error error = authorizationCodeRequestAuthenticationException.getError();
        OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication =
                authorizationCodeRequestAuthenticationException.getAuthorizationCodeRequestAuthentication();

        Map<String, Object> responseClaims = new HashMap<>();

        if (authorizationCodeRequestAuthentication == null ||
                !StringUtils.hasText(authorizationCodeRequestAuthentication.getRedirectUri())) {

            responseClaims.put("code", HttpServletResponse.SC_BAD_REQUEST);
            responseClaims.put("message", error.toString());
            ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
            this.httpMessageConverter.write(responseClaims, null, httpResponse);
            return;
        }

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
