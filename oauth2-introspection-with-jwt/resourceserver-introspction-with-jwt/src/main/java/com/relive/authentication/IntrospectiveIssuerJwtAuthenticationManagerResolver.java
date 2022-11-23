package com.relive.authentication;

import com.relive.introspection.OAuth2Introspection;
import com.relive.introspection.OAuth2IntrospectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: ReLive
 * @date: 2022/11/20 20:29
 */
@Slf4j
public class IntrospectiveIssuerJwtAuthenticationManagerResolver implements AuthenticationManagerResolver<String> {

    private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();

    private final OAuth2IntrospectionService introspectionService;

    public IntrospectiveIssuerJwtAuthenticationManagerResolver(OAuth2IntrospectionService introspectionService) {
        Assert.notNull(introspectionService, "introspectionService can be not null");
        this.introspectionService = introspectionService;
    }

    @Override
    public AuthenticationManager resolve(String issuer) {
        OAuth2Introspection oAuth2Introspection = this.introspectionService.loadIntrospection(issuer);

        if (oAuth2Introspection != null) {
            AuthenticationManager authenticationManager = this.authenticationManagers.computeIfAbsent(issuer,
                    (k) -> {
                        log.debug("Constructing AuthenticationManager");
                        //TODO 生成OpaqueTokenIntrospector
                        return new OpaqueTokenAuthenticationProvider(null)::authenticate;
                    });
            log.debug(LogMessage.format("Resolved AuthenticationManager for issuer '%s'", issuer).toString());
            return authenticationManager;

        } else {
            log.debug("Did not resolve AuthenticationManager since issuer is not trusted");
        }
        return null;
    }
}
