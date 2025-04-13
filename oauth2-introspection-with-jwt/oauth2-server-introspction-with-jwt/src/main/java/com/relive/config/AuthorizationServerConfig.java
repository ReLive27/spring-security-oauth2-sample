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
 * 授权服务器配置类，配置授权端点、安全过滤器、客户端注册信息、JWK 生成等内容
 *
 * @author: ReLive
 * @date: 2022/11/27 19:41
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    /**
     * 配置授权服务器的安全过滤器链，优先级最高
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // 应用默认的 OAuth2 授权服务器安全配置
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http.exceptionHandling(exceptions -> exceptions.
                authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))).build();
    }

    /**
     * 注册 OAuth2 客户端信息，保存在内存中
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                // 客户端 ID 和密钥（加密方式为 noop，即明文）
                .clientId("relive-client")
                .clientSecret("{noop}relive-client")
                // 支持的客户端认证方式
                .clientAuthenticationMethods(s -> {
                    s.add(ClientAuthenticationMethod.CLIENT_SECRET_POST); // 通过 POST 提交凭证
                    s.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC); // 通过 HTTP Basic 认证
                })
                // 支持的授权类型
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // 授权码模式
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN) // 刷新令牌模式
                // 授权成功后的重定向地址
                .redirectUri("http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code")
                // 可访问的作用域
                .scope("message.read")
                // 客户端设置：是否需要用户确认授权、是否要求使用 PKCE
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true) // 启用授权确认页
                        .requireProofKey(false) // 不要求 PKCE（适用于 Web 应用）
                        .build())
                // Token 设置：包括格式、签名算法、有效期、是否重用刷新令牌等
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED) // 自包含令牌格式（JWT）
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256) // ID Token 签名算法
                        .accessTokenTimeToLive(Duration.ofSeconds(30 * 60)) // 访问令牌有效期为 30 分钟
                        .refreshTokenTimeToLive(Duration.ofSeconds(60 * 60)) // 刷新令牌有效期为 1 小时
                        .reuseRefreshTokens(false) // 禁止重用刷新令牌，每次都会生成新的
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    /**
     * 配置授权服务器相关信息，如 issuer 地址
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://127.0.0.1:8080") // 授权服务器的 issuer 声明
                .build();
    }

    /**
     * 配置 JWK（JSON Web Key）源，用于签发 JWT 的密钥管理
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa(); // 生成 RSA 密钥
        JWKSet jwkSet = new JWKSet(rsaKey); // 构造 JWK 集
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet); // 提供 JWK 给 Spring Security 使用
    }

    /**
     * 工具类：用于生成 RSA 密钥对
     */
    static class Jwks {
        private Jwks() {
        }

        public static RSAKey generateRsa() {
            KeyPair keyPair = KeyGeneratorUtils.generateRsaKey(); // 调用工具类生成密钥对
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic(); // 获取公钥
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate(); // 获取私钥
            // 构造 RSAKey 对象，并设置唯一标识
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        }
    }

    /**
     * 工具类：生成 RSA 密钥对
     */
    static class KeyGeneratorUtils {
        private KeyGeneratorUtils() {
        }

        static KeyPair generateRsaKey() {
            KeyPair keyPair;
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA"); // 指定算法为 RSA
                keyPairGenerator.initialize(2048); // 设置密钥长度为 2048 位
                keyPair = keyPairGenerator.generateKeyPair(); // 生成密钥对
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            return keyPair;
        }
    }
}
