package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * Redis 配置类
 *
 * <p>
 * 配置 Redis 模板和 Lua 脚本，用于实现访问令牌的频率限制功能。
 *
 * <p>
 * 主要配置：
 * <ul>
 *     <li>RedisTemplate: 用于与 Redis 交互的模板</li>
 *     <li>RedisScript: 执行令牌限制的 Lua 脚本</li>
 * </ul>
 *
 * @author ReLive
 * @date 2022/10/10 19:46
 */
@Configuration(proxyBeanMethods = false)
public class RedisConfig {

    /**
     * 配置 Redis 模板
     *
     * <p>
     * 使用 String 序列化器处理键，使用 Jackson JSON 序列化器处理值。
     *
     * @param connectionFactory Redis 连接工厂
     * @return RedisTemplate Redis 模板
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> valueSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        return redisTemplate;
    }

    /**
     * 配置 Redis Lua 脚本
     *
     * <p>
     * 加载用于令牌频率限制的 Lua 脚本。Lua 脚本在 Redis 中执行，
     * 确保限制检查的原子性。
     *
     * @return RedisScript<Boolean> Redis 脚本
     */
    @Bean
    @SuppressWarnings("unchecked")
    public RedisScript<Boolean> redisRequestRateLimiterScript() {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("/scripts/access_token_restriction.lua")));
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }
}
