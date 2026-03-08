package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * PKCE 资源服务器安全配置类
 *
 * <p>
 * 配置 Spring Security 作为 OAuth2 资源服务器，验证 JWT 访问令牌。
 *
 * <p>
 * 注意：PKCE 流程颁发的访问令牌与普通授权码流程相同，
 * 资源服务器无需特殊配置即可验证。
 *
 * @author ReLive
 * @date 2022/7/5 12:45 下午
 */
@Configuration
public class SecurityConfig {

    /**
     * 配置资源服务器安全过滤器链
     *
     * <p>
     * 设置基于 JWT 的 OAuth2 资源服务器安全规则：
     * <ul>
     *     <li>"/resource/article" 端点需要 "message.read" 权限范围</li>
     *     <li>其他端点需要认证</li>
     * </ul>
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilter(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/resource/article")
                .hasAuthority("SCOPE_message.read")
                .anyRequest().authenticated()
        )
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        return http.build();
    }
}
