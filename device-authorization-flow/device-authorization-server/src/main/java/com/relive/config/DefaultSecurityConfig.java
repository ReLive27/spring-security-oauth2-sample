package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 默认的 Spring Security 配置类。
 * <p>
 * 该类提供了 HTTP 请求安全性过滤链的默认配置，
 * 包括用户身份认证、表单登录、OAuth2 资源服务器的 JWT 支持等。
 *
 * @author: ReLive27
 * @date: 2024/1/20 23:38
 */
@Configuration(proxyBeanMethods = false)
public class DefaultSecurityConfig {

    /**
     * 配置默认的 SecurityFilterChain，用于处理 HTTP 请求的安全配置。
     * <p>
     * - 保护所有 HTTP 请求，需要进行身份认证。<br>
     * - 提供表单登录作为默认的认证方式。<br>
     * - 启用 OAuth2 资源服务器，并使用 JWT（JSON Web Token）解析访问令牌。<br>
     * - 禁用 CSRF（跨站请求伪造）保护，以适用于无状态 REST API。<br>
     *
     * @param http {@link HttpSecurity} 提供安全配置的核心对象。
     * @return 配置完成的 {@link SecurityFilterChain} 对象。
     * @throws Exception 如果配置过程中出现错误。
     */
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 授权请求：所有请求必须经过认证
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                // 启用表单登录作为默认认证机制
                .formLogin(withDefaults())
                // 启用 OAuth2 资源服务器，并启用 JWT 支持
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                // 禁用 CSRF 保护
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * 配置一个内存用户存储服务，提供简单的用户身份认证。
     * <p>
     * 此配置为测试和开发环境提供了一个默认用户 "admin"，密码为 "password"。
     * <p>
     * 用户角色为 "USER"。
     *
     * @return 一个 {@link UserDetailsService} 实例，用于加载用户详细信息。
     */
    @Bean
    UserDetailsService users() {
        // 创建一个默认用户
        UserDetails user = User.withUsername("admin") // 用户名：admin
                .password("{noop}password") // 密码：password（{noop} 表示不加密）
                .roles("USER") // 角色：USER
                .build();

        // 使用内存存储用户详细信息
        return new InMemoryUserDetailsManager(user);
    }
}
