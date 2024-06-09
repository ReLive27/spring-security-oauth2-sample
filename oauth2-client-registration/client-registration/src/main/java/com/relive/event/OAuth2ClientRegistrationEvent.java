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
 * 自动注册客户端事件
 *
 * @author: ReLive27
 * @date: 2024/6/2 21:00
 */
@Component
@RequiredArgsConstructor
public class OAuth2ClientRegistrationEvent implements ApplicationListener<ApplicationReadyEvent> {

    private final JdbcClientRegistrationRepository registrationRepository;

    private final OAuth2ClientProperties properties;

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        URI clientsEndpoint = new URI("http://localhost:8080/connect/register");

        // We want to register a client for the code grant
        OIDCClientMetadata clientMetadata = new OIDCClientMetadata();
        clientMetadata.setGrantTypes(Collections.singleton(GrantType.AUTHORIZATION_CODE));
        clientMetadata.setRedirectionURI(URI.create("http://127.0.0.1:8070/oauth2/callback/messaging-client-authorization-code"));
        clientMetadata.setName("Test Client");
        clientMetadata.setScope(new Scope("message.read"));
        clientMetadata.setCustomField("require-authorization-consent", false);
        clientMetadata.setCustomField("require-proof-key", false);

        OIDCClientRegistrationRequest regRequest = new OIDCClientRegistrationRequest(
                clientsEndpoint,
                clientMetadata,
                this.getToken()
        );

        HTTPResponse httpResponse = regRequest.toHTTPRequest().send();

        ClientRegistrationResponse regResponse = OIDCClientRegistrationResponseParser.parse(httpResponse);

        if (!regResponse.indicatesSuccess()) {
            ClientRegistrationErrorResponse errorResponse = (ClientRegistrationErrorResponse) regResponse;
            throw new IllegalStateException(errorResponse.getErrorObject().toString());
        }

        OIDCClientInformationResponse successResponse = (OIDCClientInformationResponse) regResponse;
        this.registrationRepository.save(new OAuth2ClientRegistrationMapper(successResponse).asClientRegistration());

    }

    private BearerAccessToken getToken() throws URISyntaxException, IOException, ParseException {
        AuthorizationGrant clientGrant = new ClientCredentialsGrant();
        OAuth2ClientProperties.Registration registration = properties.getRegistration().get("client-registration");
        ClientID clientID = new ClientID(registration.getClientId());
        Secret clientSecret = new Secret(registration.getClientSecret());
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        Scope scope = new Scope(registration.getScope().toArray(new String[0]));

        OAuth2ClientProperties.Provider provider = properties.getProvider().get("client-registartion-provider");
        URI tokenEndpoint = new URI(provider.getTokenUri());

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, clientGrant, scope);

        TokenResponse response = TokenResponse.parse(request.toHTTPRequest().send());

        if (!response.indicatesSuccess()) {
            TokenErrorResponse errorResponse = response.toErrorResponse();
            throw new IllegalStateException(errorResponse.getErrorObject().toString());
        }

        AccessTokenResponse successResponse = response.toSuccessResponse();

        AccessToken accessToken = successResponse.getTokens().getAccessToken();
        return new BearerAccessToken(accessToken.getValue());
    }
}
