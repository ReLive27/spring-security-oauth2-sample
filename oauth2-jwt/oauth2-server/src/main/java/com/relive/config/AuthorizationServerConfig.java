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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.time.Duration;
import java.util.UUID;

/**
 * OAuth2 授权服务器配置类
 * <p>
 * 本配置类负责配置 Spring Authorization Server 的核心组件，包括：
 * <ul>
 *     <li>授权服务器安全过滤器链配置</li>
 *     <li>客户端注册信息存储（JDBC）</li>
 *     <li>授权信息存储（JDBC）</li>
 *     <li>授权同意记录存储（JDBC）</li>
 *     <li>JWT 密钥源配置（JWK）</li>
 *     <li>授权服务器元数据配置（Issuer）</li>
 * </ul>
 *
 * @author ReLive
 * @date 2021/12/13 12:18 下午
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    /**
     * 配置授权服务器端点的安全过滤器链
     * <p>
     * 该方法设置了授权服务器端点的安全规则，包括：
     * <ul>
     *     <li>应用 OAuth2 授权服务器的默认安全配置</li>
     *     <li>配置未认证请求的入口点，重定向到登录页面</li>
     * </ul>
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http.exceptionHandling(exceptions -> exceptions.
                authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))).build();
    }

    /**
     * 配置客户端注册信息存储库（使用 JDBC 持久化）
     * <p>
     * 注册一个示例客户端，支持以下配置：
     * <ul>
     *     <li>客户端认证方式：CLIENT_SECRET_POST 和 CLIENT_SECRET_BASIC</li>
     *     <li>授权类型：授权码、刷新令牌、客户端凭证、密码</li>
     *     <li>访问令牌格式：自包含 JWT</li>
     *     <li>访问令牌有效期：30 分钟</li>
     *     <li>刷新令牌有效期：60 分钟</li>
     * </ul>
     *
     * @param jdbcTemplate JDBC 模板，用于访问数据库
     * @return RegisteredClientRepository 客户端注册信息存储库
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        RegisteredClient registeredClient = RegisteredClient.withId("1")
                .clientId("relive-client")
                .clientSecret("{noop}relive-client")
                .clientAuthenticationMethods(s -> {
                    s.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
                    s.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                })
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                .redirectUri("http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code")
                .scope("message.read")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .requireProofKey(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                        .accessTokenTimeToLive(Duration.ofSeconds(30 * 60))
                        .refreshTokenTimeToLive(Duration.ofSeconds(60 * 60))
                        .reuseRefreshTokens(true)
                        .build())
                .build();

        JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
        registeredClientRepository.save(registeredClient);

        return registeredClientRepository;
    }

    /**
     * 配置授权信息服务（使用 JDBC 持久化）
     * <p>
     * 用于存储和管理 OAuth2 授权信息，包括授权码、访问令牌、刷新令牌等
     *
     * @param jdbcTemplate               JDBC 模板
     * @param registeredClientRepository 客户端注册存储库
     * @return OAuth2AuthorizationService 授权信息服务
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * 配置授权同意服务（使用 JDBC 持久化）
     * <p>
     * 用于存储和管理用户的授权同意记录（用户同意客户端访问其资源的记录）
     *
     * @param jdbcTemplate               JDBC 模板
     * @param registeredClientRepository 客户端注册存储库
     * @return OAuth2AuthorizationConsentService 授权同意服务
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * 配置授权服务器元数据
     * <p>
     * 设置授权服务器的 Issuer（发布者）标识符，用于 JWT 令牌中的 iss 声明
     *
     * @return AuthorizationServerSettings 授权服务器设置
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://127.0.0.1:8080")
                .build();
    }

    /**
     * 配置 JSON Web Key（JWK）源
     * <p>
     * 用于签名和验证 JWT 访问令牌。本配置生成一个 RSA 密钥对，
     * 用于签署由本授权服务器颁发的 JWT 令牌。
     *
     * @return JWKSource<SecurityContext> JWK 密钥源
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

}

