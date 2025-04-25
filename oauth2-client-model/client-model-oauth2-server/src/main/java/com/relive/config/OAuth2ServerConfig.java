package com.relive.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;


/**
 * 配置 OAuth2 授权服务器。
 *
 * @author: ReLive
 * @date: 2022/7/2 10:57 下午
 */
@Configuration
public class OAuth2ServerConfig {

    /**
     * 配置 OAuth2 授权服务器的安全过滤器链。
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 过滤器链
     * @throws Exception 配置可能抛出的异常
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http.exceptionHandling(exceptions -> exceptions.
                authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))).build();
    }

    /**
     * 配置已注册的 OAuth2 客户端存储库。
     *
     * @return RegisteredClientRepository 客户端存储库
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
                .scope("message.read")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .requireProofKey(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED) // 生成 JWT 访问令牌
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256) // ID 令牌签名算法
                        .accessTokenTimeToLive(Duration.ofSeconds(30 * 60)) // 访问令牌有效期
                        .refreshTokenTimeToLive(Duration.ofSeconds(60 * 60)) // 刷新令牌有效期
                        .reuseRefreshTokens(true) // 是否允许重复使用刷新令牌
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    /**
     * 配置授权服务器设置。
     *
     * @return AuthorizationServerSettings 授权服务器配置
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://127.0.0.1:8080")
                .build();
    }

    /**
     * 配置 JWK（JSON Web Key）源。
     *
     * @return JWKSource<SecurityContext> JWK 源
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * JWK 生成工具类。
     */
    static class Jwks {

        private Jwks() {
        }

        /**
         * 生成 RSA 密钥对并创建 RSAKey。
         *
         * @return 生成的 RSAKey
         */
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
     * RSA 密钥对生成工具类。
     */
    static class KeyGeneratorUtils {

        private KeyGeneratorUtils() {
        }

        /**
         * 生成 RSA 密钥对。
         *
         * @return 生成的 KeyPair
         */
        static KeyPair generateRsaKey() {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                return keyPairGenerator.generateKeyPair();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
