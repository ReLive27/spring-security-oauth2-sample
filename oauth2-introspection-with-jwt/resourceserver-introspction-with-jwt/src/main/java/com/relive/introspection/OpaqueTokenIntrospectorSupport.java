package com.relive.introspection;

import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

/**
 * @author: ReLive
 * @date: 2022/11/24 21:03
 */
public interface OpaqueTokenIntrospectorSupport {

    OpaqueTokenIntrospector fromOAuth2Introspection(OAuth2Introspection oAuth2Introspection);
}
