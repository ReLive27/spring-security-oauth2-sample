package com.relive.jose;

import com.nimbusds.jose.jwk.JWK;

/**
 * kid generation strategy.
 *
 * @author: ReLive
 * @date: 2022/8/23 13:07
 * @see JWK
 */
@FunctionalInterface
public interface KeyIDStrategy {

    String generateKeyID();
}
