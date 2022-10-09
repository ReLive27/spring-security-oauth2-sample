package com.relive.token;

/**
 * @author: ReLive
 * @date: 2022/10/9 21:07
 */
public class RedisAccessTokenLimiter implements AccessTokenLimiter{


    @Override
    public boolean requiresGenerateToken(String clientId) {
        //TODO redis实现访问限制
        return false;
    }
}
