package com.relive.event;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationErrorResponse;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationResponse;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformationResponse;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientRegistrationRequest;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientRegistrationResponseParser;
import com.relive.config.JdbcClientRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * 应用启动完成后自动执行客户端注册。
 * <p>
 * 本类监听 Spring Boot 的 ApplicationReadyEvent 事件，在应用启动完成后，
 * 向认证服务器发送 OIDC 客户端注册请求，并将注册成功的客户端信息保存到数据库。
 * </p>
 *
 * ⚙ 依赖项：
 * - 使用 Spring Boot `OAuth2ClientProperties` 获取配置的客户端凭证和授权服务器信息；
 * - 使用 `JdbcClientRegistrationRepository` 存储客户端注册信息；
 * - 使用 Nimbus OAuth 2.0 SDK 发送注册请求。
 *
 * 🧠 注意事项：
 * - 当前注册端点写死为 `http://localhost:8080/connect/register`，请根据实际环境修改。
 * - 注册请求中包含自定义字段 `require-authorization-consent` 和 `require-proof-key`。
 *
 * @author: ReLive27
 * @date: 2024/6/2 21:00
 */
@Component
@RequiredArgsConstructor
public class OAuth2ClientRegistrationEvent implements ApplicationListener<ApplicationReadyEvent> {

    // JDBC 客户端注册信息持久化仓库
    private final JdbcClientRegistrationRepository registrationRepository;

    // 客户端和授权服务器的配置属性
    private final OAuth2ClientProperties properties;

    /**
     * 监听应用启动事件，在应用启动完成后自动执行客户端注册。
     */
    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        URI clientsEndpoint = new URI("http://localhost:8080/connect/register"); // 客户端注册端点

        // 构建客户端元数据（如名称、重定向地址、授权类型、scope 等）
        OIDCClientMetadata clientMetadata = new OIDCClientMetadata();
        clientMetadata.setGrantTypes(Collections.singleton(GrantType.AUTHORIZATION_CODE));
        clientMetadata.setRedirectionURI(URI.create("http://127.0.0.1:8070/oauth2/callback/messaging-client-authorization-code"));
        clientMetadata.setName("Test Client");
        clientMetadata.setScope(new Scope("message.read"));

        // 自定义字段，根据服务端是否支持进行设置
        clientMetadata.setCustomField("require-authorization-consent", false);
        clientMetadata.setCustomField("require-proof-key", false);

        // 构建注册请求并携带访问令牌
        OIDCClientRegistrationRequest regRequest = new OIDCClientRegistrationRequest(
                clientsEndpoint,
                clientMetadata,
                this.getToken() // 使用 Client Credentials 模式获取 token
        );

        // 发送注册请求
        HTTPResponse httpResponse = regRequest.toHTTPRequest().send();

        // 解析注册响应
        ClientRegistrationResponse regResponse = OIDCClientRegistrationResponseParser.parse(httpResponse);

        if (!regResponse.indicatesSuccess()) {
            // 注册失败，抛出异常
            ClientRegistrationErrorResponse errorResponse = (ClientRegistrationErrorResponse) regResponse;
            throw new IllegalStateException(errorResponse.getErrorObject().toString());
        }

        // 注册成功，将注册信息保存到数据库
        OIDCClientInformationResponse successResponse = (OIDCClientInformationResponse) regResponse;
        this.registrationRepository.save(new OAuth2ClientRegistrationMapper(successResponse).asClientRegistration());
    }

    /**
     * 使用 Client Credentials 模式向授权服务器申请访问令牌。
     * 用于进行客户端注册时身份验证。
     */
    private BearerAccessToken getToken() throws URISyntaxException, IOException, ParseException {
        AuthorizationGrant clientGrant = new ClientCredentialsGrant(); // 客户端授权模式

        // 获取客户端配置信息
        OAuth2ClientProperties.Registration registration = properties.getRegistration().get("client-registration");
        ClientID clientID = new ClientID(registration.getClientId());
        Secret clientSecret = new Secret(registration.getClientSecret());
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        // 设置作用域
        Scope scope = new Scope(registration.getScope().toArray(new String[0]));

        // 获取授权服务器的 token 端点地址
        OAuth2ClientProperties.Provider provider = properties.getProvider().get("client-registartion-provider");
        URI tokenEndpoint = new URI(provider.getTokenUri());

        // 构建 Token 请求
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, clientGrant, scope);

        // 发送 Token 请求
        TokenResponse response = TokenResponse.parse(request.toHTTPRequest().send());

        if (!response.indicatesSuccess()) {
            // 获取 token 失败
            TokenErrorResponse errorResponse = response.toErrorResponse();
            throw new IllegalStateException(errorResponse.getErrorObject().toString());
        }

        // 成功返回访问令牌
        AccessTokenResponse successResponse = response.toSuccessResponse();
        AccessToken accessToken = successResponse.getTokens().getAccessToken();
        return new BearerAccessToken(accessToken.getValue());
    }
}
