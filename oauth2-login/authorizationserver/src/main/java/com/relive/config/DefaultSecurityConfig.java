package com.relive.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 默认安全配置类（OAuth2 登录示例）
 *
 * <p>
 * 配置 Spring Security 保护授权服务器的用户端点，
 * 同时作为资源服务器验证 JWT 令牌。
 *
 * @author ReLive
 * @date 2022/6/24 4:22 下午
 */
@Configuration(proxyBeanMethods = false)
public class DefaultSecurityConfig {

    /**
     * 配置默认安全过滤器链
     *
     * <p>
     * 设置安全规则：
     * <ul>
     *     <li>/userInfo 端点需要 profile 范围权限</li>
     *     <li>其他请求需要认证</li>
     *     <li>启用表单登录</li>
     *     <li>启用 JWT 资源服务器支持</li>
     * </ul>
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/userInfo").hasAuthority("SCOPE_profile")
                .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        return http.build();
    }

    /**
     * 配置 JWT 解码器
     *
     * @param jwkSource JWK 密钥源
     * @return JwtDecoder JWT 解码器
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * 配置用户详情服务
     *
     * @return UserDetailsService 用户详情服务
     */
    @Bean
    public UserDetailsService users() {
        UserDetails user = User.withUsername("admin")
                .password("{noop}password")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
