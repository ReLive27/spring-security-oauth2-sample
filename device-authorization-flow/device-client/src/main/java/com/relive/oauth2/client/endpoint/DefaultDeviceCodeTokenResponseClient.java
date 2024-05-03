package com.relive.oauth2.client.endpoint;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * @author: ReLive27
 * @date: 2024/5/2 13:20
 */
public class DefaultDeviceCodeTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2DeviceCodeGrantRequest> {
    private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";
    private Converter<OAuth2DeviceCodeGrantRequest, RequestEntity<?>> requestEntityConverter = new OAuth2DeviceCodeGrantRequestEntityConverter();
    private RestOperations restOperations;

    public DefaultDeviceCodeTokenResponseClient() {
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        this.restOperations = restTemplate;
    }

    public OAuth2AccessTokenResponse getTokenResponse(OAuth2DeviceCodeGrantRequest deviceCodeGrantRequest) {
        Assert.notNull(deviceCodeGrantRequest, "deviceCodeGrantRequest cannot be null");
        RequestEntity<?> request = this.requestEntityConverter.convert(deviceCodeGrantRequest);
        ResponseEntity<OAuth2AccessTokenResponse> response = this.getResponse(request);
        OAuth2AccessTokenResponse tokenResponse = response.getBody();
        Assert.notNull(tokenResponse, "The authorization server responded to this device Code grant request with an empty body; as such, it cannot be materialized into an OAuth2AccessTokenResponse instance. Please check the HTTP response device code in your server logs for more details.");
        return tokenResponse;
    }

    private ResponseEntity<OAuth2AccessTokenResponse> getResponse(RequestEntity<?> request) {
        try {
            return this.restOperations.exchange(request, OAuth2AccessTokenResponse.class);
        } catch (RestClientException e) {
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: " + e.getMessage(), (String) null);
            throw new OAuth2AuthorizationException(oauth2Error, e);
        }
    }

    public void setRequestEntityConverter(Converter<OAuth2DeviceCodeGrantRequest, RequestEntity<?>> requestEntityConverter) {
        Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
        this.requestEntityConverter = requestEntityConverter;
    }

    public void setRestOperations(RestOperations restOperations) {
        Assert.notNull(restOperations, "restOperations cannot be null");
        this.restOperations = restOperations;
    }
}
