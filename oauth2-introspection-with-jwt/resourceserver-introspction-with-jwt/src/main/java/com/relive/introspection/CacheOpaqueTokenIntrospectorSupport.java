package com.relive.introspection;

import org.springframework.cache.Cache;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * @author: ReLive
 * @date: 2022/11/24 21:45
 */
public class CacheOpaqueTokenIntrospectorSupport implements OpaqueTokenIntrospectorSupport {

    private Cache cache;

    private RestOperations restOperations;

    @Override
    public OpaqueTokenIntrospector fromOAuth2Introspection(OAuth2Introspection oAuth2Introspection) {
        OpaqueTokenIntrospector opaqueTokenIntrospector = this.createNimbusOpaqueTokenIntrospector(oAuth2Introspection);
        if (this.cache == null) {
            return opaqueTokenIntrospector;
        }

        return new CachingOpaqueTokenIntrospector(cache, opaqueTokenIntrospector);
    }

    private OpaqueTokenIntrospector createNimbusOpaqueTokenIntrospector(OAuth2Introspection oAuth2Introspection) {
        if (this.restOperations == null) {
            return new NimbusOpaqueTokenIntrospector(oAuth2Introspection.getIntrospectionUri(),
                    oAuth2Introspection.getClientId(), oAuth2Introspection.getClientSecret());
        }
        RestTemplate restTemplate = (RestTemplate) this.restOperations;
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(oAuth2Introspection.getClientId(), oAuth2Introspection.getClientSecret()));
        return new NimbusOpaqueTokenIntrospector(oAuth2Introspection.getIntrospectionUri(), this.restOperations);
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public void setRestOperations(RestOperations restOperations) {
        this.restOperations = restOperations;
    }
}
