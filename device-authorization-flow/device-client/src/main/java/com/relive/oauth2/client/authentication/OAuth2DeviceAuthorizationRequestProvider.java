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

/**
 * @author: ReLive27
 * @date: 2024/5/1 12:02
 */
@Slf4j
public class OAuth2DeviceAuthorizationRequestProvider implements AuthenticationProvider {

    private JsonParser jsonParser = JsonParserFactory.getJsonParser();

    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2DeviceAuthorizationRequestToken deviceCodeAuthenticationToken =
                (OAuth2DeviceAuthorizationRequestToken) authentication;
        ClientRegistration clientRegistration = deviceCodeAuthenticationToken.getClientRegistration();

        if (!clientRegistration.getAuthorizationGrantType().equals(AuthorizationGrantType.DEVICE_CODE)) {
            OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
            throw new OAuth2AuthorizationException(oauth2Error);
        }

        DeviceAuthorizationRequest deviceAuthorizationRequest = new DeviceAuthorizationRequest.Builder(new ClientID(clientRegistration.getClientId()))
                .scope(new Scope(Scope.parse(clientRegistration.getScopes())))
                .endpointURI(new URI(clientRegistration.getProviderDetails().getAuthorizationUri()))
                .customParameter(OAuth2ParameterNames.RESPONSE_TYPE, "device_code")
                .build();
        HTTPResponse deviceCodeResponse = deviceAuthorizationRequest.toHTTPRequest().send();

        Map<String, Object> data = jsonParser.parseMap(deviceCodeResponse.getBody());

        return new OAuth2DeviceAuthorizationRequestToken((String) data.get("user_code"), (String) data.get("device_code"),
                (String) data.get("verification_uri"), (Integer) data.get("expires_in"), clientRegistration, (String) data.get("verification_uri_complete"));

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2DeviceAuthorizationRequestToken.class.isAssignableFrom(authentication);
    }
}
