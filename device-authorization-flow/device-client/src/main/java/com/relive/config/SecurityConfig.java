package com.relive.config;

import com.relive.oauth2.client.configurers.OAuth2DeviceClientAuthenticationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author: ReLive27
 * @date: 2024/3/5 20:20
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.requestMatchers("/", "/img/**").permitAll()
                                .anyRequest().authenticated()
                ).csrf(AbstractHttpConfigurer::disable)
                .apply(new OAuth2DeviceClientAuthenticationConfigurer());
        return http.build();
    }
}
