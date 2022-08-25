package com.relive.jose.jwk.source;

import com.nimbusds.jose.jwk.JWK;

import java.util.Date;

/**
 * @author: ReLive
 * @date: 2022/8/22 21:55
 */
public final class JWKWithTimestamp {
    public final JWK jwk;
    private final Date timestamp;

    public JWKWithTimestamp(JWK jwk) {
        this(jwk, new Date());
    }

    public JWKWithTimestamp(JWK jwk, Date timestamp) {
        if (jwk == null) {
            throw new IllegalArgumentException("The JWK must not be null");
        } else {
            this.jwk = jwk;
            if (timestamp == null) {
                throw new IllegalArgumentException("The timestamp must not null");
            } else {
                this.timestamp = timestamp;
            }
        }
    }

    public JWK getJwk() {
        return jwk;
    }

    public Date getDate() {
        return timestamp;
    }
}
