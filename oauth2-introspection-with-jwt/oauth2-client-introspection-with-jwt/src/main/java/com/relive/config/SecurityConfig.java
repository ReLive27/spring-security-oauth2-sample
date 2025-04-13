package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 安全配置类，配置 Spring Security 和 OAuth2 客户端相关内容
 *
 * @author: ReLive
 * @date: 2022/11/27 20:06
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * 配置 Spring Security 的安全过滤链
     * 当前配置放行所有请求，并启用 OAuth2 客户端功能
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 安全过滤链
     * @throws Exception 异常抛出
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        // 方便测试，放行所有请求，不进行权限控制
                        authorizeRequests.anyRequest().permitAll()
                )
                // 启用 OAuth2 客户端支持
                .oauth2Client(withDefaults());
        return http.build();
    }

    /**
     * 创建 WebClient 实例，并配置 OAuth2 客户端授权过滤器
     *
     * @param authorizedClientManager OAuth2 客户端管理器
     * @return WebClient 配置了 OAuth2 的客户端
     */
    @Bean
    WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        // 创建支持 OAuth2 的 ExchangeFilterFunction，用于自动处理令牌
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        // 将 OAuth2 配置应用到 WebClient 中
        return WebClient.builder()
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }

    /**
     * 配置 OAuth2 客户端管理器，支持授权码和刷新令牌模式
     *
     * @param clientRegistrationRepository 客户端注册信息仓库
     * @param authorizedClientRepository   已授权客户端信息仓库
     * @return OAuth2AuthorizedClientManager 客户端管理器
     */
    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                          OAuth2AuthorizedClientRepository authorizedClientRepository) {

        // 构建支持的授权方式：授权码 + 刷新令牌
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder
                        .builder()
                        .authorizationCode()   // 支持授权码模式
                        .refreshToken()        // 支持刷新令牌模式
                        .build();

        // 创建默认 OAuth2 客户端管理器
        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);

        // 设置授权提供者
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
