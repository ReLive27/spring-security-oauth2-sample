package com.relive.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

/**
 * 转换UserInfo 端点的请求，并将token存放在Header
 *
 * @author: ReLive
 * @date: 2022/7/19 11:09 上午
 */
public class OAuth2UserSetHeaderRequestEntityConverter implements Converter<OAuth2UserRequest, RequestEntity<?>> {

    public RequestEntity<?> convert(OAuth2UserRequest userRequest) {
        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        HttpMethod httpMethod = this.getHttpMethod(clientRegistration);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        URI uri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri()).build().toUri();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userRequest.getAccessToken().getTokenValue());
        RequestEntity request = new RequestEntity(headers, httpMethod, uri);
        return request;
    }

    private HttpMethod getHttpMethod(ClientRegistration clientRegistration) {
        return AuthenticationMethod.FORM.equals(clientRegistration.getProviderDetails().getUserInfoEndpoint().getAuthenticationMethod()) ? HttpMethod.POST : HttpMethod.GET;
    }
}
