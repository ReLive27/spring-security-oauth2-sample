package com.relive.jose.jwk.source;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于caffeine的 {@link JWKSet} 存储实现
 *
 * @author: ReLive
 * @date: 2022/8/23 19:40
 */
public class CaffeineJWKSetCache implements JWKSetCache {
    private final long lifespan;
    private final long refreshTime;
    private final TimeUnit timeUnit;
    private final Cache<Long, JWK> cache;

    public CaffeineJWKSetCache() {
        this(15L, 5L, TimeUnit.MINUTES);
    }

    public CaffeineJWKSetCache(long lifespan, long refreshTime, TimeUnit timeUnit) {
        this.lifespan = lifespan;
        this.refreshTime = refreshTime;
        if ((lifespan > -1L || refreshTime > -1L) && timeUnit == null) {
            throw new IllegalArgumentException("A time unit must be specified for non-negative lifespans or refresh times");
        } else {
            this.timeUnit = timeUnit;
        }
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder().maximumSize(10);
        if (lifespan > -1L) {
            caffeine.expireAfterWrite(this.lifespan, this.timeUnit);
        }
        this.cache = caffeine.build();

    }

    @Override
    public void put(JWKSet jwkSet) {
        if (jwkSet != null) {
            if (!CollectionUtils.isEmpty(jwkSet.getKeys())) {
                jwkSet.getKeys().forEach(jwk -> cache.put(new Date().getTime(), jwk));
            }
        }
    }

    @Override
    public JWKSet get() {
        List<@NonNull JWK> jwks = new ArrayList<>(cache.asMap().values());
        return CollectionUtils.isEmpty(jwks) ? null : new JWKSet(jwks);
    }

    @Override
    public boolean requiresRefresh() {
        return this.refreshTime > -1L && cache.asMap().keySet().stream()
                .max(Long::compareTo)
                .filter(time -> (new Date()).getTime() > time + TimeUnit.MILLISECONDS.convert(this.refreshTime, this.timeUnit))
                .isPresent();
    }
}
