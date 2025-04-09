package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 默认的 Spring Security 配置类，用于配置基本的用户身份认证机制。
 * <p>
 * 该配置类用于提供登录认证页面和内存用户信息，适用于授权服务器登录功能。
 * 通常与 AuthorizationServer 配置类配合使用。
 * </p>
 *
 * <p>
 * 此配置主要包含三个部分：
 * <ul>
 *     <li>配置表单登录的安全过滤链</li>
 *     <li>配置一个内存用户（admin/password）</li>
 *     <li>配置密码加密器（默认支持多种加密方式）</li>
 * </ul>
 * </p>
 *
 * @author ReLive
 * @date 2022/05/22 22:30 下午
 */
@Configuration
@EnableWebSecurity
public class DefaultSecurityConfig {

    /**
     * 配置默认的安全过滤器链。
     * <p>
     * 所有请求都要求认证，启用表单登录方式。
     * </p>
     *
     * @param http HttpSecurity 对象，由 Spring 注入
     * @return SecurityFilterChain 安全过滤链
     * @throws Exception 异常信息
     */
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 所有请求都需要认证
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                // 启用表单登录功能，默认登录页面为 /login
                .formLogin(withDefaults());
        return http.build();
    }

    /**
     * 配置一个内存用户用于演示。
     * <p>
     * 用户名：admin，密码：password（使用 {noop} 明文方式），角色为 USER。
     * </p>
     *
     * @return UserDetailsService 内存用户详情服务
     */
    @Bean
    UserDetailsService users() {
        UserDetails user = User.withUsername("admin")
                .password("{noop}password") // 明文密码，不建议用于生产环境
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * 配置密码加密器。
     * <p>
     * 使用 Spring Security 提供的委托加密器，支持多种加密方案（bcrypt、noop、pbkdf2 等）。
     * 实际使用中应避免使用 {noop}。
     * </p>
     *
     * @return PasswordEncoder 密码加密器
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
