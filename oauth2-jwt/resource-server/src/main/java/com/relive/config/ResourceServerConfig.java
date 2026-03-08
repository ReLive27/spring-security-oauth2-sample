package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 资源服务器安全配置类
 *
 * <p>
 * 配置 Spring Security 作为 OAuth2 资源服务器，用于：
 * <ul>
 *     <li>验证 JWT 访问令牌</li>
 *     <li>基于令牌中的权限（SCOPE）控制资源访问</li>
 * </ul>
 *
 * @author ReLive
 * @date 2021/12/25 2:05 下午
 */
@EnableWebSecurity
public class ResourceServerConfig {

    /**
     * 配置资源服务器的安全过滤器链
     *
     * <p>
     * 设置基于 JWT 的 OAuth2 资源服务器安全规则：
     * <ul>
     *     <li>"/resource/test/**" 端点需要 "message.read" 权限范围</li>
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
                .requestMatchers("/resource/test/**").hasAuthority("SCOPE_message.read"))
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }
}
