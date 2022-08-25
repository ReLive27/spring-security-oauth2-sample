package com.relive.jose.jwk.source;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于内存的 {@link JWKSet} 存储实现
 *
 * @author: ReLive
 * @date: 2022/8/22 21:36
 * @see JWKWithTimestamp
 */
public class InMemoryJWKSetCache implements JWKSetCache {
    private final long lifespan;
    private final long refreshTime;
    private final TimeUnit timeUnit;
    private volatile Set<JWKWithTimestamp> jwkWithTimestamps;


    public InMemoryJWKSetCache() {
        this(15L, 5L, TimeUnit.MINUTES);
    }

    public InMemoryJWKSetCache(long lifespan, long refreshTime, TimeUnit timeUnit) {
        this.lifespan = lifespan;
        this.refreshTime = refreshTime;
        if ((lifespan > -1L || refreshTime > -1L) && timeUnit == null) {
            throw new IllegalArgumentException("A time unit must be specified for non-negative lifespans or refresh times");
        } else {
            this.timeUnit = timeUnit;
        }
        this.jwkWithTimestamps = new LinkedHashSet<>();
    }

    @Override
    public void put(JWKSet jwkSet) {
        if (jwkSet != null) {
            if (!CollectionUtils.isEmpty(jwkSet.getKeys())) {
                List<JWKWithTimestamp> updateJWKWithTs = jwkSet.getKeys().stream().map(JWKWithTimestamp::new)
                        .collect(Collectors.toList());
                this.jwkWithTimestamps.addAll(updateJWKWithTs);
            }
        }
    }

    @Override
    public JWKSet get() {
        return !CollectionUtils.isEmpty(this.jwkWithTimestamps) && !this.isExpired() ? new JWKSet(this.jwkWithTimestamps.stream()
                .filter(t -> t.getDate().getTime() + TimeUnit.MILLISECONDS.convert(this.lifespan, this.timeUnit) > (new Date()).getTime())
                .map(JWKWithTimestamp::getJwk).collect(Collectors.toList())) : null;
    }

    @Override
    public boolean requiresRefresh() {
        return !CollectionUtils.isEmpty(this.jwkWithTimestamps) && this.refreshTime > -1L && this.jwkWithTimestamps.stream().map(jwkWithTimestamp -> jwkWithTimestamp.getDate().getTime())
                .max(Long::compareTo)
                .filter(time -> (new Date()).getTime() > time + TimeUnit.MILLISECONDS.convert(this.refreshTime, this.timeUnit))
                .isPresent();
    }

    public boolean isExpired() {
        return !CollectionUtils.isEmpty(this.jwkWithTimestamps) && this.lifespan > -1L && this.jwkWithTimestamps.stream().map(jwkWithTimestamp -> jwkWithTimestamp.getDate().getTime())
                .max(Long::compareTo)
                .filter(time -> (new Date()).getTime() > time + TimeUnit.MILLISECONDS.convert(this.lifespan, this.timeUnit))
                .isPresent();
    }

    public long getLifespan(TimeUnit timeUnit) {
        return this.lifespan < 0L ? this.lifespan : timeUnit.convert(this.lifespan, this.timeUnit);
    }

    public long getRefreshTime(TimeUnit timeUnit) {
        return this.refreshTime < 0L ? this.refreshTime : timeUnit.convert(this.refreshTime, this.timeUnit);
    }
}
