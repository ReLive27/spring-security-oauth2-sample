package com.relive.configure;

import com.relive.authentication.IntrospectiveIssuerJwtAuthenticationManagerResolver;
import com.relive.introspection.CacheOpaqueTokenIntrospectorSupport;
import com.relive.introspection.OAuth2IntrospectionService;
import com.relive.introspection.OpaqueTokenIntrospectorSupport;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;

import java.util.Optional;

/**
 * 用于配置支持 Opaque Token Introspection 的 OAuth2 资源服务器
 * 支持根据 Issuer 动态创建 AuthenticationManager 并通过 introspection 接口验证 token。
 * <p>
 * 可通过 {@link OpaqueTokenIntrospectorSupportConfigurer} 配置缓存与 HTTP 客户端。
 * <p>
 * 示例用法：
 * http.apply(new OAuth2IntrospectiveResourceServerAuthorizationConfigurer())
 * .opaqueTokenIntrospectorSupport()
 * .cache(myCache)
 * .restOperations(myRestOperations);
 *
 * @author: ReLive
 * @date: 2022/11/22 19:25
 */
public class OAuth2IntrospectiveResourceServerAuthorizationConfigurer extends AbstractHttpConfigurer<OAuth2IntrospectiveResourceServerAuthorizationConfigurer, HttpSecurity> {

    /**
     * 用于根据 issuer 解析出对应的 AuthenticationManager
     */
    private AuthenticationManagerResolver<String> authenticationManagerResolver;

    /**
     * 用于构造 OpaqueTokenIntrospectorSupport 的配置器
     */
    private OpaqueTokenIntrospectorSupportConfigurer opaqueTokenIntrospectorSupportConfigurer;

    /**
     * 设置自定义的 AuthenticationManagerResolver
     */
    public OAuth2IntrospectiveResourceServerAuthorizationConfigurer authenticationManagerResolver(AuthenticationManagerResolver<String> authenticationManagerResolver) {
        Assert.notNull(authenticationManagerResolver, "authenticationManagerResolver can not be null");
        this.authenticationManagerResolver = authenticationManagerResolver;
        return this;
    }

    /**
     * 获取 OpaqueTokenIntrospectorSupport 配置器，可用于配置缓存、RestOperations 或自定义实现
     */
    public OpaqueTokenIntrospectorSupportConfigurer opaqueTokenIntrospectorSupport() {
        if (this.opaqueTokenIntrospectorSupportConfigurer == null) {
            this.opaqueTokenIntrospectorSupportConfigurer = new OpaqueTokenIntrospectorSupportConfigurer();
        }
        return this.opaqueTokenIntrospectorSupportConfigurer;
    }

    /**
     * 使用 lambda 自定义方式配置 OpaqueTokenIntrospectorSupport
     */
    public OAuth2IntrospectiveResourceServerAuthorizationConfigurer opaqueTokenIntrospectorSupportConfigurer(Customizer<OpaqueTokenIntrospectorSupportConfigurer> opaqueTokenIntrospectorSupportConfigurerCustomizer) {
        if (this.opaqueTokenIntrospectorSupportConfigurer == null) {
            this.opaqueTokenIntrospectorSupportConfigurer = new OpaqueTokenIntrospectorSupportConfigurer();
        }
        opaqueTokenIntrospectorSupportConfigurerCustomizer.customize(this.opaqueTokenIntrospectorSupportConfigurer);
        return this;
    }

    @Override
    public void init(HttpSecurity http) throws Exception {
        this.validateConfiguration();
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        if (this.authenticationManagerResolver == null) {
            // 获取配置所需的 introspection 元数据服务和 introspector 支持类
            OAuth2IntrospectionService oAuth2IntrospectionService = applicationContext.getBean(OAuth2IntrospectionService.class);
            OpaqueTokenIntrospectorSupport opaqueTokenIntrospectorSupport = this.getOpaqueTokenIntrospectorSupport(applicationContext);

            // 创建自定义 AuthenticationManagerResolver
            IntrospectiveIssuerJwtAuthenticationManagerResolver introspectiveIssuerJwtAuthenticationManagerResolver =
                    new IntrospectiveIssuerJwtAuthenticationManagerResolver(oAuth2IntrospectionService, opaqueTokenIntrospectorSupport);
            this.authenticationManagerResolver = introspectiveIssuerJwtAuthenticationManagerResolver;
        }

        // 将自定义 resolver 包装为 JwtIssuerAuthenticationManagerResolver 以兼容 Spring Security 结构
        JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver =
                new JwtIssuerAuthenticationManagerResolver(this.authenticationManagerResolver);

        // 设置资源服务器使用自定义 authenticationManagerResolver
        http.oauth2ResourceServer(oauth2 -> oauth2
                .authenticationManagerResolver(jwtIssuerAuthenticationManagerResolver)
        );
    }

    /**
     * 获取最终构造的 OpaqueTokenIntrospectorSupport 实例
     */
    private OpaqueTokenIntrospectorSupport getOpaqueTokenIntrospectorSupport(ApplicationContext context) {
        if (this.opaqueTokenIntrospectorSupportConfigurer != null) {
            return this.opaqueTokenIntrospectorSupportConfigurer.getOpaqueTokenIntrospectorSupport(context);
        }
        return null;
    }

    /**
     * 校验配置完整性
     */
    private void validateConfiguration() {
        Assert.state(this.opaqueTokenIntrospectorSupportConfigurer != null, "Make sure to configure OpaqueTokenIntrospectorSupport" +
                "via http.apply(new OAuth2IntrospectiveResourceServerAuthorizationConfigurer()).opaqueTokenIntrospectorSupport().");
    }

    /**
     * 用于构造 OpaqueTokenIntrospectorSupport 的配置器类，可选地设置缓存、RestOperations 或完全替换实现类
     */
    public class OpaqueTokenIntrospectorSupportConfigurer {
        private Cache cache;
        private RestOperations restOperations;
        private OpaqueTokenIntrospectorSupport opaqueTokenIntrospectorSupport;

        /**
         * 设置 introspection 缓存，用于缓存令牌 introspection 结果
         */
        public OpaqueTokenIntrospectorSupportConfigurer cache(Cache cache) {
            Assert.notNull(cache, "cache cannot be null");
            this.cache = cache;
            return this;
        }

        /**
         * 设置用于 introspection 请求的 RestOperations 实现（如 RestTemplate）
         */
        public OpaqueTokenIntrospectorSupportConfigurer restOperations(RestOperations restOperations) {
            Assert.notNull(restOperations, "restOperations cannot be null");
            this.restOperations = restOperations;
            return this;
        }

        /**
         * 完全替换默认的 OpaqueTokenIntrospectorSupport 实现
         */
        public OpaqueTokenIntrospectorSupportConfigurer opaqueTokenIntrospectorSupport(OpaqueTokenIntrospectorSupport opaqueTokenIntrospectorSupport) {
            Assert.notNull(opaqueTokenIntrospectorSupport, "opaqueTokenIntrospectorSupport cannot be null");
            this.opaqueTokenIntrospectorSupport = opaqueTokenIntrospectorSupport;
            return this;
        }

        /**
         * 返回外层配置器
         */
        public OAuth2IntrospectiveResourceServerAuthorizationConfigurer and() {
            return OAuth2IntrospectiveResourceServerAuthorizationConfigurer.this;
        }

        /**
         * 内部方法：根据配置构造最终的 OpaqueTokenIntrospectorSupport 实例
         */
        OpaqueTokenIntrospectorSupport getOpaqueTokenIntrospectorSupport(ApplicationContext context) {
            if (this.opaqueTokenIntrospectorSupport == null) {
                CacheOpaqueTokenIntrospectorSupport opaqueTokenIntrospectorSupport = new CacheOpaqueTokenIntrospectorSupport();

                // 尝试从容器中获取 CacheManager 并创建默认缓存
                if (context.getBeanNamesForType(CacheManager.class).length > 0) {
                    this.cache = context.getBean(CacheManager.class).getCache("oauth2:introspective");
                    if (this.cache == null) {
                        throw new IllegalStateException("The CacheManager should be set to allow lazy creation of cache instances");
                    }
                }

                // 注入缓存与 RestOperations
                Optional.ofNullable(this.cache).ifPresent(opaqueTokenIntrospectorSupport::setCache);
                Optional.ofNullable(this.restOperations).ifPresent(opaqueTokenIntrospectorSupport::setRestOperations);

                return opaqueTokenIntrospectorSupport;
            }

            return this.opaqueTokenIntrospectorSupport;
        }
    }
}
