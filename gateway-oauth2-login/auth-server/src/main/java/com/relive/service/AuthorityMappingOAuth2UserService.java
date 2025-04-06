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
 * 权限映射服务，OAuth2 登录用户将根据映射获得相应权限，
 * 如果映射权限为空，则默认赋予最低权限 ROLE_OPERATION。
 * <p>
 * 注意：`authority` 和 `role` 是示例中的自定义权限信息字段，
 * 它们不属于 OAuth2 协议和 OpenID Connect 协议的标准部分。
 * </p>
 *
 * <p><b>使用方式：</b></p>
 * <pre>
 *     AuthorityMappingOAuth2UserService authorityMappingService =
 *         new AuthorityMappingOAuth2UserService(oAuth2ClientRoleRepository);
 *     OAuth2User user = authorityMappingService.loadUser(userRequest);
 * </pre>
 *
 * @author ReLive
 * @date 2022/7/12 6:31 下午
 * @see OAuth2UserService
 * @see DefaultOAuth2UserService
 * @see OAuth2ClientRoleRepository
 */
@RequiredArgsConstructor
public class AuthorityMappingOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    /**
     * 委托的默认 OAuth2 用户服务。
     */
    private DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    /**
     * 用于查询客户端角色的存储库。
     */
    private final OAuth2ClientRoleRepository oAuth2ClientRoleRepository;

    /**
     * 加载并映射 OAuth2 登录用户的权限信息。
     * <p>
     * 此方法将从 OAuth2 提供商返回的用户请求中提取额外的权限和角色信息，
     * 并根据这些信息映射用户的权限。如果未映射到权限，则赋予最低权限 ROLE_OPERATION。
     * </p>
     *
     * @param userRequest OAuth2 用户请求，包含 OAuth2 提供商返回的用户信息和授权信息
     * @return 映射后的 OAuth2 用户，带有相应的权限
     * @throws OAuth2AuthenticationException 如果加载用户信息失败，抛出此异常
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 使用默认的 OAuth2 用户服务加载用户信息
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) delegate.loadUser(userRequest);

        // 获取额外的参数，可能包含权限信息
        Map<String, Object> additionalParameters = userRequest.getAdditionalParameters();
        Set<String> role = new HashSet<>();
        if (additionalParameters.containsKey("authority")) {
            role.addAll((Collection<? extends String>) additionalParameters.get("authority"));
        }
        if (additionalParameters.containsKey("role")) {
            role.addAll((Collection<? extends String>) additionalParameters.get("role"));
        }

        // 根据角色信息映射权限
        Set<SimpleGrantedAuthority> mappedAuthorities = role.stream()
                .map(r -> oAuth2ClientRoleRepository.findByClientRegistrationIdAndRoleCode(
                        userRequest.getClientRegistration().getRegistrationId(), r))
                .map(OAuth2ClientRole::getRole)
                .map(Role::getRoleCode)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        // 如果没有映射到任何权限，则赋予最低权限 ROLE_OPERATION
        if (CollectionUtils.isEmpty(mappedAuthorities)) {
            mappedAuthorities = new HashSet<>(
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_OPERATION")));
        }

        // 获取用户的用户名属性，并返回带有权限的 OAuth2 用户
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(mappedAuthorities, oAuth2User.getAttributes(), userNameAttributeName);
    }
}
