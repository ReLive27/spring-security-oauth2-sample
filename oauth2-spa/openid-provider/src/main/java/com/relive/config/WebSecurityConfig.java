package com.relive.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.relive.authentication.Http200LogoutSuccessHandler;
import com.relive.authentication.JwtAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

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
                .formLogin().successHandler(new JwtAuthenticationSuccessHandler(jwkSource))
                .and()
                .logout().logoutSuccessHandler(new Http200LogoutSuccessHandler())
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable();
        return http.build();
    }

    @Bean
    UserDetailsService users() {
        UserDetails user = User.withUsername("admin")
                .password("{noop}password")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
