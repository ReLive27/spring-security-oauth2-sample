package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author: ReLive
 * @date: 2022/6/24 11:15 上午
 */
@EnableWebSecurity
public class ResourceServerConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.requestMatchers()
                .antMatchers("/resource/article/**")
                .and()
                .authorizeRequests()
                .mvcMatchers("/resource/article/**")
                .access("hasAuthority('SCOPE_read')")
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }
}

