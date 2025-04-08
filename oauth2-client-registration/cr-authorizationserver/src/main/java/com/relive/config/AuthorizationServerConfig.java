package com.relive.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import static com.relive.config.CustomClientMetadataConfig.configureCustomClientMetadataConverters;

/**
 * 授权服务器配置类，配置了 Spring Authorization Server 所需的核心组件：
 * - 安全过滤链
 * - 客户端注册仓库
 * - JWK 密钥源
 * - 授权服务器元数据信息
 *
 * 包含对 OIDC 客户端动态注册的支持。
 *
 * @author: ReLive
 * @date: 2022/11/28 19:37
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    /**
     * 授权服务器的安全过滤链配置
     *
     * @param http HttpSecurity 实例
     * @return 配置好的 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // 应用授权服务器默认配置（含 OAuth2、OIDC 等端点）
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // 开启 OIDC 客户端注册端点，并配置自定义 metadata 解析器
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(oidc -> oidc.clientRegistrationEndpoint(clientRegistrationEndpoint -> {
                    clientRegistrationEndpoint
                            .authenticationProviders(configureCustomClientMetadataConverters());
                }));

        // 启用 JWT 资源服务器支持
        http.oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer.jwt(Customizer.withDefaults()));

        // 如果未认证，跳转到登录页
        return http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .build();
    }

    /**
     * 配置客户端注册仓库，基于 JDBC 存储客户端信息。
     * 初始化时注册一个 registrar-client，用于管理其他客户端的注册。
     *
     * @param jdbcTemplate JDBC 模板
     * @return 客户端注册仓库实例
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        RegisteredClient registrarClient = RegisteredClient.withId("1")
                .clientId("registrar-client")
                .clientSecret("{noop}relive27-client") // 注意：此处未加密
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("client.create")
                .scope("client.read")
                .build();

        JdbcRegisteredClientRepository jdbcRegisteredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
        jdbcRegisteredClientRepository.save(registrarClient);
        return jdbcRegisteredClientRepository;
    }

    /**
     * 授权服务器的元数据信息配置（issuer 等）
     *
     * @return 授权服务器设置
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://127.0.0.1:8080") // 设置发行者地址
                .build();
    }

    /**
     * 配置 JWK 密钥源，用于签发 JWT 令牌。
     *
     * @return JWK 源
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * RSA 密钥生成工具类，用于创建 JWK 所需的密钥对。
     */
    static class Jwks {

        private Jwks() {
        }

        /**
         * 生成 RSA 密钥，并构造成 Nimbus 的 RSAKey 对象
         *
         * @return RSAKey 实例
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
     * 密钥生成工具类，封装生成 KeyPair 的过程
     */
    static class KeyGeneratorUtils {

        private KeyGeneratorUtils() {
        }

        /**
         * 生成 RSA 密钥对（2048 位）
         *
         * @return KeyPair 实例
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
