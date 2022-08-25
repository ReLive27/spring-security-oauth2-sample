package com.relive.config;

import com.relive.filter.TokenContextFilter;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter;

/**
 * @author: ReLive
 * @date: 2022/8/21 20:10
 */
public class TokenContextConfigurer<B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<TokenContextConfigurer<B>, B> {

    @Override
    public void configure(B builder) {
        TokenContextFilter tokenContextFilter = new TokenContextFilter();
        builder.addFilterBefore(tokenContextFilter, OAuth2TokenEndpointFilter.class);
    }
}
