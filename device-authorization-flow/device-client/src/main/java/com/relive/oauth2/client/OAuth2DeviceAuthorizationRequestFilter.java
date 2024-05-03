package com.relive.oauth2.client;

import com.relive.oauth2.client.authentication.OAuth2DeviceAuthorizationRequestToken;
import com.relive.oauth2.client.endpoint.OAuth2DeviceAuthorizationRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * @author: ReLive27
 * @date: 2024/4/30 21:47
 */
@Slf4j
public class OAuth2DeviceAuthorizationRequestFilter extends OncePerRequestFilter {
    private static final String REGISTRATION_ID_URI_VARIABLE_NAME = "registrationId";

    private static final String CLIENT_REGISTRATION_NOT_FOUND_ERROR_CODE = "client_registration_not_found";

    public static final String DEFAULT_FILTER_PROCESSES_URI = "/oauth2/device/authorization";

    private DeviceAuthorizationRequestRepository<OAuth2DeviceAuthorizationRequest> deviceCodeRepository = new HttpSessionOAuth2DeviceCodeRepository();

    private final AuthenticationManager authenticationManager;

    private ClientRegistrationRepository clientRegistrationRepository;

    private final AntPathRequestMatcher authorizationRequestMatcher;

    private AuthenticationFailureHandler authenticationFailureHandler = this::unsuccessfulRedirectForAuthorization;


    public OAuth2DeviceAuthorizationRequestFilter(AuthenticationManager authenticationManager,
                                                  ClientRegistrationRepository clientRegistrationRepository) {
        this(authenticationManager, clientRegistrationRepository, DEFAULT_FILTER_PROCESSES_URI);
    }

    public OAuth2DeviceAuthorizationRequestFilter(AuthenticationManager authenticationManager, ClientRegistrationRepository clientRegistrationRepository,
                                                  String authorizationRequestBaseUri) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.hasText(authorizationRequestBaseUri, "authorizationRequestBaseUri cannot be empty");
        this.authenticationManager = authenticationManager;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizationRequestMatcher = new AntPathRequestMatcher(
                authorizationRequestBaseUri + "/{" + REGISTRATION_ID_URI_VARIABLE_NAME + "}");
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!this.authorizationRequestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String registrationId = this.resolveRegistrationId(request);
            if (registrationId == null) {
                OAuth2Error oauth2Error = new OAuth2Error(CLIENT_REGISTRATION_NOT_FOUND_ERROR_CODE,
                        "Client Registration not found with Id: " + registrationId, null);
                throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
            }

            ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(
                    registrationId);
            if (clientRegistration == null) {
                OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT);
                throw new OAuth2AuthorizationException(oauth2Error);
            }

            if (log.isTraceEnabled()) {
                log.trace("Retrieved registered client");
            }
            OAuth2DeviceAuthorizationRequestToken authenticationToken = new OAuth2DeviceAuthorizationRequestToken(clientRegistration);
            Authentication authenticationResult = this.authenticationManager.authenticate(authenticationToken);

            OAuth2DeviceAuthorizationRequest authorizationRequest = new OAuth2DeviceAuthorizationRequest();
            this.createOAuth2AuthorizationRequest(authorizationRequest, (OAuth2DeviceAuthorizationRequestToken) authenticationResult);
            this.deviceCodeRepository.saveAuthorizationRequest(authorizationRequest, request, response);

            sendDeviceVerification(request, response, (OAuth2DeviceAuthorizationRequestToken) authenticationResult);

        } catch (OAuth2AuthenticationException ex) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("Authorization request failed: %s", ex.getError()), ex);
            }
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
        }
    }

    private void createOAuth2AuthorizationRequest(OAuth2DeviceAuthorizationRequest authorizationRequest, OAuth2DeviceAuthorizationRequestToken authenticationToken) {
        authorizationRequest.setRegistrationId(authenticationToken.getClientRegistration().getRegistrationId());
        authorizationRequest.setDeviceCode(authenticationToken.getDeviceCode());
    }

    private void sendDeviceVerification(HttpServletRequest request, HttpServletResponse response,
                                        OAuth2DeviceAuthorizationRequestToken authorizationConsentAuthentication) throws IOException {

        String userCode = authorizationConsentAuthentication.getUserCode();
        String verificationUriComplete = authorizationConsentAuthentication.getVerificationUriComplete();
        String verificationUri = authorizationConsentAuthentication.getVerificationUri();

        //TODO 重定向到自定义设备激活页面
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Displaying generated consent screen");
        }

        DefaultDeviceActivatePage.displayDeviceActivate(request, response, userCode, verificationUriComplete, verificationUri, Collections.emptyMap());

    }

    private void unsuccessfulRedirectForAuthorization(HttpServletRequest request, HttpServletResponse response,
                                                      AuthenticationException ex) throws IOException {
        Throwable cause = ex.getCause();
        LogMessage message = LogMessage.format("Authorization Request failed: %s", cause);
        this.logger.error(message, ex);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    private String resolveRegistrationId(HttpServletRequest request) {
        if (this.authorizationRequestMatcher.matches(request)) {
            return this.authorizationRequestMatcher.matcher(request)
                    .getVariables()
                    .get(REGISTRATION_ID_URI_VARIABLE_NAME);
        }
        return null;
    }


    public void setDeviceCodeRepository(DeviceAuthorizationRequestRepository<OAuth2DeviceAuthorizationRequest> deviceCodeRepository) {
        this.deviceCodeRepository = deviceCodeRepository;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationFailureHandler = authenticationFailureHandler;
    }
}
