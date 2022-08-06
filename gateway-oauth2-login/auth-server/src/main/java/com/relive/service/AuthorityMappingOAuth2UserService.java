package com.relive.service;

import com.relive.entity.OAuth2ClientRole;
import com.relive.entity.Role;
import com.relive.repository.OAuth2ClientRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限映射服务，OAuth2登录用户将被赋予对应权限，映射权限为空将赋予最下权限ROLE_OPERATION
 * <p>
 * 注意：`authority`和`role`为本示例中自定义权限信息字段，非OAuth2协议及OpenID Connect协议中规范
 * </p>
 *
 * @author: ReLive
 * @date: 2022/7/12 6:31 下午
 */
@RequiredArgsConstructor
public class AuthorityMappingOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final OAuth2ClientRoleRepository oAuth2ClientRoleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) delegate.loadUser(userRequest);

        Map<String, Object> additionalParameters = userRequest.getAdditionalParameters();
        Set<String> role = new HashSet<>();
        if (additionalParameters.containsKey("authority")) {
            role.addAll((Collection<? extends String>) additionalParameters.get("authority"));
        }
        if (additionalParameters.containsKey("role")) {
            role.addAll((Collection<? extends String>) additionalParameters.get("role"));
        }
        Set<SimpleGrantedAuthority> mappedAuthorities = role.stream()
                .map(r -> oAuth2ClientRoleRepository.findByClientRegistrationIdAndRoleCode(userRequest.getClientRegistration().getRegistrationId(), r))
                .map(OAuth2ClientRole::getRole).map(Role::getRoleCode).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        //当没有指定客户端角色，则默认赋予最小权限ROLE_OPERATION
        if (CollectionUtils.isEmpty(mappedAuthorities)) {
            mappedAuthorities = new HashSet<>(
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_OPERATION")));
        }
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        return new DefaultOAuth2User(mappedAuthorities, oAuth2User.getAttributes(), userNameAttributeName);
    }
}
