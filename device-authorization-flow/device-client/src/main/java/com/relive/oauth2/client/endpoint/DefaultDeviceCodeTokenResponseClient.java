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
 * 默认的设备代码令牌响应客户端实现类，负责从授权服务器获取设备代码授权响应（包含 Access Token 和 Refresh Token）。
 * 该类会将设备代码授权请求发送到授权服务器，并从中提取 OAuth2 访问令牌响应。
 *
 * @author: ReLive27
 * @date: 2024/5/2 13:20
 */
public class DefaultDeviceCodeTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2DeviceCodeGrantRequest> {

    /**
     * 错误码：无效的令牌响应错误。
     */
    private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

    /**
     * 将设备代码授权请求转换为 HTTP 请求实体的转换器。
     */
    private Converter<OAuth2DeviceCodeGrantRequest, RequestEntity<?>> requestEntityConverter = new OAuth2DeviceCodeGrantRequestEntityConverter();

    /**
     * 用于执行 HTTP 请求的 RestOperations 实例，默认使用 RestTemplate。
     */
    private RestOperations restOperations;

    /**
     * 创建一个默认的设备代码令牌响应客户端实例，使用默认的 {@link RestTemplate} 配置。
     */
    public DefaultDeviceCodeTokenResponseClient() {
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        this.restOperations = restTemplate;
    }

    /**
     * 从授权服务器获取设备代码授权请求的 OAuth2 访问令牌响应。
     *
     * @param deviceCodeGrantRequest 设备代码授权请求，包含客户端信息和设备代码等信息
     * @return {@link OAuth2AccessTokenResponse} 包含访问令牌和其他响应数据
     * @throws OAuth2AuthorizationException 如果请求失败，将抛出此异常
     */
    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2DeviceCodeGrantRequest deviceCodeGrantRequest) {
        Assert.notNull(deviceCodeGrantRequest, "deviceCodeGrantRequest cannot be null");

        // 将设备代码授权请求转换为 HTTP 请求
        RequestEntity<?> request = this.requestEntityConverter.convert(deviceCodeGrantRequest);

        // 发送请求并获取响应
        ResponseEntity<OAuth2AccessTokenResponse> response = this.getResponse(request);

        // 获取响应体并进行空值校验
        OAuth2AccessTokenResponse tokenResponse = response.getBody();
        Assert.notNull(tokenResponse, "The authorization server responded to this device Code grant request with an empty body; as such, it cannot be materialized into an OAuth2AccessTokenResponse instance. Please check the HTTP response device code in your server logs for more details.");

        return tokenResponse;
    }

    /**
     * 发送 HTTP 请求并获取响应。
     *
     * @param request {@link RequestEntity} 请求实体
     * @return {@link ResponseEntity} OAuth2 访问令牌响应
     * @throws OAuth2AuthorizationException 如果请求失败，将抛出此异常
     */
    private ResponseEntity<OAuth2AccessTokenResponse> getResponse(RequestEntity<?> request) {
        try {
            // 执行请求并返回响应
            return this.restOperations.exchange(request, OAuth2AccessTokenResponse.class);
        } catch (RestClientException e) {
            // 捕获请求失败并抛出 OAuth2 授权异常
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: " + e.getMessage(), (String) null);
            throw new OAuth2AuthorizationException(oauth2Error, e);
        }
    }

    /**
     * 设置用于将设备代码授权请求转换为 HTTP 请求实体的转换器。
     *
     * @param requestEntityConverter 请求实体转换器
     */
    public void setRequestEntityConverter(Converter<OAuth2DeviceCodeGrantRequest, RequestEntity<?>> requestEntityConverter) {
        Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
        this.requestEntityConverter = requestEntityConverter;
    }

    /**
     * 设置用于执行 HTTP 请求的 {@link RestOperations} 实例。
     *
     * @param restOperations {@link RestOperations} 实例
     */
    public void setRestOperations(RestOperations restOperations) {
        Assert.notNull(restOperations, "restOperations cannot be null");
        this.restOperations = restOperations;
    }
}
