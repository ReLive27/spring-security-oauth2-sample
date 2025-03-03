package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 配置 Spring Security 资源服务器的安全策略。
 *
 * @author: ReLive
 * @date: 2022/7/2 11:01 下午
 */
@Configuration
public class SecurityConfig {

    /**
     * 配置 OAuth2 资源服务器的安全过滤器链。
     * 该方法指定访问 /resource/article 需要 "SCOPE_message.read" 权限，
     * 其他请求需要身份验证，并启用 JWT 作为 OAuth2 资源服务器的令牌解析机制。
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 可能的异常
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/resource/article").hasAuthority("SCOPE_message.read")
                .anyRequest().authenticated())
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        return http.build();
    }
}
