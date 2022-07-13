package com.relive.config;

import com.relive.entity.OAuth2ClientRole;
import com.relive.entity.Role;
import com.relive.repository.OAuth2ClientRoleRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Oidc 角色映射器
 *
 * @author: ReLive
 * @date: 2022/7/12 6:31 下午
 */
public class OidcRoleMappingUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private OidcUserService oidcUserService;
    private final OAuth2ClientRoleRepository oAuth2ClientRoleRepository;

    public OidcRoleMappingUserService(OAuth2ClientRoleRepository oAuth2ClientRoleRepository) {
        Assert.notNull(oAuth2ClientRoleRepository, "oAuth2ClientRoleRepository can not be null");
        this.oAuth2ClientRoleRepository = oAuth2ClientRoleRepository;
        DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
        defaultOAuth2UserService.setRequestEntityConverter(new Converter<OAuth2UserRequest, RequestEntity<?>>() {
            @Override
            public RequestEntity<?> convert(OAuth2UserRequest userRequest) {
                //指定POST和GET请求方式token都存放在Header中
                ClientRegistration clientRegistration = userRequest.getClientRegistration();
                HttpMethod httpMethod = AuthenticationMethod.FORM.equals(clientRegistration.getProviderDetails().getUserInfoEndpoint().getAuthenticationMethod()) ? HttpMethod.POST : HttpMethod.GET;
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                URI uri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri()).build().toUri();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(userRequest.getAccessToken().getTokenValue());
                RequestEntity request = new RequestEntity(headers, httpMethod, uri);
                return request;
            }
        });
        this.oidcUserService = new OidcUserService();
        this.oidcUserService.setOauth2UserService(defaultOAuth2UserService);
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);

        OidcIdToken idToken = userRequest.getIdToken();
        List<String> role = idToken.getClaimAsStringList("role");
        Set<SimpleGrantedAuthority> mappedAuthorities = role.stream()
                .map(r -> oAuth2ClientRoleRepository.findByClientRegistrationIdAndRoleCode(userRequest.getClientRegistration().getRegistrationId(), r))
                .map(OAuth2ClientRole::getRole).map(Role::getRoleCode).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        oidcUser = new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());

        return oidcUser;
    }
}
