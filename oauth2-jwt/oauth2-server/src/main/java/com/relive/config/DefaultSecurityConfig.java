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
 * 默认安全配置类
 *
 * <p>
 * 配置 Spring Security 的基本安全设置，包括：
 * <ul>
 *     <li>HTTP 安全过滤器链配置</li>
 *     <li>用户详情服务（内存中存储）</li>
 *     <li>密码编码器</li>
 * </ul>
 *
 * <p>
 * 该配置主要用于授权服务器的用户认证（登录页面）。
 *
 * @author ReLive
 * @date 2021/12/13 12:20 下午
 */
@EnableWebSecurity
@Configuration
public class DefaultSecurityConfig {


    /**
     * 配置默认的安全过滤器链
     *
     * <p>
     * 设置所有请求都需要认证，并启用表单登录。
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                .formLogin(withDefaults());
        return http.build();
    }

    /**
     * 配置用户详情服务（内存存储）
     *
     * <p>
     * 创建一个示例用户用于登录认证。
     *
     * @return UserDetailsService 用户详情服务
     */
    @Bean
    UserDetailsService users() {
        UserDetails user = User.withUsername("admin")
                .password("{noop}password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * 配置密码编码器
     *
     * <p>
     * 使用 Spring Security 的委托密码编码器，支持多种编码格式（如 {noop}、{bcrypt} 等）。
     *
     * @return PasswordEncoder 密码编码器
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


}
