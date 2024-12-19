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
 * 该类将 {@link OAuth2DeviceCodeGrantRequest} 转换为 HTTP 请求实体，
 * 用于发送设备代码授权请求到授权服务器。
 * 它实现了 {@link Converter} 接口，将设备代码授权请求转化为符合 OAuth2 协议的 HTTP 请求。
 *
 * @author: ReLive27
 * @date: 2024/5/2 13:08
 */
public class OAuth2DeviceCodeGrantRequestEntityConverter implements Converter<OAuth2DeviceCodeGrantRequest, RequestEntity<?>> {

    /**
     * 将 {@link OAuth2DeviceCodeGrantRequest} 转换为 {@link RequestEntity}，
     * 这是一个包含 HTTP 头和参数的请求实体，用于向授权服务器发送请求。
     *
     * @param deviceCodeGrantRequest 设备代码授权请求
     * @return 转换后的 HTTP 请求实体
     */
    @Override
    public RequestEntity<?> convert(OAuth2DeviceCodeGrantRequest deviceCodeGrantRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(getDefaultTokenRequestHeaders());
        MultiValueMap<String, String> parameters = this.createParameters(deviceCodeGrantRequest);
        URI uri = UriComponentsBuilder.fromUriString(deviceCodeGrantRequest.getClientRegistration().getProviderDetails().getTokenUri()).build().toUri();
        return new RequestEntity(parameters, headers, HttpMethod.POST, uri);
    }

    /**
     * 获取默认的 OAuth2 令牌请求头。
     * 设置接收类型为 JSON，内容类型为表单 URL 编码。
     *
     * @return 默认的 HTTP 请求头
     */
    private static HttpHeaders getDefaultTokenRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        MediaType contentType = MediaType.valueOf("application/x-www-form-urlencoded;charset=UTF-8");
        headers.setContentType(contentType);
        return headers;
    }

    /**
     * 创建设备代码授权请求的参数。
     * 参数包括设备代码、授权类型、客户端 ID 等。
     *
     * @param deviceCodeGrantRequest 设备代码授权请求
     * @return HTTP 请求参数
     */
    protected MultiValueMap<String, String> createParameters(OAuth2DeviceCodeGrantRequest deviceCodeGrantRequest) {
        ClientRegistration clientRegistration = deviceCodeGrantRequest.getClientRegistration();
        OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest = deviceCodeGrantRequest.getDeviceAuthorizationRequest();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", deviceCodeGrantRequest.getGrantType().getValue());
        parameters.add("device_code", deviceAuthorizationRequest.getDeviceCode());
        if (ClientAuthenticationMethod.NONE.equals(clientRegistration.getClientAuthenticationMethod())) {
            parameters.add("client_id", clientRegistration.getClientId());
        }

        return parameters;
    }
}
