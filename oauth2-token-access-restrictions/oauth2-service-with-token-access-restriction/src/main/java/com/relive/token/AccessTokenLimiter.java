package com.relive.token;

/**
 * @author: ReLive
 * @date: 2022/10/9 20:52
 */
@FunctionalInterface
public interface AccessTokenLimiter {

    boolean requiresGenerateToken(String clientId);
}
