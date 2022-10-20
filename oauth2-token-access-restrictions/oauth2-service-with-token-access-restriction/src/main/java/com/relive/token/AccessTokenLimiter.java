package com.relive.token;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * AccessToken 限制器接口
 *
 * @author: ReLive
 * @date: 2022/10/9 20:52
 */
@FunctionalInterface
public interface AccessTokenLimiter {

    boolean isAllowed(RegisteredClient registeredClient);
}
