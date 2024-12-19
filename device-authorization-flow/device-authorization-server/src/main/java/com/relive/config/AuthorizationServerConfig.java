package com.relive.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
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
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

/**
 * @author: ReLive27
 * @date: 2024/1/20 22:43
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    /**
     * 配置 OAuth2 授权服务器的安全过滤链。
     * <p>
     * 主要设置授权服务器的端点安全性，包括设备授权端点和设备验证端点。
     *
     * @param http HttpSecurity 实例，用于定义安全过滤链。
     * @return 配置后的 SecurityFilterChain。
     * @throws Exception 配置异常。
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        // 配置设备授权端点与设备验证端点
        authorizationServerConfigurer.deviceAuthorizationEndpoint(Customizer.withDefaults())
                .deviceVerificationEndpoint(deviceVerification ->
                        deviceVerification.deviceVerificationResponseHandler(new SimpleUrlAuthenticationSuccessHandler("/success")));

        // 获取授权服务器的端点匹配器
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        // 配置过滤器链
        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated()) // 所有请求需认证
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher)) // 忽略 CSRF 保护
                .apply(authorizationServerConfigurer);

        // 自定义设备客户端身份验证配置
        DeviceClientAuthenticationConfigurer deviceClientAuthenticationConfigurer = new DeviceClientAuthenticationConfigurer();
        deviceClientAuthenticationConfigurer.configure(http);

        // 处理未认证请求，重定向到登录页面
        http.exceptionHandling(exceptions ->
                exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")));

        return http.build();
    }

    /**
     * 配置注册客户端信息存储库。
     * <p>
     * 此方法定义了一个支持设备授权流和刷新令牌的客户端。
     *
     * @return 已注册客户端存储库。
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId("1")
                .clientId("relive-device-client") // 客户端 ID
                .clientAuthenticationMethods(s -> s.add(ClientAuthenticationMethod.NONE)) // 无凭据客户端认证
                .authorizationGrantTypes(a -> {
                    a.add(AuthorizationGrantType.DEVICE_CODE); // 设备授权流
                    a.add(AuthorizationGrantType.REFRESH_TOKEN); // 刷新令牌
                })
                .scope("message.read") // 定义权限范围
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true) // 需要用户授权
                        .requireProofKey(false) // 不使用 PKCE
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED) // 自包含 JWT 访问令牌
                        .accessTokenTimeToLive(Duration.ofSeconds(30 * 60)) // 访问令牌 30 分钟
                        .refreshTokenTimeToLive(Duration.ofSeconds(60 * 60)) // 刷新令牌 60 分钟
                        .deviceCodeTimeToLive(Duration.ofSeconds(30 * 60)) // 设备代码 30 分钟
                        .reuseRefreshTokens(true) // 允许重用刷新令牌
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    /**
     * 配置 OAuth2 授权服务。
     *
     * @return 使用内存存储的 OAuth2AuthorizationService 实例。
     */
    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    /**
     * 配置 OAuth2 授权同意服务。
     *
     * @return 使用内存存储的 OAuth2AuthorizationConsentService 实例。
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService() {
        return new InMemoryOAuth2AuthorizationConsentService();
    }

    /**
     * 配置授权服务器的基本设置。
     *
     * @return 授权服务器设置，包括 issuer（发布者地址）。
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8080") // 授权服务器地址
                .build();
    }

    /**
     * 配置 JWT 密钥源（JWK）。
     * <p>
     * 使用生成的 RSA 密钥对签署 JWT 访问令牌。
     *
     * @return JWKSource 实例。
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * RSA 密钥生成工具类。
     */
    static class Jwks {
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
     * RSA 密钥生成器。
     */
    static class KeyGeneratorUtils {
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
