package com.relive.jose.jwk.source;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.relive.jose.RotateKeySourceException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 基于redis的 {@link JWKSet} 存储实现
 *
 * @author: ReLive
 * @date: 2022/8/24 09:07
 */
public class RedisJWKSetCache implements JWKSetCache {
    private static final boolean springDataRedis_2_0 = ClassUtils.isPresent("org.springframework.data.redis.connection.RedisStandaloneConfiguration", RedisJWKSetCache.class.getClassLoader());
    private static final String JWK_KEY = "jwks";
    private final RedisConnectionFactory connectionFactory;

    private String prefix = "";
    //TODO 序列化策略
    private Method redisConnectionSet_2_0;

    public RedisJWKSetCache(RedisConnectionFactory connectionFactory) {
        Assert.notNull(connectionFactory, "redisConnectionFactory cannot be null");
        this.connectionFactory = connectionFactory;
        if (springDataRedis_2_0) {
            this.loadRedisConnectionMethods_2_0();
        }
    }

    @Override
    public void put(JWKSet jwkSet) {
        if (jwkSet != null) {
            if (!CollectionUtils.isEmpty(jwkSet.getKeys())) {
                byte[] key = null;
                byte[] value = null;
                RedisConnection connection = this.getConnection();

                try {
                    if (springDataRedis_2_0) {
                        try {
                            this.redisConnectionSet_2_0.invoke(connection, key, value);
                        } catch (Exception e) {
                            //TODO 异常处理
                            throw new RuntimeException("e", e);
                        }
                    } else {
                        connection.set(key, value);
                    }
                } finally {
                    connection.close();
                }
            }
        }
    }

    @Override
    public JWKSet get() {
        return null;
    }

    @Override
    public boolean requiresRefresh() {
        return false;
    }

    private byte[] serializeKey(String key) {
        //TODO
        return null;
    }

    private byte[] serialize(Object o) {
        //TODO
        return null;
    }

    private void loadRedisConnectionMethods_2_0() {
        //TODO 改为list
        this.redisConnectionSet_2_0 = ReflectionUtils.findMethod(RedisConnection.class, "set", new Class[]{byte[].class, byte[].class});
    }

    private RedisConnection getConnection() {
        return this.connectionFactory.getConnection();
    }


}
