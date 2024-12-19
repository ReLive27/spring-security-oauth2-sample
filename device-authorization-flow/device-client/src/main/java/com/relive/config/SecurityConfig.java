package com.relive.config;

import com.relive.oauth2.client.configurers.OAuth2DeviceClientAuthenticationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;


/**
 * 安全配置类，定义应用的安全过滤器链和 HTTP 请求安全规则。
 * <p>
 * 该配置允许指定 URL 资源的访问规则，并应用自定义的设备客户端认证配置。
 *
 * @author: ReLive27
 * @date: 2024/3/5 20:20
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * 配置默认的 {@link SecurityFilterChain} 以定义 HTTP 安全规则。
     * <p>
     * - 允许对根路径("/") 和静态资源路径("/img/**") 进行无认证访问。
     * - 其他任何请求都需要进行认证。
     * - 禁用 CSRF 防护。
     * - 应用自定义的设备客户端认证配置 {@link OAuth2DeviceClientAuthenticationConfigurer}。
     *
     * @param http {@link HttpSecurity} 用于配置 HTTP 安全规则
     * @return {@link SecurityFilterChain} 实例，用于 Spring Security 过滤请求
     * @throws Exception 如果配置过程中出现错误
     */
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/", "/img/**").permitAll() // 无需认证的路径
                                .anyRequest().authenticated() // 其他所有请求需认证
                )
                .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF 防护
                .apply(new OAuth2DeviceClientAuthenticationConfigurer()); // 应用自定义设备客户端认证
        return http.build();
    }
}
