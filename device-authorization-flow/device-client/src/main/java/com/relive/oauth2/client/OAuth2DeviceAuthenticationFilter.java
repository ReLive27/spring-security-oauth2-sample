package com.relive.oauth2.client;

import com.relive.oauth2.client.authentication.OAuth2DeviceAuthenticationToken;
import com.relive.oauth2.client.endpoint.OAuth2DeviceAuthorizationRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * @author: ReLive27
 * @date: 2024/5/1 21:03
 */
public class OAuth2DeviceAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String DEFAULT_FILTER_PROCESSES_URI = "/login/oauth2/device/token";

    private ClientRegistrationRepository clientRegistrationRepository;

    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    private DeviceAuthorizationRequestRepository<OAuth2DeviceAuthorizationRequest> deviceCodeRepository = new HttpSessionOAuth2DeviceCodeRepository();

    private Converter<OAuth2DeviceAuthenticationToken, OAuth2AuthenticationToken> authenticationResultConverter = this::createAuthenticationResult;

    public OAuth2DeviceAuthenticationFilter(ClientRegistrationRepository clientRegistrationRepository,
                                            OAuth2AuthorizedClientService authorizedClientService) {
        this(clientRegistrationRepository, new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService), DEFAULT_FILTER_PROCESSES_URI);
    }

    public OAuth2DeviceAuthenticationFilter(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientRepository authorizedClientRepository, String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.notNull(authorizedClientRepository, "authorizedClientRepository cannot be null");
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        OAuth2DeviceAuthorizationRequest deviceAuthorizationRequest = this.deviceCodeRepository
                .loadAuthorizationRequest(request);
        if (deviceAuthorizationRequest == null) {
            OAuth2Error oauth2Error = new OAuth2Error("device_authorization_request_not_found");
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        String registrationId = deviceAuthorizationRequest.getRegistrationId();
        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
                    "Client Registration not found with Id: " + registrationId, null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }


        Object authenticationDetails = this.authenticationDetailsSource.buildDetails(request);
        OAuth2DeviceAuthenticationToken authenticationRequest = new OAuth2DeviceAuthenticationToken(clientRegistration, deviceAuthorizationRequest);
        authenticationRequest.setDetails(authenticationDetails);
        OAuth2DeviceAuthenticationToken authenticationResult = (OAuth2DeviceAuthenticationToken) this.getAuthenticationManager().authenticate(authenticationRequest);
        OAuth2AuthenticationToken oauth2Authentication = this.authenticationResultConverter
                .convert(authenticationResult);
        Assert.notNull(oauth2Authentication, "authentication result cannot be null");
        oauth2Authentication.setDetails(authenticationDetails);
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                authenticationResult.getClientRegistration(), oauth2Authentication.getName(),
                authenticationResult.getAccessToken(), authenticationResult.getRefreshToken());

        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient, oauth2Authentication, request, response);
        this.deviceCodeRepository.removeAuthorizationRequest(request, response);
        return oauth2Authentication;
    }

    private OAuth2AuthenticationToken createAuthenticationResult(OAuth2DeviceAuthenticationToken authenticationResult) {
        return new OAuth2AuthenticationToken(authenticationResult.getPrincipal(), authenticationResult.getAuthorities(),
                authenticationResult.getClientRegistration().getRegistrationId());
    }

    public void setClientRegistrationRepository(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    public void setAuthorizedClientRepository(OAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.authorizedClientRepository = authorizedClientRepository;
    }

    public void setDeviceCodeRepository(DeviceAuthorizationRequestRepository<OAuth2DeviceAuthorizationRequest> deviceCodeRepository) {
        this.deviceCodeRepository = deviceCodeRepository;
    }

    public void setAuthenticationResultConverter(Converter<OAuth2DeviceAuthenticationToken, OAuth2AuthenticationToken> authenticationResultConverter) {
        this.authenticationResultConverter = authenticationResultConverter;
    }
}
