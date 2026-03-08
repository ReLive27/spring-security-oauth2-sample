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
 * PKCE OAuth2 客户端安全配置类
 *
 * <p>
 * 配置 Spring Security 作为使用 PKCE 的 OAuth2 客户端。
 * PKCE 客户端无需提供 client_secret，适用于公共客户端（如移动应用、SPA）。
 *
 * <p>
 * 主要配置：
 * <ul>
 *     <li>OAuth2 客户端功能</li>
 *     <li>支持授权码和刷新令牌流程</li>
 *     <li>WebClient 集成用于发起受保护的 HTTP 请求</li>
 * </ul>
 *
 * @author ReLive
 * @date 2022/7/4 9:07 下午
 */
@Configuration
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     *
     * <p>
     * 测试期间开放所有端点权限，并启用 OAuth2 客户端功能。
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        // 测试期间开放所有权限
                        authorizeRequests.anyRequest().permitAll()
                )
                .oauth2Client(withDefaults());
        return http.build();
    }

    /**
     * 配置支持 OAuth2 的 WebClient
     *
     * @param authorizedClientManager 授权客户端管理器
     * @return WebClient Web 客户端
     */
    @Bean
    WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        return WebClient.builder()
                .filter(oauth2Client)
                .build();
    }

    /**
     * 配置授权客户端管理器
     *
     * @param clientRegistrationRepository  客户端注册存储库
     * @param authorizedClientRepository    授权客户端存储库
     * @return OAuth2AuthorizedClientManager 授权客户端管理器
     */
    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                          OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder
                .builder()
                .authorizationCode()
                .refreshToken()
                .build();
        DefaultOAuth2AuthorizedClientManager authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
