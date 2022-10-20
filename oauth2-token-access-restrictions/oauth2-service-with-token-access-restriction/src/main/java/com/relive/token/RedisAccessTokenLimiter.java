package com.relive.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * @author: ReLive
 * @date: 2022/10/9 21:07
 */
@Slf4j
public class RedisAccessTokenLimiter implements AccessTokenLimiter {
    private static final String ACCESS_TOKEN_LIMIT_TIME_SECONDS = "accessTokenLimitTimeSeconds";
    private static final String ACCESS_TOKEN_LIMIT_RATE = "accessTokenLimitRate";
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Boolean> script;

    public RedisAccessTokenLimiter(RedisTemplate<String, Object> redisTemplate, RedisScript<Boolean> script) {
        Assert.notNull(redisTemplate, "redisTemplate can not be null");
        Assert.notNull(script, "script can not be null");
        this.redisTemplate = redisTemplate;
        this.script = script;
    }


    @Override
    public boolean isAllowed(RegisteredClient registeredClient) {

        TokenSettings tokenSettings = registeredClient.getTokenSettings();
        if (tokenSettings == null || tokenSettings.getSetting(ACCESS_TOKEN_LIMIT_TIME_SECONDS) == null ||
                tokenSettings.getSetting(ACCESS_TOKEN_LIMIT_RATE) == null) {
            return true;
        }
        int accessTokenLimitTimeSeconds = tokenSettings.getSetting(ACCESS_TOKEN_LIMIT_TIME_SECONDS);

        int accessTokenLimitRate = tokenSettings.getSetting(ACCESS_TOKEN_LIMIT_RATE);

        String clientId = registeredClient.getClientId();

        try {
            List<String> keys = getKeys(clientId);

            return redisTemplate.execute(this.script, keys, accessTokenLimitTimeSeconds, accessTokenLimitRate);
        } catch (Exception e) {
            /*
             * 我们不希望硬依赖 Redis 来允许访问。 确保设置
             * 一个警报，知道发生了许多次。
             */
            log.error("Error determining if user allowed from redis", e);
        }
        return true;
    }

    static List<String> getKeys(String id) {
        // 在key周围使用 `{}` 以使用 Redis Key hash tag
        // 这允许使用 redis 集群
        String prefix = "access_token_rate_limiter.{" + id;

        String key = prefix + "}.client";
        return Arrays.asList(key);
    }

}
