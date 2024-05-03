package com.relive.oauth2.client.endpoint;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

/**
 * @author: ReLive27
 * @date: 2024/5/2 13:08
 */
public class OAuth2DeviceCodeGrantRequestEntityConverter implements Converter<OAuth2DeviceCodeGrantRequest, RequestEntity<?>> {

    @Override
    public RequestEntity<?> convert(OAuth2DeviceCodeGrantRequest deviceCodeGrantRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(getDefaultTokenRequestHeaders());
        MultiValueMap<String, String> parameters = this.createParameters(deviceCodeGrantRequest);
        URI uri = UriComponentsBuilder.fromUriString(deviceCodeGrantRequest.getClientRegistration().getProviderDetails().getTokenUri()).build().toUri();
        return new RequestEntity(parameters, headers, HttpMethod.POST, uri);
    }

    private static HttpHeaders getDefaultTokenRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        MediaType contentType = MediaType.valueOf("application/x-www-form-urlencoded;charset=UTF-8");
        headers.setContentType(contentType);
        return headers;
    }

    protected MultiValueMap<String, String> createParameters(OAuth2DeviceCodeGrantRequest deviceCodeGrantRequest) {
        ClientRegistration clientRegistration = deviceCodeGrantRequest.getClientRegistration();
        OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest = deviceCodeGrantRequest.getDeviceAuthorizationRequest();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap();
        parameters.add("grant_type", deviceCodeGrantRequest.getGrantType().getValue());
        parameters.add("device_code", deviceAuthorizationRequest.getDeviceCode());
        if (ClientAuthenticationMethod.NONE.equals(clientRegistration.getClientAuthenticationMethod())) {
            parameters.add("client_id", clientRegistration.getClientId());
        }

        return parameters;
    }


}
