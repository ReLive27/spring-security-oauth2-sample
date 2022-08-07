package com.relive.config;

import com.relive.entity.Permission;
import com.relive.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.stream.Collectors;

/**
 * 自定义访问令牌
 * <p>
 * 本示例以RBAC0权限模型，根据获取安全上下文中角色信息查询该角色对应权限，将权限添加到访问令牌，替换scope中原有值。
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
                    claim.put("scope", roleRepository.findByRoleCode(context.getPrincipal().getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority).findFirst().orElse("ROLE_OPERATION"))
                            .getPermissions().stream().map(Permission::getPermissionCode).collect(Collectors.toSet()));
                });
            }
        };
    }
}
