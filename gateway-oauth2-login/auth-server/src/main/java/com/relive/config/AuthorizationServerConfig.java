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
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2TokenFormat;
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
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.time.Duration;

/**
 * OAuth2及OpenID Connect 配置
 *
 * @author: ReLive
 * @date: 2022/6/23 2:03 下午
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer<>();
        //配置OIDC
        authorizationServerConfigurer.oidc(Customizer.withDefaults());

        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        return http.requestMatcher(endpointsMatcher)
                .authorizeRequests((authorizeRequests) -> {
                    ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) authorizeRequests.anyRequest()).authenticated();
                }).csrf((csrf) -> {
                    csrf.ignoringRequestMatchers(new RequestMatcher[]{endpointsMatcher});
                }).apply(authorizationServerConfigurer)
                .and()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .exceptionHandling(exceptions -> exceptions.
                        authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .apply(authorizationServerConfigurer)
                .and()
                .build();
    }

    /**
     * 持久化OAuth2客户端
     *
     * @param jdbcTemplate
     * @return
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        RegisteredClient registeredClient = RegisteredClient.withId("relive-messaging-oidc")
                .clientId("relive-client")
                .clientSecret("{noop}relive-client")
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
                        .requireAuthorizationConsent(false) //不需要授权同意
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

        JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
        registeredClientRepository.save(registeredClient);
        return registeredClientRepository;
    }

    /**
     * 负责在授权过程中持久化授权信息，例如code，access_token,refresh_token
     *
     * @param jdbcTemplate
     * @param registeredClientRepository
     * @return
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * 持久化用户同意"授权同意"信息
     *
     * @param jdbcTemplate
     * @param registeredClientRepository
     * @return
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public ProviderSettings providerSettings() {
        return ProviderSettings.builder()
                .issuer("http://127.0.0.1:8080")
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
}
