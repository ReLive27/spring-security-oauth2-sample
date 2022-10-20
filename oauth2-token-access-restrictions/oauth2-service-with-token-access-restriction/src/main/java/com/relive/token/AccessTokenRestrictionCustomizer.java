package com.relive.token;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author: ReLive
 * @date: 2022/10/8 21:01
 */
public class AccessTokenRestrictionCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    private static final String DEFAULT_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1";
    private final AccessTokenLimiter tokenLimiter;

    public AccessTokenRestrictionCustomizer(AccessTokenLimiter tokenLimiter) {
        Assert.notNull(tokenLimiter, "accessTokenLimiter can not be null");
        this.tokenLimiter = tokenLimiter;
    }

    /**
     * 通过{@link AccessTokenLimiter} 为OAuth2 客户端模式访问令牌添加访问限制
     *
     * @param context
     */
    @Override
    public void customize(JwtEncodingContext context) {
        if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType())) {
            RegisteredClient registeredClient = context.getRegisteredClient();
            if (registeredClient == null) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "OAuth 2.0 Parameter: " + OAuth2ParameterNames.CLIENT_ID, DEFAULT_ERROR_URI);
                throw new OAuth2AuthenticationException(error);
            }


            boolean requiresGenerateToken = this.tokenLimiter.isAllowed(registeredClient);
            if (!requiresGenerateToken) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED,
                        "The token generation fails, and the same client is prohibited from repeatedly obtaining the token within a short period of time.", null);
                throw new OAuth2AuthenticationException(error);
            }
        }

    }
}
