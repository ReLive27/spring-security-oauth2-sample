package com.relive.jose.jwk.source;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.relive.jose.ConsulKeySourceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.List;

/**
 * @author: ReLive
 * @date: 2022/9/13 22:53
 */
public class ConsulJWKSet<C extends SecurityContext> implements JWKSource<C> {
    @Value("${jwks:}")
    private String key;

    private final JWKSource<C> failoverJWKSource;

    public ConsulJWKSet() {
        this(null);
    }

    public ConsulJWKSet(JWKSource<C> failoverJWKSource) {
        this.failoverJWKSource = failoverJWKSource;
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, C context) throws KeySourceException {
        JWKSet jwkSet = null;
        if (StringUtils.hasText(key)) {
            try {
                jwkSet = this.parseJWKSet();
            } catch (Exception e) {
                List<JWK> failoverMatches = this.failover(e, jwkSelector, context);
                if (failoverMatches != null) {
                    return failoverMatches;
                }
                throw e;
            }

            List<JWK> matches = jwkSelector.select(jwkSet);
            if (!matches.isEmpty()) {
                return matches;
            }
        }
        return null;
    }

    private JWKSet parseJWKSet() {
        try {
            return JWKSet.parse(this.key);
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private List<JWK> failover(Exception exception, JWKSelector jwkSelector, C context) throws ConsulKeySourceException {
        if (this.getFailoverJWKSource() == null) {
            return null;
        } else {
            try {
                return this.getFailoverJWKSource().get(jwkSelector, context);
            } catch (KeySourceException e) {
                throw new ConsulKeySourceException(exception.getMessage() + "; Failover JWK source retrieval failed with: " + e.getMessage(), e);
            }
        }
    }

    public JWKSource<C> getFailoverJWKSource() {
        return this.failoverJWKSource;
    }
}
