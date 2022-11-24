package com.relive.introspection;

import org.springframework.cache.Cache;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

/**
 * @author: ReLive
 * @date: 2022/11/20 21:05
 */
public class CachingOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
    private final Cache cache;

    private final OpaqueTokenIntrospector introspector;

    public CachingOpaqueTokenIntrospector(Cache cache, OpaqueTokenIntrospector introspector) {
        this.cache = cache;
        this.introspector = introspector;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        try {
            return this.cache.get(token,
                    () -> this.introspector.introspect(token));
        } catch (Cache.ValueRetrievalException ex) {
            throw new OAuth2IntrospectionException("Did not validate token from cache.");
        } catch (OAuth2IntrospectionException e) {
            if (e instanceof BadOpaqueTokenException) {
                throw (BadOpaqueTokenException) e;
            }
            throw new OAuth2IntrospectionException(e.getMessage());
        } catch (Exception ex) {
            throw new OAuth2IntrospectionException("Token introspection failed.");
        }
    }
}
