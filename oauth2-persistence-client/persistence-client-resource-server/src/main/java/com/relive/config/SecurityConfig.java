package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 资源服务器安全配置类（持久化客户端示例）
 *
 * <p>
 * 配置 Spring Security 作为 OAuth2 资源服务器，
 * 用于验证 JWT 访问令牌并保护资源端点。
 *
 * @author ReLive
 * @date 2022/6/27 12:58 下午
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
     *     <li>使用 JWT 令牌验证机制</li>
     * </ul>
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/resource/article").hasAuthority("SCOPE_message.read"))
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }
}
