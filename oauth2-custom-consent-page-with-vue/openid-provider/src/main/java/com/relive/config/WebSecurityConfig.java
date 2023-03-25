package com.relive.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.relive.authentication.Http200LogoutSuccessHandler;
import com.relive.authentication.Http401UnauthorizedEntryPoint;
import com.relive.authentication.JwtAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;

/**
 * @author: ReLive
 * @date: 2023/3/14 19:18
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class WebSecurityConfig {

    @Autowired
    JWKSource<SecurityContext> jwkSource;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                .formLogin().successHandler(new JwtAuthenticationSuccessHandler(jwkSource)).failureHandler(new AuthenticationEntryPointFailureHandler(new Http401UnauthorizedEntryPoint()))
                .and()
                .logout().logoutSuccessHandler(new Http200LogoutSuccessHandler())
                .and()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .oauth2Login(oauth2Login -> oauth2Login.successHandler(new JwtAuthenticationSuccessHandler(jwkSource)))
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(new Http401UnauthorizedEntryPoint());
        return http.build();
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        //Please apply for the correct clientId and clientSecret on github
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("github")
                .clientId("123456")
                .clientSecret("123456")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:9528/oauth2/callback")
                .scope(new String[]{"read:user"})
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("login")
                .clientName("GitHub").build();


        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    @Bean
    UserDetailsService users() {
        UserDetails user = User.withUsername("admin")
                .password("{noop}111111")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
