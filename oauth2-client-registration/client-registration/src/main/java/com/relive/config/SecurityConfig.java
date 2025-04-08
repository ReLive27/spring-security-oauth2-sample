package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 安全配置类，配置 OAuth2 客户端相关的安全机制和 WebClient 支持。
 *
 * @author: ReLive27
 * @date: 2024/6/2 20:58
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * 配置 Spring Security 的过滤器链。
     * - 当前放开所有权限，允许所有请求（测试阶段使用）
     * - 开启 OAuth2 Client 支持
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        // 测试阶段放开所有权限
                        authorizeRequests.anyRequest().permitAll()
                )
                .oauth2Client(withDefaults()); // 启用 OAuth2 客户端功能
        return http.build();
    }

    /**
     * 配置从数据库中加载客户端注册信息（ClientRegistration）。
     * 使用 JdbcTemplate 查询数据库中的 client_registration 表
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcClientRegistrationRepository(jdbcTemplate);
    }

    /**
     * 配置 OAuth2 授权客户端服务，从数据库保存和加载授权信息（AccessToken、RefreshToken 等）。
     */
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            JdbcTemplate jdbcTemplate,
            ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }

    /**
     * 配置 OAuth2 客户端授权信息存储仓库，基于用户认证信息进行存储。
     * 默认使用 `Authentication.getName()` 来区分每个授权客户端。
     */
    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

    /**
     * 配置带 OAuth2 支持的 WebClient，用于发起带授权信息的 HTTP 请求。
     * 自动根据认证上下文注入 Access Token。
     */
    @Bean
    WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        return WebClient.builder()
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }

    /**
     * 配置 OAuth2 授权客户端管理器，控制授权逻辑。
     * 此处只支持 Authorization Code 授权类型，适用于常见的第三方登录等场景。
     */
    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        // 构建支持的授权模式，这里只启用了 authorization_code
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .build();

        // 创建默认授权客户端管理器
        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);

        // 设置授权策略提供者
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
