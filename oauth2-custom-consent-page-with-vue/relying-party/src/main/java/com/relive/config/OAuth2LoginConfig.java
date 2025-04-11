package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * OAuth2 登录配置类
 *
 * 主要用于演示结合表单登录和 OAuth2 登录（如 GitHub、Gitee 第三方登录）的安全配置。
 * 默认启用的是 Spring Security 提供的默认登录页面。
 *
 * @author ReLive
 * @date 2023/3/18 18:43
 */
@Configuration(proxyBeanMethods = false)
public class OAuth2LoginConfig {

    /**
     * 安全过滤链配置
     *
     * - 所有请求都需要认证
     * - 启用表单登录，并设置默认登录成功跳转地址为 /home
     * - 启用 OAuth2 登录（使用默认配置）
     * - 禁用 CSRF（适用于前后端分离系统或测试场景）
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin(from -> {
                    from.defaultSuccessUrl("/home"); // 登录成功后跳转的默认地址
                })
                .oauth2Login(Customizer.withDefaults()) // 启用默认的 OAuth2 登录流程
                .csrf().disable(); // 禁用 CSRF
        return http.build();
    }

    /**
     * 创建一个内存中的用户（用于表单登录测试）
     *
     * 用户名：admin
     * 密码：password
     * 角色：USER
     */
    @Bean
    UserDetailsService users() {
        UserDetails user = User.withUsername("admin")
                .password("{noop}password") // 明文密码（仅用于演示）
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * 配置密码加密器
     * 使用 Spring Security 默认推荐的 DelegatingPasswordEncoder
     * 支持多种加密方式（如 bcrypt、noop 等）
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
