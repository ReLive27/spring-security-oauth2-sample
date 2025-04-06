package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author: ReLive
 * @date: 2022/6/24 11:15 上午
 */
@Configuration(proxyBeanMethods = false) // 声明该类为配置类，并禁用代理以提高性能
@EnableWebSecurity // 开启Spring Security的Web安全功能
@EnableMethodSecurity // 启用方法级安全控制（如 @PreAuthorize 等）
public class ResourceServerConfig {

    /**
     * 定义安全过滤器链的 Bean，用于配置 HTTP 安全策略
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorize) -> authorize
                // 所有请求都需要认证
                .anyRequest().authenticated()
        )
                // 启用 OAuth2 资源服务器功能
                .oauth2ResourceServer()
                // 使用 JWT 令牌进行身份验证
                .jwt();
        return http.build(); // 构建并返回过滤器链
    }

    /**
     * 自定义 JWT 转换器 Bean，用于从 JWT 中提取权限信息
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // 设置 JWT 中用于标识权限的字段名为 "authorities"
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        // 不添加前缀，默认会加 "SCOPE_"，这里设置为空字符串
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        // 设置自定义的权限转换器
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
