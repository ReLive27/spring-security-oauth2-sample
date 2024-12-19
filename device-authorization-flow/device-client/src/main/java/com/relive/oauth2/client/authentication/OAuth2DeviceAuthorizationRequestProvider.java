package com.relive.oauth2.client.authentication;

import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.device.DeviceAuthorizationRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.net.URI;
import java.util.Map;

@Slf4j
/**
 * 提供 OAuth2 设备授权请求的认证处理逻辑。
 * <p>
 * 该类负责验证设备授权请求，并通过发送设备授权请求来获取设备授权信息（例如 user_code、device_code、verification_uri 等）。
 * 它实现了 {@link AuthenticationProvider} 接口，并根据 OAuth 2.0 设备授权流程提供认证功能。
 *
 * @author: ReLive27
 * @date: 2024/5/1 12:02
 */
public class OAuth2DeviceAuthorizationRequestProvider implements AuthenticationProvider {

    private JsonParser jsonParser = JsonParserFactory.getJsonParser();

    /**
     * 处理设备授权请求的认证过程。
     * <p>
     * 该方法接收一个设备授权请求令牌，验证其授权类型，并通过与 OAuth 2.0 授权服务器通信获取设备授权码。
     * 如果授权类型无效或设备授权请求失败，将抛出 {@link OAuth2AuthorizationException} 异常。
     * <p>
     * 认证成功后，返回一个包含设备授权信息的 {@link OAuth2DeviceAuthorizationRequestToken} 实例。
     *
     * @param authentication 当前认证的 {@link Authentication} 实例，它应该是 {@link OAuth2DeviceAuthorizationRequestToken}
     * @return 返回一个认证后的 {@link OAuth2DeviceAuthorizationRequestToken}
     * @throws AuthenticationException 如果认证失败或授权请求无效
     */
    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2DeviceAuthorizationRequestToken deviceCodeAuthenticationToken =
                (OAuth2DeviceAuthorizationRequestToken) authentication;
        ClientRegistration clientRegistration = deviceCodeAuthenticationToken.getClientRegistration();

        // 验证授权类型是否为 DEVICE_CODE
        if (!clientRegistration.getAuthorizationGrantType().equals(AuthorizationGrantType.DEVICE_CODE)) {
            OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
            throw new OAuth2AuthorizationException(oauth2Error);
        }

        // 创建设备授权请求
        DeviceAuthorizationRequest deviceAuthorizationRequest = new DeviceAuthorizationRequest.Builder(new ClientID(clientRegistration.getClientId()))
                .scope(new Scope(Scope.parse(clientRegistration.getScopes())))
                .endpointURI(new URI(clientRegistration.getProviderDetails().getAuthorizationUri()))
                .customParameter(OAuth2ParameterNames.RESPONSE_TYPE, "device_code")
                .build();

        // 发送请求并获取响应
        HTTPResponse deviceCodeResponse = deviceAuthorizationRequest.toHTTPRequest().send();

        // 解析设备授权响应
        Map<String, Object> data = jsonParser.parseMap(deviceCodeResponse.getBody());

        // 返回设备授权请求令牌
        return new OAuth2DeviceAuthorizationRequestToken(
                (String) data.get("user_code"),
                (String) data.get("device_code"),
                (String) data.get("verification_uri"),
                (Integer) data.get("expires_in"),
                clientRegistration,
                (String) data.get("verification_uri_complete")
        );
    }

    /**
     * 判断是否支持给定的认证类 {@link Class}。
     * <p>
     * 该方法检查当前的认证类是否为 {@link OAuth2DeviceAuthorizationRequestToken}，从而确定是否支持设备授权认证。
     *
     * @param authentication 认证类 {@link Class}
     * @return 如果支持则返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2DeviceAuthorizationRequestToken.class.isAssignableFrom(authentication);
    }
}
