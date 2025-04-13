package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security 安全配置类，配置表单登录及内存用户信息。
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * 配置默认的安全过滤链，启用表单登录。
     */
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated() // 所有请求都需认证
                )
                .formLogin(withDefaults()); // 使用默认表单登录页面
        return http.build();
    }

    /**
     * 配置内存用户信息，用户名为 admin，密码为 password。
     */
    @Bean
    UserDetailsService users() {
        UserDetails user = User.withUsername("admin")
                .password("{noop}password") // 明文密码，开发测试用
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * 配置密码编码器，支持多种编码格式。
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
