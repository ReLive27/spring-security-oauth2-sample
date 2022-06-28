package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author: ReLive
 * @date: 2022/6/27 12:58 下午
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.requestMatchers()
                .antMatchers("/resource/article")
                .and()
                .authorizeRequests()
                .mvcMatchers("/resource/article")
                .access("hasAuthority('SCOPE_message.read')")
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }
}
