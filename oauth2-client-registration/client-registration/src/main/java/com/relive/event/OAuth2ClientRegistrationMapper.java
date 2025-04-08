package com.relive.event;

import com.nimbusds.oauth2.sdk.client.ClientMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformationResponse;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OIDC 客户端注册响应信息映射工具类，
 * 用于将 OIDCClientInformationResponse 转换为 Spring Security 所需的 ClientRegistration 实例。
 *
 * 此转换便于将自动注册的客户端信息持久化到本地数据库或内存中，以供 Spring Security OAuth2 客户端使用。
 *
 * @author: ReLive27
 * @date: 2024/6/4 23:13
 */
public class OAuth2ClientRegistrationMapper {

    /**
     * 客户端注册响应信息（包含 client_id、client_secret、元数据等）
     */
    private final OIDCClientInformationResponse informationResponse;

    /**
     * 构造函数，接收一个客户端注册响应
     *
     * @param informationResponse OIDC 客户端注册响应
     */
    public OAuth2ClientRegistrationMapper(OIDCClientInformationResponse informationResponse) {
        this.informationResponse = informationResponse;
    }

    /**
     * 将 OIDCClientInformationResponse 映射为 Spring Security 的 ClientRegistration 对象。
     *
     * @return 转换后的 ClientRegistration 实例
     */
    public ClientRegistration asClientRegistration() {
        // 设置注册ID（registrationId），在 OAuth2 客户端中用于唯一标识一个客户端
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId("messaging-client-authorization-code");

        // 获取注册响应中的关键信息
        OIDCClientInformation oidcClientInformation = informationResponse.getOIDCClientInformation();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();

        // 映射 client_id 和 client_secret
        map.from(oidcClientInformation.getID().getValue()).to(builder::clientId);
        map.from(oidcClientInformation.getSecret().getValue()).to(builder::clientSecret);

        // 提取注册端点的基础地址，拼接生成授权、令牌、JWK URI
        UriComponents baseUriComponents = UriComponentsBuilder.fromUri(oidcClientInformation.getRegistrationURI())
                .replacePath(null).replaceQuery(null).build();
        String baseUri = baseUriComponents.toUriString();
        map.from(baseUri + "/oauth2/authorize").to(builder::authorizationUri);
        map.from(baseUri + "/oauth2/token").to(builder::tokenUri);
        map.from(baseUri + "/oauth2/jwks").to(builder::jwkSetUri);

        // 映射元数据中的其他信息
        ClientMetadata metadata = oidcClientInformation.getMetadata();
        map.from(new ClientAuthenticationMethod(metadata.getTokenEndpointAuthMethod().getValue())).to(builder::clientAuthenticationMethod);
        Assert.notEmpty(metadata.getGrantTypes(), "grantTypes cannot be empty");
        map.from(new AuthorizationGrantType(metadata.getGrantTypes().iterator().next().getValue())).to(builder::authorizationGrantType);
        Assert.notEmpty(metadata.getRedirectionURIs(), "redirectURIs cannot be empty");
        map.from(metadata.getRedirectionURIs().iterator().next().toString()).to(builder::redirectUri);

        // 映射 scope 和客户端名称
        map.from(metadata.getScope().toStringList()).to(builder::scope);
        map.from(metadata.getName()).to(builder::clientName);

        return builder.build();
    }
}
