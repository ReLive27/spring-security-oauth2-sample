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
 * @author: ReLive27
 * @date: 2024/6/4 23:13
 */
public class OAuth2ClientRegistrationMapper {

    private final OIDCClientInformationResponse informationResponse;

    public OAuth2ClientRegistrationMapper(OIDCClientInformationResponse informationResponse) {
        this.informationResponse = informationResponse;
    }

    public ClientRegistration asClientRegistration() {
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId("messaging-client-authorization-code");
        OIDCClientInformation oidcClientInformation = informationResponse.getOIDCClientInformation();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(oidcClientInformation.getID().getValue()).to(builder::clientId);
        map.from(oidcClientInformation.getSecret().getValue()).to(builder::clientSecret);
        UriComponents baseUriComponents = UriComponentsBuilder.fromUri(oidcClientInformation.getRegistrationURI())
                .replacePath(null).replaceQuery(null).build();
        String baseUri = baseUriComponents.toUriString();
        map.from(baseUri + "/oauth2/authorize").to(builder::authorizationUri);
        map.from(baseUri + "/oauth2/token").to(builder::tokenUri);
        map.from(baseUri + "/oauth2/jwks").to(builder::jwkSetUri);

        ClientMetadata metadata = oidcClientInformation.getMetadata();
        map.from(new ClientAuthenticationMethod(metadata.getTokenEndpointAuthMethod().getValue())).to(builder::clientAuthenticationMethod);
        Assert.notEmpty(metadata.getGrantTypes(), "grantTypes cannot be empty");
        map.from(new AuthorizationGrantType(metadata.getGrantTypes().iterator().next().getValue())).to(builder::authorizationGrantType);
        Assert.notEmpty(metadata.getRedirectionURIs(), "redirectURIs cannot be empty");
        map.from(metadata.getRedirectionURIs().iterator().next().toString()).to(builder::redirectUri);
        map.from(metadata.getScope().toStringList()).to(builder::scope);
        map.from(metadata.getName()).to(builder::clientName);
        return builder.build();
    }

}
