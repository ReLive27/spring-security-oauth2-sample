package com.relive.oauth2.client.authentication;

import com.relive.oauth2.client.endpoint.OAuth2DeviceCodeGrantRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2024/5/2 10:48
 */
public class OAuth2DeviceAuthenticationProvider implements AuthenticationProvider {

    private final OAuth2DeviceCodeAuthenticationProvider deviceCodeAuthenticationProvider;

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> userService;

    private GrantedAuthoritiesMapper authoritiesMapper = ((authorities) -> authorities);

    public OAuth2DeviceAuthenticationProvider(
            OAuth2AccessTokenResponseClient<OAuth2DeviceCodeGrantRequest> accessTokenResponseClient,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> userService) {
        Assert.notNull(userService, "userService cannot be null");
        this.deviceCodeAuthenticationProvider = new OAuth2DeviceCodeAuthenticationProvider(
                accessTokenResponseClient);
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2DeviceAuthenticationToken deviceAuthenticationToken = (OAuth2DeviceAuthenticationToken) authentication;

        OAuth2DeviceCodeAuthenticationToken deviceCodeAuthenticationToken;
        try {
            deviceCodeAuthenticationToken = (OAuth2DeviceCodeAuthenticationToken) this.deviceCodeAuthenticationProvider.authenticate(
                    new OAuth2DeviceCodeAuthenticationToken(deviceAuthenticationToken.getClientRegistration(),
                            deviceAuthenticationToken.getDeviceAuthorizationRequest()));
        } catch (OAuth2AuthorizationException ex) {
            OAuth2Error oauth2Error = ex.getError();
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
        }
        OAuth2AccessToken accessToken = deviceCodeAuthenticationToken.getAccessToken();
        Map<String, Object> additionalParameters = deviceCodeAuthenticationToken.getAdditionalParameters();
        OAuth2User oauth2User = this.userService.loadUser(new OAuth2UserRequest(
                deviceAuthenticationToken.getClientRegistration(), accessToken, additionalParameters));
        Collection<? extends GrantedAuthority> mappedAuthorities = this.authoritiesMapper
                .mapAuthorities(oauth2User.getAuthorities());
        OAuth2DeviceAuthenticationToken authenticationResult = new OAuth2DeviceAuthenticationToken(
                deviceAuthenticationToken.getClientRegistration(), deviceAuthenticationToken.getDeviceAuthorizationRequest(),
                oauth2User, mappedAuthorities, accessToken, deviceCodeAuthenticationToken.getRefreshToken());
        authenticationResult.setDetails(deviceAuthenticationToken.getDetails());
        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2DeviceAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
