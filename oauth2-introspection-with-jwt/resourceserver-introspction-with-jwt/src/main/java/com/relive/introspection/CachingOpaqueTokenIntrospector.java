package com.relive.introspection;

import lombok.SneakyThrows;
import org.springframework.cache.Cache;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.io.IOException;

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

    @SneakyThrows
    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        try {
            return this.cache.get(token,
                    () -> this.introspector.introspect(token));
        } catch (Cache.ValueRetrievalException ex) {
            Throwable thrownByValueLoader = ex.getCause();
            if (thrownByValueLoader instanceof IOException) {
                throw (IOException) thrownByValueLoader;
            }
            throw new IOException(thrownByValueLoader);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
}
