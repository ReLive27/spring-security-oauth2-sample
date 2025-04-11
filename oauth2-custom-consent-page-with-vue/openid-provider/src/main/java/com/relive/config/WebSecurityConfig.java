package com.relive.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.relive.authentication.Http200LogoutSuccessHandler;
import com.relive.authentication.Http401UnauthorizedEntryPoint;
import com.relive.authentication.JwtAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;

/**
 * Web 安全配置类
 * <p>
 * 配置认证方式、授权规则、OAuth2 登录、JWT 资源服务器等相关内容
 * </p>
 *
 * @author ReLive
 * @date 2023/3/14 19:18
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class WebSecurityConfig {

    // 注入 JWK 源，用于生成 JWT Token
    @Autowired
    JWKSource<SecurityContext> jwkSource;

    /**
     * 安全过滤链配置
     *
     * @param http HttpSecurity 对象
     * @return SecurityFilterChain 实例
     * @throws Exception 异常抛出
     */
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 所有请求都需要认证
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                // 表单登录配置
                .formLogin()
                .successHandler(new JwtAuthenticationSuccessHandler(jwkSource)) // 登录成功后生成 JWT Token
                .failureHandler(new AuthenticationEntryPointFailureHandler(new Http401UnauthorizedEntryPoint())) // 登录失败处理器
                .and()
                // 注销登录配置
                .logout()
                .logoutSuccessHandler(new Http200LogoutSuccessHandler()) // 注销成功返回 200
                .and()
                // 配置 OAuth2 资源服务器，启用 JWT 支持
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                // 配置 OAuth2 登录（如 GitHub 登录）
                .oauth2Login(oauth2Login -> oauth2Login.successHandler(new JwtAuthenticationSuccessHandler(jwkSource)))
                // 关闭会话状态（无状态）
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 关闭 CSRF 防护（REST API 推荐禁用）
                .csrf().disable()
                // 自定义未认证处理器（返回 401）
                .exceptionHandling().authenticationEntryPoint(new Http401UnauthorizedEntryPoint());

        return http.build();
    }

    /**
     * 配置 OAuth2 客户端注册信息（此处以 GitHub 为例）
     *
     * @return 客户端注册仓库
     */
    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        // 注意：请替换为你自己申请的 GitHub clientId 和 clientSecret
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("github")
                .clientId("123456") // GitHub 应用的 clientId
                .clientSecret("123456") // GitHub 应用的 clientSecret
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // 客户端认证方式
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // 授权码模式
                .redirectUri("http://localhost:9528/oauth2/callback") // 重定向 URI
                .scope(new String[]{"read:user"}) // 申请的权限范围
                .authorizationUri("https://github.com/login/oauth/authorize") // 授权地址
                .tokenUri("https://github.com/login/oauth/access_token") // 令牌获取地址
                .userInfoUri("https://api.github.com/user") // 用户信息地址
                .userNameAttributeName("login") // 用户名字段
                .clientName("GitHub") // 客户端名称
                .build();

        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    /**
     * 配置内存中的用户信息（用于表单登录）
     *
     * @return 用户服务实例
     */
    @Bean
    UserDetailsService users() {
        UserDetails user = User.withUsername("admin")
                .password("{noop}111111") // 明文密码（仅适用于测试）
                .roles("ADMIN") // 角色
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
