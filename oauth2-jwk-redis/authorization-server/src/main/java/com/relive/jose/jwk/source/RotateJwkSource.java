package com.relive.jose.jwk.source;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.JWKGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.relive.jose.KeyIDStrategy;
import com.relive.jose.RotateKeySourceException;
import com.relive.jose.TimestampKeyIDStrategy;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于密钥轮换的 {@link JWKSource}
 *
 * @author: ReLive
 * @date: 2022/8/18 13:23
 * @see JWKSource
 */
@Getter
public final class RotateJwkSource<C extends SecurityContext> implements JWKSource<C> {
    private final JWKSource<C> failoverJWKSource;
    private final JWKSetCache jwkSetCache;
    private final JWKGenerator<? extends JWK> jwkGenerator;
    private KeyIDStrategy keyIDStrategy;

    public RotateJwkSource() {
        this(new InMemoryJWKSetCache(), null, null, null);
    }

    public RotateJwkSource(JWKSetCache jwkSetCache) {
        this(jwkSetCache, null, null, null);
    }

    public RotateJwkSource(JWKSetCache jwkSetCache, JWKSource<C> failoverJWKSource) {
        this(jwkSetCache, failoverJWKSource, null, null);
    }

    public RotateJwkSource(JWKSetCache jwkSetCache, JWKGenerator<? extends JWK> jwkGenerator) {
        this(jwkSetCache, null, jwkGenerator, null);
    }

    public RotateJwkSource(JWKSetCache jwkSetCache, JWKSource<C> failoverJWKSource, JWKGenerator<? extends JWK> jwkGenerator, KeyIDStrategy keyIDStrategy) {
        Assert.notNull(jwkSetCache, "jwkSetCache cannot be null");
        this.jwkSetCache = jwkSetCache;
        this.failoverJWKSource = failoverJWKSource;
        if (jwkGenerator == null) {
            this.jwkGenerator = new RSAKeyGenerator(RSAKeyGenerator.MIN_KEY_SIZE_BITS);
        } else {
            this.jwkGenerator = jwkGenerator;
        }
        if (keyIDStrategy == null) {
            this.keyIDStrategy = new TimestampKeyIDStrategy();
        } else {
            this.keyIDStrategy = keyIDStrategy;
        }

    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, C context) throws RotateKeySourceException {
        JWKSet jwkSet = this.jwkSetCache.get();
        if (this.jwkSetCache.requiresRefresh() || jwkSet == null) {
            try {
                synchronized (this) {
                    jwkSet = this.jwkSetCache.get();
                    if (this.jwkSetCache.requiresRefresh() || jwkSet == null) {
                        jwkSet = this.updateJWKSet(jwkSet);
                    }
                }
            } catch (Exception e) {
                List<JWK> failoverMatches = this.failover(e, jwkSelector, context);
                if (failoverMatches != null) {
                    return failoverMatches;
                }

                if (jwkSet == null) {
                    throw e;
                }
            }
        }
        List<JWK> jwks = jwkSelector.select(jwkSet);
        if (!jwks.isEmpty()) {
            return jwks;
        } else {
            return Collections.emptyList();
        }
    }

    private JWKSet updateJWKSet(JWKSet jwkSet) throws RotateKeySourceException {
        JWK jwk;
        try {
            jwkGenerator.keyID(keyIDStrategy.generateKeyID());
            jwk = jwkGenerator.generate();
        } catch (JOSEException e) {
            throw new RotateKeySourceException("Couldn't generate JWK:" + e.getMessage(), e);
        }
        JWKSet updateJWKSet = new JWKSet(jwk);
        this.jwkSetCache.put(updateJWKSet);
        if (jwkSet != null) {
            List<JWK> keys = jwkSet.getKeys();
            List<JWK> updateJwks = keys.stream().collect(Collectors.toList());
            updateJwks.add(jwk);
            updateJWKSet = new JWKSet(keys);
        }
        return updateJWKSet;
    }

    private List<JWK> failover(Exception exception, JWKSelector jwkSelector, C context) throws RotateKeySourceException {
        if (this.getFailoverJWKSource() == null) {
            return null;
        } else {
            try {
                return this.getFailoverJWKSource().get(jwkSelector, context);
            } catch (KeySourceException e) {
                throw new RotateKeySourceException(exception.getMessage() + "; Failover JWK source retrieval failed with: " + e.getMessage(), e);
            }
        }
    }

    public void setKeyIDStrategy(KeyIDStrategy keyIDStrategy) {
        this.keyIDStrategy = keyIDStrategy;
    }
}
