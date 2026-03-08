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
 * PKCE（Proof Key for Code Exchange）授权服务器配置类
 *
 * <p>
 * 本配置类演示如何配置仅支持 PKCE 的 OAuth2 授权服务器。
 * PKCE 是一种安全机制，用于防止授权码拦截攻击，特别适用于移动应用和单页应用（SPA）。
 *
 * <p>
 * 主要特性：
 * <ul>
 *     <li>客户端无需提供 client_secret（使用 NONE 认证方法）</li>
 *     <li>强制启用 PKCE（requireProofKey = true）</li>
 *     <li>仅支持授权码流程</li>
 *     <li>使用自包含 JWT 作为访问令牌</li>
 * </ul>
 *
 * @author ReLive
 * @date 2022/7/4 8:52 下午
 */
@Configuration(proxyBeanMethods = false)
public class OAuth2ServerConfig {

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
     * 配置支持 PKCE 的客户端注册信息存储库
     *
     * <p>
     * 注册一个仅支持 PKCE 的公共客户端：
     * <ul>
     *     <li>客户端认证方法：NONE（无 client_secret）</li>
     *     <li>强制启用 PKCE（requireProofKey = true）</li>
     *     <li>仅支持授权码流程</li>
     * </ul>
     *
     * @return RegisteredClientRepository 客户端注册信息存储库
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("relive-client")
                .clientAuthenticationMethods(s -> {
                    s.add(ClientAuthenticationMethod.NONE);
                })
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://127.0.0.1:8070/login/oauth2/code/messaging-client-pkce")
                .scope("message.read")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .requireProofKey(true) // 强制启用 PKCE
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                        .accessTokenTimeToLive(Duration.ofSeconds(30 * 60))
                        .refreshTokenTimeToLive(Duration.ofSeconds(60 * 60))
                        .reuseRefreshTokens(true)
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
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

        /**
         * 生成 RSA 密钥
         *
         * @return RSAKey RSA 密钥
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
