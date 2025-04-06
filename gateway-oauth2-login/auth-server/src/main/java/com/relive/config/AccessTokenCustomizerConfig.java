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
 * 自定义访问令牌配置类
 * <p>
 * 本示例使用 RBAC0 权限模型，在安全上下文中根据用户所拥有的角色信息查询对应权限，
 * 然后将这些权限信息添加到访问令牌（access_token）的自定义声明中，并替换原有的 scope 值。
 * </p>
 *
 * 示例效果：JWT 中将包含名为 "authorities" 的权限字段，值为该用户角色下的权限列表。
 *
 * @author ReLive
 * @date 2022/8/7 20:22
 */
@Configuration(proxyBeanMethods = false)
public class AccessTokenCustomizerConfig {

    /**
     * 注入角色数据访问接口，用于根据角色编码查询权限信息。
     */
    @Autowired
    RoleRepository roleRepository;

    /**
     * 自定义令牌增强器，将权限信息写入 JWT 令牌中。
     *
     * @return OAuth2TokenCustomizer 实例，用于定制化访问令牌。
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return (context) -> {
            // 判断当前处理的是访问令牌（access_token）
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
