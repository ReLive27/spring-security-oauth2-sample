package com.relive.config;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.relive.jose.jwk.source.RedisJWKSetCache;
import com.relive.jose.jwk.source.RotateJwkSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2TokenFormat;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * @author: ReLive
 * @date: 2022/8/18 13:17
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        //添加TokenContextFilter在OAuth2TokenEndpointFilter之前
        return http.apply(new TokenContextConfigurer<HttpSecurity>())
                .and()
                .exceptionHandling(exceptions -> exceptions.
                        authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))).build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
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
                        .requireAuthorizationConsent(true)//requireAuthorizationConsent：是否需要授权统同意
                        .requireProofKey(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED) // 生成JWT令牌
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)//idTokenSignatureAlgorithm：签名算法
                        .accessTokenTimeToLive(Duration.ofSeconds(30 * 60))//accessTokenTimeToLive：access_token有效期
                        .refreshTokenTimeToLive(Duration.ofSeconds(60 * 60))//refreshTokenTimeToLive：refresh_token有效期
                        .reuseRefreshTokens(true)//reuseRefreshTokens：是否重用刷新令牌
                        .build())
                .build();


        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    @Bean
    public ProviderSettings providerSettings() {
        return ProviderSettings.builder()
                .issuer("http://127.0.0.1:8080")
                .build();
    }


    /**
     * 定义 {@link RotateJwkSource} 轮询密钥的 {@link JWKSource}
     *
     * @return
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource(RedisConnectionFactory connectionFactory) {
        RedisJWKSetCache redisJWKSetCache = new RedisJWKSetCache(connectionFactory);
        redisJWKSetCache.setPrefix("auth-server");

        return new RotateJwkSource<>(redisJWKSetCache);
    }

    /**
     * 设置kid，通过 {@link JWKSource} 获取最大值kid
     *
     * @param jwkSource
     * @return
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(JWKSource<SecurityContext> jwkSource) {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType()) ||
                    OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {

                JWKSelector jwkSelector = new JWKSelector(new JWKMatcher.Builder().build());
                List<JWK> jwks;
                try {
                    jwks = jwkSource.get(jwkSelector, null);
                } catch (KeySourceException e) {
                    throw new IllegalStateException("Failed to select the JWK(s) -> " + e.getMessage(), e);
                }
                String kid = jwks.stream().map(JWK::getKeyID)
                        .max(String::compareTo)
                        .orElseThrow(() -> new IllegalArgumentException("kid not found"));
                context.getHeaders().keyId(kid);
            }
        };
    }
}
