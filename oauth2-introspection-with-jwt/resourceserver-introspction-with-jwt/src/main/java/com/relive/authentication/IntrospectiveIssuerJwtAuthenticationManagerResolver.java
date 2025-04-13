package com.relive.authentication;

import com.relive.introspection.OAuth2Introspection;
import com.relive.introspection.OAuth2IntrospectionService;
import com.relive.introspection.OpaqueTokenIntrospectorSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据请求中的 Issuer（发行者）动态解析出对应的 AuthenticationManager。
 * <p>
 * 此类结合 introspectionService 以及 opaqueTokenIntrospectorSupport 实现基于不透明令牌的认证支持。
 * 对每个 issuer 只创建一次 AuthenticationManager，并缓存以提高性能。
 * <p>
 * 用于资源服务器在多租户或联邦场景下，根据不同的 issuer 动态支持多个认证中心。
 * <p>
 * 实现原理：
 * - introspectionService 提供 introspection 信息
 * - opaqueTokenIntrospectorSupport 将 introspection 信息转换为 Spring Security 所支持的 OpaqueTokenIntrospector
 * - 最终创建 OpaqueTokenAuthenticationProvider 并作为 AuthenticationManager 使用
 * <p>
 * 使用 ConcurrentHashMap 做缓存，避免重复创建。
 *
 * @author: ReLive
 * @date: 2022/11/20 20:29
 */
@Slf4j
public class IntrospectiveIssuerJwtAuthenticationManagerResolver implements AuthenticationManagerResolver<String> {

    /**
     * issuer 对应的 AuthenticationManager 缓存
     */
    private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();

    /**
     * introspection 元数据加载服务
     */
    private final OAuth2IntrospectionService introspectionService;

    /**
     * introspection 转换器支持类
     */
    private final OpaqueTokenIntrospectorSupport opaqueTokenIntrospectorSupport;

    /**
     * 构造函数，注入 introspectionService 和转换器支持类
     */
    public IntrospectiveIssuerJwtAuthenticationManagerResolver(OAuth2IntrospectionService introspectionService,
                                                               OpaqueTokenIntrospectorSupport opaqueTokenIntrospectorSupport) {
        Assert.notNull(introspectionService, "introspectionService can be not null");
        Assert.notNull(opaqueTokenIntrospectorSupport, "opaqueTokenIntrospectorSupport can be not null");
        this.introspectionService = introspectionService;
        this.opaqueTokenIntrospectorSupport = opaqueTokenIntrospectorSupport;
    }

    /**
     * 根据 issuer 解析出对应的 AuthenticationManager。
     * 如果已缓存则直接返回，否则根据 introspection 信息创建并缓存。
     */
    @Override
    public AuthenticationManager resolve(String issuer) {
        OAuth2Introspection oAuth2Introspection = this.introspectionService.loadIntrospection(issuer);

        if (oAuth2Introspection != null) {
            AuthenticationManager authenticationManager = this.authenticationManagers.computeIfAbsent(issuer,
                    (k) -> {
                        log.debug("Constructing AuthenticationManager");
                        OpaqueTokenIntrospector opaqueTokenIntrospector = this.opaqueTokenIntrospectorSupport.fromOAuth2Introspection(oAuth2Introspection);
                        return new OpaqueTokenAuthenticationProvider(opaqueTokenIntrospector)::authenticate;
                    });
            log.debug(LogMessage.format("Resolved AuthenticationManager for issuer '%s'", issuer).toString());
            return authenticationManager;

        } else {
            log.debug("Did not resolve AuthenticationManager since issuer is not trusted");
        }
        return null;
    }
}
