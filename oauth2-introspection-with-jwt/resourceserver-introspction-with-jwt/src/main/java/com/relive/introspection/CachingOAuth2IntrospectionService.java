package com.relive.introspection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;

/**
 * @author: ReLive27
 * @date: 2024/2/27 18:39
 */
@Slf4j
public class CachingOAuth2IntrospectionService implements OAuth2IntrospectionService {
    private final Cache cache;
    private OAuth2IntrospectionService introspectionService;

    public CachingOAuth2IntrospectionService(Cache cache) {
        this(cache, null);
    }

    public CachingOAuth2IntrospectionService(Cache cache, OAuth2IntrospectionService introspectionService) {
        this.cache = cache;
        this.introspectionService = introspectionService;
    }

    @Override
    public OAuth2Introspection loadIntrospection(String issuer) {
        try {
            return this.cache.get(issuer,
                    () -> {
                        if (this.introspectionService != null) {
                            return this.introspectionService.loadIntrospection(issuer);
                        }
                        return null;
                    });
        } catch (Cache.ValueRetrievalException ex) {
            throw new OAuth2IntrospectionException("Can't get OAuth2Introspection from cache.");
        } catch (OAuth2IntrospectionException e) {
            if (e instanceof BadOpaqueTokenException) {
                throw e;
            }
            throw new OAuth2IntrospectionException(e.getMessage());
        } catch (Exception ex) {
            log.error("OAuth2Introspection acquisition failed.", ex);
            throw new OAuth2IntrospectionException("OAuth2Introspection acquisition failed.");
        }
    }

    @Override
    public void saveOAuth2Introspection(OAuth2Introspection authorizedClient) {
        this.cache.putIfAbsent(authorizedClient.getIssuer(), authorizedClient);
        if (this.introspectionService != null) {
            this.introspectionService.saveOAuth2Introspection(authorizedClient);
        }
    }

    @Override
    public void removeOAuth2Introspection(String issuer) {
        this.cache.evictIfPresent(issuer);
        if (this.introspectionService != null) {
            this.introspectionService.removeOAuth2Introspection(issuer);
        }
    }
}
