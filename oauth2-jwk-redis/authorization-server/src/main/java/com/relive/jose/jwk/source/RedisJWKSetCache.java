package com.relive.jose.jwk.source;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import org.springframework.data.redis.connection.DefaultTuple;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisZSetCommands.Limit;
import org.springframework.data.redis.connection.RedisZSetCommands.Range;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于redis的 {@link JWKSet} 存储实现
 *
 * @author: ReLive
 * @date: 2022/8/24 09:07
 */
public class RedisJWKSetCache implements JWKSetCache {
    private static final boolean springDataRedis_2_0 = ClassUtils.isPresent("org.springframework.data.redis.connection.RedisStandaloneConfiguration", RedisJWKSetCache.class.getClassLoader());
    private final RedisConnectionFactory connectionFactory;
    private final String JWK_KEY = "jwks";
    private String prefix = "";
    private RedisSerializer<String> redisSerializer = new Jackson2JsonRedisSerializer<>(String.class);
    private Method redisConnectionSet_2_0;
    private final long lifespan;
    private final long refreshTime;
    private final TimeUnit timeUnit;

    public RedisJWKSetCache(RedisConnectionFactory connectionFactory) {
        this(15L, 5L, TimeUnit.MINUTES, connectionFactory);
    }

    public RedisJWKSetCache(long lifespan, long refreshTime, TimeUnit timeUnit, RedisConnectionFactory connectionFactory) {
        this.lifespan = lifespan;
        this.refreshTime = refreshTime;
        if ((lifespan > -1L || refreshTime > -1L) && timeUnit == null) {
            throw new IllegalArgumentException("A time unit must be specified for non-negative lifespans or refresh times");
        } else {
            this.timeUnit = timeUnit;
        }
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
                RedisConnection connection = this.getConnection();
                byte[] key = this.serializeKey(JWK_KEY);

                connection.openPipeline();

                if (this.lifespan > -1) {
                    long max = new Date().getTime() - TimeUnit.MILLISECONDS.convert(this.lifespan, this.timeUnit);
                    connection.zRemRangeByScore(key, Range.range().lte(max));
                }

                List<JWK> keys = jwkSet.getKeys();
                try {
                    for (JWK jwk : keys) {
                        byte[] value = this.serialize(jwk.toJSONString());

                        if (springDataRedis_2_0) {
                            try {
                                this.redisConnectionSet_2_0.invoke(connection, key, new Date().getTime(), value);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            connection.zAdd(key, new Date().getTime(), value);
                        }
                    }


                    connection.closePipeline();
                } finally {
                    connection.close();
                }
            }
        }
    }

    @Override
    public JWKSet get() {
        RedisConnection connection = this.getConnection();
        byte[] key = this.serializeKey(JWK_KEY);
        try {
            connection.openPipeline();

            Long efficientCount = Optional.ofNullable(connection.zCard(key)).orElse(0L);
            if (efficientCount > 0) {
                Set<byte[]> jwkBytes = connection.zRevRangeByScore(key, Range.range());
                List<JWK> jwks = jwkBytes.stream().map(this::deserialize).map(this::parse).collect(Collectors.toList());

                connection.closePipeline();

                return new JWKSet(jwks);
            }
            connection.closePipeline();

            return null;
        } finally {
            connection.close();
        }
    }

    private JWK parse(String jwkJsonString) {
        try {
            return JWK.parse(jwkJsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean requiresRefresh() {
        RedisConnection connection = this.getConnection();
        byte[] key = this.serializeKey("jwks");
        try {
            connection.openPipeline();
            Long efficientCount = Optional.ofNullable(connection.zCard(key)).orElse(0L);
            Set<Tuple> maximumScoreTuple = connection.zRevRangeByScoreWithScores(key, Range.range(), Limit.limit().count(1));
            connection.closePipeline();

            long lastRefreshTime = 0L;
            if (!CollectionUtils.isEmpty(maximumScoreTuple)) {
                lastRefreshTime = maximumScoreTuple.stream().findFirst().orElse(new DefaultTuple(null, 0.0)).getScore().longValue();
            }
            return efficientCount > 0 && this.refreshTime > -1L && (new Date()).getTime() > lastRefreshTime + TimeUnit.MILLISECONDS.convert(this.refreshTime, this.timeUnit);
        } finally {
            connection.close();
        }

    }

    private byte[] serializeKey(String key) {
        return this.serialize(this.prefix + ":" + key);
    }

    private byte[] serialize(String value) {
        return this.redisSerializer.serialize(value);
    }

    private String deserialize(byte[] bytes) {
        return this.redisSerializer.deserialize(bytes);
    }

    private void loadRedisConnectionMethods_2_0() {
        this.redisConnectionSet_2_0 = ReflectionUtils.findMethod(RedisConnection.class, "zAdd", new Class[]{byte[].class, double.class, byte[].class});
    }

    private RedisConnection getConnection() {
        return this.connectionFactory.getConnection();
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setRedisSerializer(RedisSerializer<String> redisSerializer) {
        this.redisSerializer = redisSerializer;
    }
}
