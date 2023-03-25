package com.relive.config;

import com.relive.entity.Permission;
import com.relive.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.stream.Collectors;

/**
 * Custom Access Token
 * <p>
 * This example uses the RBAC0 permission model to query the corresponding permissions of the role based on the obtained role information in the security context, add the permission to the access token, and replace the original value in the scope.
 * </p>
 *
 * @author: ReLive
 * @date: 2022/8/7 20:22
 */
@Configuration(proxyBeanMethods = false)
public class AccessTokenCustomizerConfig {

    @Autowired
    RoleRepository roleRepository;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims().claims(claim -> {
                    claim.put("authorities", roleRepository.findByRoleCode(context.getPrincipal().getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority).findFirst().orElse("ROLE_OPERATION"))
                            .getPermissions().stream().map(Permission::getPermissionCode).collect(Collectors.toSet()));
                });
            }
        };
    }
}
