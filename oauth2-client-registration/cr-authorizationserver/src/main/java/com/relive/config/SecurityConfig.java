package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security 安全配置类
 *
 * 配置了 HTTP 请求的授权规则、表单登录、资源服务器 JWT 支持、
 * 以及内存中的用户身份信息。
 *
 * @author ReLive
 * @date 2022/11/29 19:05
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * 安全过滤器链配置
     *
     * - 所有请求必须认证通过
     * - 支持表单登录（默认登录页面）
     * - 启用 OAuth2 资源服务器的 JWT 支持，用于验证 Access Token
     *
     * @param http HttpSecurity 配置对象
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 所有请求都需要认证
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                // 启用 JWT 方式的资源服务器支持
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                // 启用默认表单登录
                .formLogin(withDefaults());

        return http.build();
    }

    /**
     * 配置内存中的用户信息
     *
     * 创建一个用户名为 admin、密码为 password、角色为 ADMIN 的用户。
     *
     * @return 用户详情服务实例
     */
    @Bean
    UserDetailsService users() {
        UserDetails user = User.withUsername("admin")
                .password("{noop}password") // {noop} 表示不加密，仅用于开发测试
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * 密码编码器配置
     *
     * 使用 Spring Security 提供的 DelegatingPasswordEncoder，支持多种编码方式。
     *
     * @return 密码编码器实例
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
