package com.relive.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 配置 Spring Security 相关的安全设置。
 * 该类配置了 HTTP 请求的授权规则以及 OAuth2 introspective 资源服务器授权配置。
 *
 * @author: ReLive
 * @date: 2022/11/16 21:32
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * 配置 SecurityFilterChain，定义 HTTP 请求的安全策略。
     * 该方法配置了请求的访问权限，使用 OAuth2 introspective 资源服务器认证。
     *
     * @param http HttpSecurity 配置对象
     * @return 返回配置后的 SecurityFilterChain 实例
     * @throws Exception 可能抛出的异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 配置 HTTP 请求的授权规则
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // 配置特定资源路径需要指定的权限
                        .requestMatchers("/resource/article").hasAuthority("SCOPE_message.read")
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 应用 OAuth2 introspective 资源服务器授权配置
                .apply(new OAuth2IntrospectiveResourceServerAuthorizationConfigurer())
                // 配置 Opaque Token introspector 支持
                .opaqueTokenIntrospectorSupport();

        // 返回配置后的 SecurityFilterChain 实例
        return http.build();
    }
}
