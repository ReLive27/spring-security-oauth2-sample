package com.relive.security;

import com.ecwid.consul.v1.ConsulClient;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.relive.jose.source.ConsulConfigRotateJWKSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
 * @date: 2022/9/30 15:50
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http
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
                .redirectUri("http://127.0.0.1:8070/login/oauth2/code/messaging-client-authorization-code")
                .scope("message.read")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .requireProofKey(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED) //Self-contained token, using JWT format
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                        .accessTokenTimeToLive(Duration.ofSeconds(15 * 60))
                        .refreshTokenTimeToLive(Duration.ofSeconds(30 * 60))
                        .reuseRefreshTokens(false)
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
     * Key rotation based on Consul Config
     *
     * @param consulClient
     * @return
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource(ConsulClient consulClient) {
        return new ConsulConfigRotateJWKSource<>(consulClient);
    }

    /**
     * Set kid and get the maximum kid through {@link JWKSource}
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
