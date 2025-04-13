package com.relive.configure;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.relive.introspection.CachingOAuth2IntrospectionService;
import com.relive.introspection.JdbcOAuth2IntrospectionService;
import com.relive.introspection.OAuth2Introspection;
import com.relive.introspection.OAuth2IntrospectionService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 配置 OAuth2 资源服务器的相关服务，包含缓存、Redis 配置和 OAuth2 introspection 服务。
 * 该类主要提供两个配置：
 * 1. CacheManager：用于配置缓存管理器，支持 Caffeine 缓存。
 * 2. OAuth2IntrospectionService：用于配置 OAuth2 introspection 服务，支持 Redis 和数据库 JDBC。
 *
 * @author: ReLive
 * @date: 2022/11/24 20:39
 */
@Configuration(proxyBeanMethods = false)
public class OAuth2IntrospectiveResourceServerConfiguration {

    /**
     * 配置 Caffeine 缓存管理器，用于 OAuth2 introspection 缓存
     * Caffeine 是一种高性能的 Java 缓存库，支持基于时间、大小和访问的缓存过期策略。
     *
     * @return 返回一个 CaffeineCacheManager 实例
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // 配置 Caffeine 缓存
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)  // 初始容量
                .maximumSize(200)  // 最大缓存条目数
                .expireAfterWrite(10, TimeUnit.MINUTES)  // 设置缓存过期时间
                .recordStats());  // 开启统计
        return cacheManager;
    }

    /**
     * 配置 OAuth2 introspection 服务，支持 Redis 缓存和数据库 JDBC。
     * 此方法通过 Redis 和数据库连接配置一个联合的 OAuth2 introspection 服务，缓存 introspection 响应。
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @param jdbcTemplate           JDBC 模板，用于访问数据库
     * @return 返回一个配置好的 OAuth2IntrospectionService 实例
     */
    @Bean
    public OAuth2IntrospectionService oAuth2IntrospectionService(RedisConnectionFactory redisConnectionFactory,
                                                                 JdbcTemplate jdbcTemplate) {
        // 配置 Redis 缓存
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                // 设置key为string序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置value为json序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                // 不缓存空值
                .disableCachingNullValues()
                // 设置缓存过期时间
                .entryTtl(Duration.ofMinutes(5));

        // 配置 RedisCacheManager，管理 Redis 缓存
        RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)  // 使用默认配置
                .initialCacheNames(Collections.singleton("oauth2Introspection"))  // 设置初始缓存名称
                .build();

        // 配置 CachingOAuth2IntrospectionService，结合 Redis 缓存和数据库 JDBC 服务
        OAuth2IntrospectionService oAuth2IntrospectionService = new CachingOAuth2IntrospectionService(cacheManager.getCache("oauth2Introspection"), new JdbcOAuth2IntrospectionService(jdbcTemplate));

        // 保存默认的 OAuth2 introspection 信息
        oAuth2IntrospectionService.saveOAuth2Introspection(OAuth2Introspection.withIssuer("http://127.0.0.1:8080")
                .id(UUID.randomUUID().toString())
                .clientId("relive-client")
                .clientSecret("relive-client")
                .introspectionUri("http://127.0.0.1:8080/oauth2/introspect").build());
        return oAuth2IntrospectionService;
    }
}
