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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.time.Duration;

/**
 * OAuth2 与 OpenID Connect 认证服务器配置
 *
 * 配置内容包括：
 * - 授权服务器端点安全拦截配置
 * - 客户端注册信息持久化
 * - 授权信息持久化
 * - 授权同意记录持久化
 * - JWK 密钥对及 JWT 解码器配置
 * - Issuer 配置
 *
 * @author ReLive
 * @date 2022/6/23 下午2:03
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    /**
     * 授权服务器端点的安全过滤器链
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 安全过滤器链
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        // 启用 OpenID Connect 协议支持
        authorizationServerConfigurer.oidc(Customizer.withDefaults());

        // 获取授权服务器端点的请求匹配器
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        return http.securityMatcher(endpointsMatcher)
                .authorizeHttpRequests((authorizeRequests) ->
                        authorizeRequests.anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher)) // 禁用端点的 CSRF
                .apply(authorizationServerConfigurer)
                .and()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt) // 启用 JWT 资源服务器支持
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))) // 未认证跳转到登录页面
                .apply(authorizationServerConfigurer)
                .and()
                .build();
    }

    /**
     * 注册并持久化 OAuth2 客户端信息
     *
     * @param jdbcTemplate JdbcTemplate 数据访问对象
     * @return RegisteredClientRepository 客户端注册信息仓库
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        RegisteredClient registeredClient = RegisteredClient.withId("relive-messaging-oidc")
                .clientId("relive-client")
                .clientSecret("{noop}relive-client") // 使用 NoOp 加密（仅用于示例）
                .clientAuthenticationMethods(s -> {
                    s.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
                    s.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                })
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:8070/login/oauth2/code/messaging-gateway-oidc")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .scope("read")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false) // 不需要用户手动授权确认
                        .requireProofKey(false) // 不启用 PKCE
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED) // 使用自包含 JWT 作为 access_token
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256) // ID Token 使用 RS256 签名
                        .accessTokenTimeToLive(Duration.ofMinutes(30)) // access_token 有效期 30 分钟
                        .refreshTokenTimeToLive(Duration.ofHours(1)) // refresh_token 有效期 1 小时
                        .reuseRefreshTokens(true) // 启用 refresh_token 重用
                        .build())
                .build();

        JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
        registeredClientRepository.save(registeredClient);
        return registeredClientRepository;
    }

    /**
     * 持久化授权信息，如：code、access_token、refresh_token 等
     *
     * @param jdbcTemplate 数据库访问模板
     * @param registeredClientRepository 客户端注册仓库
     * @return OAuth2AuthorizationService 授权服务
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * 持久化用户同意记录（授权确认信息）
     *
     * @param jdbcTemplate 数据库访问模板
     * @param registeredClientRepository 客户端注册仓库
     * @return OAuth2AuthorizationConsentService 授权同意服务
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * 配置授权服务器的元信息（如 Issuer）
     *
     * @return AuthorizationServerSettings 授权服务器设置
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://127.0.0.1:8080") // 授权服务器地址
                .build();
    }

    /**
     * 配置 JWK 源（用于签名 JWT）
     *
     * @return JWKSource 密钥源
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa(); // 生成 RSA 密钥对
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * JWT 解码器，用于验证 access_token
     *
     * @param jwkSource JWK 密钥源
     * @return JwtDecoder JWT 解码器
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
}
