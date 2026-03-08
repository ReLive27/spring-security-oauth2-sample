package com.relive.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.relive.token.AccessTokenLimiter;
import com.relive.token.AccessTokenRestrictionCustomizer;
import com.relive.token.RedisAccessTokenLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

/**
 * OAuth2 授权服务器配置类（带访问令牌频率限制）
 *
 * <p>
 * 本配置类演示如何在授权服务器中实现访问令牌的频率限制功能，
 * 用于控制客户端获取访问令牌的速率，防止令牌滥用。
 *
 * <p>
 * 主要特性：
 * <ul>
 *     <li>基于 Redis 的令牌频率限制（使用 Redis 滑动窗口算法）</li>
 *     <li>自定义 Token Customizer 在 JWT 中嵌入限制信息</li>
 *     <li>客户端凭证授权类型（Client Credentials）</li>
 *     <li>自定义令牌设置（accessTokenLimitTimeSeconds 和 accessTokenLimitRate）</li>
 * </ul>
 *
 * @author ReLive
 * @date 2022/10/8 20:58
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    /**
     * 配置授权服务器安全过滤器链
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
     * 配置客户端注册信息存储库
     *
     * <p>
     * 注册支持客户端凭证授权类型的客户端，并配置自定义令牌设置：
     * <ul>
     *     <li>accessTokenLimitTimeSeconds: 限制时间窗口（5分钟）</li>
     *     <li>accessTokenLimitRate: 限制次数（3次）</li>
     * </ul>
     * 表示在 5 分钟内最多允许获取 3 个访问令牌。
     *
     * @return RegisteredClientRepository 客户端注册信息存储库
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("relive-client")
                .clientSecret("{noop}relive-client")
                .clientAuthenticationMethods(s -> {
                    s.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
                    s.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                })
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://127.0.0.1:8070/login/oauth2/code/messaging-client-model")
                .scope("message.read")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                        .accessTokenTimeToLive(Duration.ofSeconds(30 * 60))
                        .refreshTokenTimeToLive(Duration.ofSeconds(60 * 60))
                        .reuseRefreshTokens(true)
                        .setting("accessTokenLimitTimeSeconds", 5 * 60)
                        .setting("accessTokenLimitRate", 3)
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }


    /**
     * 配置基于 Redis 的访问令牌频率限制器
     *
     * <p>
     * 当 RedisTemplate 存在于类路径时，创建 Redis 实现的令牌限制器，
     * 使用 Redis 滑动窗口算法实现分布式频率限制。
     *
     * @param redisTemplate Redis 模板
     * @param script        Redis Lua 脚本
     * @return AccessTokenLimiter 访问令牌限制器
     */
    @Bean
    @ConditionalOnClass(RedisTemplate.class)
    public AccessTokenLimiter tokenLimiter(RedisTemplate redisTemplate, RedisScript script) {
        return new RedisAccessTokenLimiter(redisTemplate, script);
    }

    /**
     * 配置 OAuth2 Token Customizer
     *
     * <p>
     * 当 AccessTokenLimiter 存在于类路径时，创建自定义的 Token Customizer，
     * 在 JWT 令牌中嵌入频率限制相关信息。
     *
     * @param tokenLimiter 访问令牌限制器
     * @return OAuth2TokenCustomizer Token 自定义器
     */
    @Bean
    @ConditionalOnClass(AccessTokenLimiter.class)
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(AccessTokenLimiter tokenLimiter) {
        return new AccessTokenRestrictionCustomizer(tokenLimiter);
    }

    /**
     * 配置授权服务器元数据
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
     *
     * @return JWKSource<SecurityContext> JWK 密钥源
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * RSA 密钥生成工具类
     */
    static class Jwks {

        private Jwks() {
        }

        public static RSAKey generateRsa() {
            KeyPair keyPair = KeyGeneratorUtils.generateRsaKey();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        }
    }

    /**
     * RSA 密钥生成器工具类
     */
    static class KeyGeneratorUtils {

        private KeyGeneratorUtils() {
        }

        /**
         * 生成 RSA 密钥对
         *
         * @return KeyPair RSA 密钥对
         */
        static KeyPair generateRsaKey() {
            KeyPair keyPair;
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                keyPair = keyPairGenerator.generateKeyPair();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            return keyPair;
        }
    }
}
