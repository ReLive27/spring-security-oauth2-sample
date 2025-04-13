package com.relive.introspection;

import org.springframework.cache.Cache;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * 支持缓存的 Opaque Token introspector 实现。
 * 该类使用了缓存（如果提供）来缓存 Opaque Token 的 introspection 结果，
 * 并结合 Nimbus 的 Opaque Token introspector 实现 OAuth2 令牌验证。
 *
 * @author: ReLive
 * @date: 2022/11/24 21:45
 */
public class CacheOpaqueTokenIntrospectorSupport implements OpaqueTokenIntrospectorSupport {

    // 缓存实例，用于存储 introspection 结果
    private Cache cache;

    // 用于 HTTP 请求的 RestOperations 实例
    private RestOperations restOperations;

    /**
     * 根据 OAuth2 introspection 信息创建 OpaqueTokenIntrospector 实例。
     * 如果缓存不为空，则返回一个支持缓存的 OpaqueTokenIntrospector。
     *
     * @param oAuth2Introspection OAuth2 introspection 配置对象
     * @return 返回 OpaqueTokenIntrospector 实例
     */
    @Override
    public OpaqueTokenIntrospector fromOAuth2Introspection(OAuth2Introspection oAuth2Introspection) {
        // 创建一个基本的 OpaqueTokenIntrospector
        OpaqueTokenIntrospector opaqueTokenIntrospector = this.createNimbusOpaqueTokenIntrospector(oAuth2Introspection);

        // 如果没有配置缓存，直接返回基本的 introspector
        if (this.cache == null) {
            return opaqueTokenIntrospector;
        }

        // 使用缓存包装 OpaqueTokenIntrospector
        return new CachingOpaqueTokenIntrospector(cache, opaqueTokenIntrospector);
    }

    /**
     * 创建一个 Nimbus Opaque Token Introspector 实例。
     *
     * @param oAuth2Introspection OAuth2 introspection 配置对象
     * @return 返回 NimbusOpaqueTokenIntrospector 实例
     */
    private OpaqueTokenIntrospector createNimbusOpaqueTokenIntrospector(OAuth2Introspection oAuth2Introspection) {
        // 如果没有提供 RestOperations，使用默认构造器创建 NimbusOpaqueTokenIntrospector
        if (this.restOperations == null) {
            return new NimbusOpaqueTokenIntrospector(oAuth2Introspection.getIntrospectionUri(),
                    oAuth2Introspection.getClientId(), oAuth2Introspection.getClientSecret());
        }

        // 如果提供了 RestOperations，设置 HTTP 请求的认证信息
        RestTemplate restTemplate = (RestTemplate) this.restOperations;
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(oAuth2Introspection.getClientId(), oAuth2Introspection.getClientSecret()));

        // 返回带有 RestOperations 的 NimbusOpaqueTokenIntrospector
        return new NimbusOpaqueTokenIntrospector(oAuth2Introspection.getIntrospectionUri(), this.restOperations);
    }

    /**
     * 设置缓存实例。
     *
     * @param cache 缓存实例
     */
    public void setCache(Cache cache) {
        this.cache = cache;
    }

    /**
     * 设置 RestOperations 实例。
     *
     * @param restOperations RestOperations 实例，用于自定义 HTTP 请求
     */
    public void setRestOperations(RestOperations restOperations) {
        this.restOperations = restOperations;
    }
}
