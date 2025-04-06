package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * OAuth2 登录安全配置类，基于 Spring Security WebFlux。
 * <p>
 * 该配置类启用了 OAuth2 登录功能，并要求所有请求都必须经过身份认证。
 * 同时关闭了 CORS（跨域资源共享）功能，可根据业务需求开启。
 * </p>
 *
 * @author ReLive
 * @date 2022/6/9 12:46 下午
 */
@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
public class OAuth2LoginSecurityConfig {

    /**
     * 配置 WebFlux 安全过滤器链。
     * <p>
     * - 所有请求都必须进行身份认证。<br>
     * - 启用 OAuth2 登录，使用默认设置。<br>
     * - 关闭 CORS 功能。
     * </p>
     *
     * @param http ServerHttpSecurity 对象，用于构建安全配置。
     * @return 配置后的 SecurityWebFilterChain 安全过滤器链。
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(authorize -> authorize
                        .anyExchange().authenticated() // 所有请求都需要认证
                )
                .oauth2Login(withDefaults()) // 启用 OAuth2 登录，使用默认配置
                .cors().disable(); // 禁用 CORS 功能
        return http.build(); // 构建安全过滤器链
    }
}
