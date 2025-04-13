package com.relive.introspection;

import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

/**
 * 提供对 OAuth2 Introspection 数据的支持接口。
 * 该接口用于将 OAuth2 Introspection 数据转换为对应的 OpaqueTokenIntrospector 对象，
 * 以便进行后续的 OAuth2 令牌内省操作。
 *
 * @author: ReLive
 * @date: 2022/11/24 21:03
 */
public interface OpaqueTokenIntrospectorSupport {

    /**
     * 根据提供的 OAuth2 Introspection 数据，返回一个对应的 OpaqueTokenIntrospector 对象。
     *
     * @param oAuth2Introspection OAuth2 Introspection 数据
     * @return 返回一个 OpaqueTokenIntrospector 对象，用于执行 OAuth2 令牌内省
     */
    OpaqueTokenIntrospector fromOAuth2Introspection(OAuth2Introspection oAuth2Introspection);
}
