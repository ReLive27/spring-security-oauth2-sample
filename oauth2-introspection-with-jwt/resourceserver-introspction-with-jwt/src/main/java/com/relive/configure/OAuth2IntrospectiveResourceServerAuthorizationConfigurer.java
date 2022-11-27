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
 * @author: ReLive
 * @date: 2022/11/22 19:25
 */
public class OAuth2IntrospectiveResourceServerAuthorizationConfigurer extends AbstractHttpConfigurer<OAuth2IntrospectiveResourceServerAuthorizationConfigurer, HttpSecurity> {

    private AuthenticationManagerResolver<String> authenticationManagerResolver;
    private OpaqueTokenIntrospectorSupportConfigurer opaqueTokenIntrospectorSupportConfigurer;


    public OAuth2IntrospectiveResourceServerAuthorizationConfigurer authenticationManagerResolver(AuthenticationManagerResolver<String> authenticationManagerResolver) {
        Assert.notNull(authenticationManagerResolver, "authenticationManagerResolver can not be null");
        this.authenticationManagerResolver = authenticationManagerResolver;
        return this;
    }

    public OpaqueTokenIntrospectorSupportConfigurer opaqueTokenIntrospectorSupport() {
        if (this.opaqueTokenIntrospectorSupportConfigurer == null) {
            this.opaqueTokenIntrospectorSupportConfigurer = new OpaqueTokenIntrospectorSupportConfigurer();
        }

        return this.opaqueTokenIntrospectorSupportConfigurer;
    }

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
            OAuth2IntrospectionService oAuth2IntrospectionService = applicationContext.getBean(OAuth2IntrospectionService.class);
            OpaqueTokenIntrospectorSupport opaqueTokenIntrospectorSupport = this.getOpaqueTokenIntrospectorSupport(applicationContext);

            IntrospectiveIssuerJwtAuthenticationManagerResolver introspectiveIssuerJwtAuthenticationManagerResolver =
                    new IntrospectiveIssuerJwtAuthenticationManagerResolver(oAuth2IntrospectionService, opaqueTokenIntrospectorSupport);
            this.authenticationManagerResolver = introspectiveIssuerJwtAuthenticationManagerResolver;
        }
        JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver =
                new JwtIssuerAuthenticationManagerResolver(this.authenticationManagerResolver);
        http.oauth2ResourceServer(oauth2 -> oauth2
                .authenticationManagerResolver(jwtIssuerAuthenticationManagerResolver)
        );
    }

    private OpaqueTokenIntrospectorSupport getOpaqueTokenIntrospectorSupport(ApplicationContext context) {
        if (this.opaqueTokenIntrospectorSupportConfigurer != null) {
            return this.opaqueTokenIntrospectorSupportConfigurer.getOpaqueTokenIntrospectorSupport(context);
        }

        return null;
    }

    private void validateConfiguration() {
        Assert.state(this.opaqueTokenIntrospectorSupportConfigurer != null, "Make sure to configure OpaqueTokenIntrospectorSupport" +
                "via http.apply(new OAuth2IntrospectiveResourceServerAuthorizationConfigurer()).opaqueTokenIntrospectorSupport().");
    }


    public class OpaqueTokenIntrospectorSupportConfigurer {
        private Cache cache;
        private RestOperations restOperations;
        private OpaqueTokenIntrospectorSupport opaqueTokenIntrospectorSupport;

        public OpaqueTokenIntrospectorSupportConfigurer cache(Cache cache) {
            Assert.notNull(cache, "cache cannot be null");
            this.cache = cache;
            return this;
        }

        public OpaqueTokenIntrospectorSupportConfigurer restOperations(RestOperations restOperations) {
            Assert.notNull(restOperations, "restOperations cannot be null");
            this.restOperations = restOperations;
            return this;
        }

        public OpaqueTokenIntrospectorSupportConfigurer opaqueTokenIntrospectorSupport(OpaqueTokenIntrospectorSupport opaqueTokenIntrospectorSupport) {
            Assert.notNull(opaqueTokenIntrospectorSupport, "opaqueTokenIntrospectorSupport cannot be null");
            this.opaqueTokenIntrospectorSupport = opaqueTokenIntrospectorSupport;
            return this;
        }

        public OAuth2IntrospectiveResourceServerAuthorizationConfigurer and() {
            return OAuth2IntrospectiveResourceServerAuthorizationConfigurer.this;
        }

        OpaqueTokenIntrospectorSupport getOpaqueTokenIntrospectorSupport(ApplicationContext context) {
            if (this.opaqueTokenIntrospectorSupport == null) {
                CacheOpaqueTokenIntrospectorSupport opaqueTokenIntrospectorSupport = new CacheOpaqueTokenIntrospectorSupport();
                if (context.getBeanNamesForType(CacheManager.class).length > 0) {
                    this.cache = context.getBean(CacheManager.class).getCache("oauth2:introspective");
                }
                Optional.ofNullable(this.cache).ifPresent(opaqueTokenIntrospectorSupport::setCache);
                Optional.ofNullable(this.restOperations).ifPresent(opaqueTokenIntrospectorSupport::setRestOperations);
                return opaqueTokenIntrospectorSupport;
            }

            return this.opaqueTokenIntrospectorSupport;
        }
    }
}
