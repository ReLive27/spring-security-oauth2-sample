package com.relive.configure;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.relive.introspection.JdbcOAuth2IntrospectionService;
import com.relive.introspection.OAuth2Introspection;
import com.relive.introspection.OAuth2IntrospectionService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author: ReLive
 * @date: 2022/11/24 20:39
 */
@Configuration(proxyBeanMethods = false)
public class OAuth2IntrospectiveResourceServerConfiguration {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(200)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }

    @Bean
    public OAuth2IntrospectionService oAuth2IntrospectionService(JdbcTemplate jdbcTemplate) {
        OAuth2IntrospectionService oAuth2IntrospectionService = new JdbcOAuth2IntrospectionService(jdbcTemplate);
        oAuth2IntrospectionService.saveOAuth2Introspection(OAuth2Introspection.withIssuer("http://127.0.0.1:8080")
                .id(UUID.randomUUID().toString())
                .clientId("relive-client")
                .clientSecret("relive-client")
                .introspectionUri("http://127.0.0.1:8080/oauth2/introspect").build());
        return oAuth2IntrospectionService;
    }
}
