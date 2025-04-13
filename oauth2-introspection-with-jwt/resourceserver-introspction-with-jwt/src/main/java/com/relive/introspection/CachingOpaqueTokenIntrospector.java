package com.relive.introspection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

/**
 * 缓存支持的 Opaque Token 解析器。
 * 该类首先尝试从缓存中获取解析后的 Opaque Token 信息，
 * 如果缓存中没有，则使用提供的 introspector 进行解析并将结果缓存。
 * 它实现了 OpaqueTokenIntrospector 接口，用于解析传入的 OAuth2 令牌。
 *
 * @author: ReLive
 * @date: 2022/11/20 21:05
 */
@Slf4j
public class CachingOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
    // 用于缓存的实例
    private final Cache cache;

    // 用于执行实际令牌解析的 introspector
    private final OpaqueTokenIntrospector introspector;

    /**
     * 构造函数，初始化缓存和 OpaqueTokenIntrospector。
     *
     * @param cache 缓存实例
     * @param introspector 处理实际令牌解析的 introspector
     */
    public CachingOpaqueTokenIntrospector(Cache cache, OpaqueTokenIntrospector introspector) {
        this.cache = cache;
        this.introspector = introspector;
    }

    /**
     * 尝试从缓存中获取解析后的 Opaque Token 信息，如果缓存中没有，调用 introspector 进行解析。
     *
     * @param token 要解析的 OAuth2 Opaque Token
     * @return 解析后的 OAuth2AuthenticatedPrincipal 对象
     */
    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        try {
            // 尝试从缓存中获取令牌的解析结果
            return this.cache.get(token,
                    () -> this.introspector.introspect(token));
        } catch (Cache.ValueRetrievalException ex) {
            throw new OAuth2IntrospectionException("Did not validate token from cache.");
        } catch (OAuth2IntrospectionException e) {
            // 如果是无效的 Opaque Token 异常，抛出该异常
            if (e instanceof BadOpaqueTokenException) {
                throw (BadOpaqueTokenException) e;
            }
            // 其他 Introspection 异常，抛出处理
            throw new OAuth2IntrospectionException(e.getMessage());
        } catch (Exception ex) {
            log.error("Token introspection failed.", ex);
            throw new OAuth2IntrospectionException("Token introspection failed.");
        }
    }
}
