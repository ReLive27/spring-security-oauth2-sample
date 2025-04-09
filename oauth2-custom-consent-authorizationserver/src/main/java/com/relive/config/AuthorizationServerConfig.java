package com.relive.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.relive.jose.Jwks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.time.Duration;
import java.util.UUID;

/**
 * 授权服务器配置类
 *
 * 配置了授权端点、安全过滤器链、客户端注册、Token 签名密钥等。
 * 本类使用 Spring Authorization Server 提供的 API。
 *
 * @author: ReLive
 * @date: 2022/05/22 22:18 下午
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    // 自定义授权确认页面路径
    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";

    /**
     * 授权服务器的安全过滤器链配置
     *
     * 包括：
     * - 启用 OAuth2 授权服务器配置
     * - 自定义授权确认页面路径
     * - 忽略 CSRF
     * - 默认未认证跳转登录页
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        // 配置自定义授权确认页面
        authorizationServerConfigurer.authorizationEndpoint(authorizationEndpoint ->
                authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI));

        // 匹配授权服务器相关的请求端点
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http.securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .apply(authorizationServerConfigurer);

        // 配置未认证时跳转到登录页
        return http.exceptionHandling(exceptions ->
                exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
        ).build();
    }

    /**
     * 配置注册的 OAuth2 客户端
     *
     * 使用内存方式保存一个测试客户端，支持多种授权模式。
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("relive-client") // 客户端 ID
                .clientSecret("{noop}relive-client") // 客户端密钥（{noop} 表示明文）
                .clientName("ReLive27") // 客户端显示名称
                .clientAuthenticationMethods(methods -> {
                    methods.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
                    methods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                })
                // 支持的授权类型
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                // 回调地址
                .redirectUri("http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code")
                // 客户端可请求的作用域
                .scope(OidcScopes.PROFILE)
                .scope("message.read")
                .scope("message.write")
                // 客户端设置
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true) // 需要用户授权确认页面
                        .requireProofKey(false) // 是否启用 PKCE
                        .build())
                // Token 设置
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED) // 自包含 Token（JWT）
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256) // ID Token 使用的签名算法
                        .accessTokenTimeToLive(Duration.ofMinutes(30)) // 访问 Token 有效期
                        .refreshTokenTimeToLive(Duration.ofHours(1)) // 刷新 Token 有效期
                        .reuseRefreshTokens(true) // 是否复用刷新 Token
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    /**
     * 授权确认服务（用于保存用户对客户端授权的选择）
     *
     * 使用内存方式存储授权同意信息。
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService() {
        return new InMemoryOAuth2AuthorizationConsentService();
    }

    /**
     * 授权服务器的元信息配置
     *
     * 设置 issuer（颁发者）地址
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://127.0.0.1:8080") // 颁发者地址
                .build();
    }

    /**
     * JWT 签名密钥配置（JWK）
     *
     * 使用生成的 RSA 密钥构建 JWKSet，供资源服务器验证 JWT 使用。
     */
    @Bean
    JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa(); // 自定义生成 RSA 密钥
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

}
