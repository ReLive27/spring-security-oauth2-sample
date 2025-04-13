package com.relive.introspection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;

/**
 * 缓存支持的 OAuth2 Introspection 服务。
 * 该服务首先尝试从缓存中获取 OAuth2 Introspection 信息，
 * 如果缓存中没有，才从外部服务加载并缓存结果。
 * 它实现了 OAuth2IntrospectionService 接口，提供了加载、保存和删除 OAuth2 Introspection 数据的方法。
 *
 * @author: ReLive27
 * @date: 2024/2/27 18:39
 */
@Slf4j
public class CachingOAuth2IntrospectionService implements OAuth2IntrospectionService {

    // 缓存实例，用于存储 OAuth2 Introspection 数据
    private final Cache cache;

    // 外部的 OAuth2 Introspection 服务（可选）
    private OAuth2IntrospectionService introspectionService;

    /**
     * 构造函数，使用缓存实例来初始化 CachingOAuth2IntrospectionService。
     *
     * @param cache 缓存实例
     */
    public CachingOAuth2IntrospectionService(Cache cache) {
        this(cache, null);
    }

    /**
     * 构造函数，使用缓存和外部的 OAuth2 Introspection 服务来初始化 CachingOAuth2IntrospectionService。
     *
     * @param cache                缓存实例
     * @param introspectionService 外部的 OAuth2 Introspection 服务
     */
    public CachingOAuth2IntrospectionService(Cache cache, OAuth2IntrospectionService introspectionService) {
        this.cache = cache;
        this.introspectionService = introspectionService;
    }

    /**
     * 从缓存中加载 OAuth2 Introspection 数据。如果缓存中没有，则调用外部服务加载数据。
     *
     * @param issuer OAuth2 发行者
     * @return 返回 OAuth2 Introspection 数据
     */
    @Override
    public OAuth2Introspection loadIntrospection(String issuer) {
        try {
            // 尝试从缓存中获取 OAuth2 Introspection 数据
            return this.cache.get(issuer,
                    () -> {
                        if (this.introspectionService != null) {
                            // 如果外部服务存在，则通过外部服务加载数据
                            return this.introspectionService.loadIntrospection(issuer);
                        }
                        return null;
                    });
        } catch (Cache.ValueRetrievalException ex) {
            throw new OAuth2IntrospectionException("Can't get OAuth2Introspection from cache.");
        } catch (OAuth2IntrospectionException e) {
            // 如果是无效的 OAuth2 Token 异常，直接抛出
            if (e instanceof BadOpaqueTokenException) {
                throw e;
            }
            // 其他 OAuth2 Introspection 异常，抛出处理
            throw new OAuth2IntrospectionException(e.getMessage());
        } catch (Exception ex) {
            log.error("OAuth2Introspection acquisition failed.", ex);
            throw new OAuth2IntrospectionException("OAuth2Introspection acquisition failed.");
        }
    }

    /**
     * 将 OAuth2 Introspection 数据保存到缓存和外部服务（如果存在）。
     *
     * @param authorizedClient OAuth2 Introspection 数据
     */
    @Override
    public void saveOAuth2Introspection(OAuth2Introspection authorizedClient) {
        // 如果缓存中没有该数据，则将其放入缓存
        this.cache.putIfAbsent(authorizedClient.getIssuer(), authorizedClient);

        // 如果外部服务存在，则同步保存到外部服务
        if (this.introspectionService != null) {
            this.introspectionService.saveOAuth2Introspection(authorizedClient);
        }
    }

    /**
     * 从缓存和外部服务中删除指定的 OAuth2 Introspection 数据。
     *
     * @param issuer OAuth2 发行者
     */
    @Override
    public void removeOAuth2Introspection(String issuer) {
        // 从缓存中移除该数据
        this.cache.evictIfPresent(issuer);

        // 如果外部服务存在，则同步删除外部服务中的数据
        if (this.introspectionService != null) {
            this.introspectionService.removeOAuth2Introspection(issuer);
        }
    }
}
