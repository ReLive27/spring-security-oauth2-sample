package com.relive.jose.source;

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.JWKGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.relive.jose.ConsulConfigKeySourceException;
import com.relive.jose.KeyIDStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: ReLive
 * @date: 2022/9/1 12:41
 */
public class ConsulConfigRotateJWKSource<C extends SecurityContext> implements JWKSource<C> {
    private ObjectMapper objectMapper = new ObjectMapper();
    private final JWKSource<C> failoverJWKSource;
    private final ConsulClient consulClient;
    private final JWKSetCache jwkSetCache;
    private final JWKGenerator<? extends JWK> jwkGenerator;
    private KeyIDStrategy keyIDStrategy = this::generateKeyId;
    private String path = "/config/apps/data";


    public ConsulConfigRotateJWKSource(ConsulClient consulClient) {
        this(consulClient, null, null, null);
    }

    public ConsulConfigRotateJWKSource(ConsulClient consulClient, JWKSetCache jwkSetCache, JWKGenerator<? extends JWK> jwkGenerator, JWKSource<C> failoverJWKSource) {
        this.consulClient = consulClient;
        if (jwkSetCache == null) {
            this.jwkSetCache = new DefaultJWKSetCache();
        } else {
            this.jwkSetCache = jwkSetCache;
        }
        if (jwkGenerator == null) {
            this.jwkGenerator = new RSAKeyGenerator(RSAKeyGenerator.MIN_KEY_SIZE_BITS);
        } else {
            this.jwkGenerator = jwkGenerator;
        }
        this.failoverJWKSource = failoverJWKSource;
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, C context) throws KeySourceException {
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

    private JWKSet updateJWKSet(JWKSet jwkSet)
            throws ConsulConfigKeySourceException {
        JWK jwk;
        try {
            jwkGenerator.keyID(this.keyIDStrategy.generateKeyID());
            jwk = jwkGenerator.generate();
        } catch (JOSEException e) {
            throw new ConsulConfigKeySourceException("Couldn't generate JWK:" + e.getMessage(), e);
        }
        List<JWK> jwks = new ArrayList<>();
        jwks.add(jwk);
        if (jwkSet != null) {
            List<JWK> keys = jwkSet.getKeys();
            List<JWK> updateJwks = new ArrayList<>(keys);
            jwks.addAll(updateJwks);
        }
        JWKSet result = new JWKSet(jwks);
        try {
            consulClient.setKVValue(path, objectMapper.writeValueAsString(Collections.singletonMap("jwks", result.toString())));
        } catch (JsonProcessingException e) {
            throw new ConsulConfigKeySourceException("JWK cannot convert JSON:" + e.getMessage(), e);
        }
        jwkSetCache.put(result);
        return result;
    }


    private String generateKeyId() {
        return String.valueOf(System.currentTimeMillis());
    }

    private List<JWK> failover(Exception exception, JWKSelector jwkSelector, C context) throws ConsulConfigKeySourceException {
        if (this.getFailoverJWKSource() == null) {
            return null;
        } else {
            try {
                return this.getFailoverJWKSource().get(jwkSelector, context);
            } catch (KeySourceException e) {
                throw new ConsulConfigKeySourceException(exception.getMessage() + "; Failover JWK source retrieval failed with: " + e.getMessage(), e);
            }
        }
    }

    public JWKSource<C> getFailoverJWKSource() {
        return this.failoverJWKSource;
    }

    public void setKeyIDStrategy(KeyIDStrategy keyIDStrategy) {
        this.keyIDStrategy = keyIDStrategy;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
