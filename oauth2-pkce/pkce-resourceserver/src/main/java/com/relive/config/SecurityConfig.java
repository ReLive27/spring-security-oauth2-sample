package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author: ReLive
 * @date: 2022/7/5 12:45 下午
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain defaultSecurityFilter(HttpSecurity http) throws Exception {
        http.requestMatchers()
                .antMatchers("/resource/article")
                .and()
                .authorizeHttpRequests((authorize) -> authorize
                        .antMatchers("/resource/article")
                        .hasAuthority("SCOPE_message.read")
                        .mvcMatchers()
                )
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        return http.build();
    }
}
