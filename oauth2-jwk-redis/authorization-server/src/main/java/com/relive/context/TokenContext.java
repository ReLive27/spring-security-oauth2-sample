package com.relive.context;

import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.util.Assert;

/**
 * 包含token信息的上下文。
 *
 * @author: ReLive
 * @date: 2022/8/21 19:40
 */
public class TokenContext {
    private final TokenSettings tokenSettings;

    public TokenContext(TokenSettings tokenSettings){
        Assert.notNull(tokenSettings,"tokenSettings cannot be null");
        this.tokenSettings=tokenSettings;
    }

    public TokenSettings getTokenSettings(){
        return this.tokenSettings;
    }
}
