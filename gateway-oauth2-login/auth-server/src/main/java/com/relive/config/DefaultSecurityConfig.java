package com.relive.config;

import com.relive.repository.JdbcClientRegistrationRepository;
import com.relive.repository.OAuth2ClientRoleRepository;
import com.relive.repository.UserRepository;
import com.relive.service.AuthorityMappingOAuth2UserService;
import com.relive.service.JdbcUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 默认的 Spring Security Web 安全配置类，负责配置 HTTP 安全策略、
 * 表单登录、OAuth2 登录、用户信息加载、以及客户端授权信息的持久化。
 * <p>
 * 本配置类将 Spring Security 与 OAuth2 Client 机制结合，并通过 JDBC 实现持久化。
 * </p>
 *
 * @author ReLive
 * @date 2022/6/23
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class DefaultSecurityConfig {

    @Autowired
    UserRepositoryOAuth2UserHandler userHandler;

    /**
     * 配置 Spring Security 的安全过滤链。
     * <p>
     * - 所有请求需要认证
     * - 启用表单登录
     * - 启用 OAuth2 登录，并设置自定义认证成功处理器
     * </p>
     *
     * @param http HttpSecurity 实例
     * @return SecurityFilterChain
     * @throws Exception 配置失败时抛出
     */
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                .formLogin(withDefaults())
                .oauth2Login(oauth2login -> {
                    SavedUserAuthenticationSuccessHandler successHandler = new SavedUserAuthenticationSuccessHandler();
                    successHandler.setOauth2UserHandler(userHandler);
                    oauth2login.successHandler(successHandler);
                });
        return http.build();
    }

    /**
     * 注册自定义的 UserDetailsService，实现从数据库加载表单登录用户信息。
     *
     * @param userRepository 用户信息仓库接口
     * @return UserDetailsService 实例
     */
    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return new JdbcUserDetailsService(userRepository);
    }

    /**
     * 注册扩展的 OAuth2UserService，在用户 OAuth2 登录成功后映射权限信息。
     * <p>
     * 用于将外部登录的用户（如 GitHub）权限映射为本地系统角色。
     * </p>
     *
     * @param oAuth2ClientRoleRepository OAuth2 客户端角色仓库
     * @return OAuth2UserService 实例
     */
    @Bean
    OAuth2UserService<OAuth2UserRequest, OAuth2User> auth2UserService(OAuth2ClientRoleRepository oAuth2ClientRoleRepository) {
        return new AuthorityMappingOAuth2UserService(oAuth2ClientRoleRepository);
    }

    /**
     * 配置客户端注册信息，并持久化到数据库。
     * <p>
     * 示例中手动注册了 GitHub 客户端信息，后续可拓展为从配置文件或管理界面动态注册。
     * </p>
     *
     * @param jdbcTemplate Spring JDBC 操作对象
     * @return ClientRegistrationRepository 客户端注册信息仓库
     */
    @Bean
    ClientRegistrationRepository clientRegistrationRepository(JdbcTemplate jdbcTemplate) {
        JdbcClientRegistrationRepository jdbcClientRegistrationRepository = new JdbcClientRegistrationRepository(jdbcTemplate);
        // 示例：请替换为实际申请的 GitHub clientId 和 clientSecret
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("github")
                .clientId("123456")
                .clientSecret("123456")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .scope("read:user")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("login")
                .clientName("GitHub")
                .build();

        jdbcClientRegistrationRepository.save(clientRegistration);
        return jdbcClientRegistrationRepository;
    }

    /**
     * 用于管理 OAuth2 客户端授权信息的服务，负责持久化 Access Token、Refresh Token 等信息。
     *
     * @param jdbcTemplate JDBC 工具类
     * @param clientRegistrationRepository 客户端注册仓库
     * @return OAuth2AuthorizedClientService 实例
     */
    @Bean
    OAuth2AuthorizedClientService authorizedClientService(
            JdbcTemplate jdbcTemplate,
            ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }

    /**
     * 配置 OAuth2 客户端授权信息的存储方式，结合 Spring Security 的认证主体进行持久化存储。
     *
     * @param authorizedClientService 客户端授权服务
     * @return OAuth2AuthorizedClientRepository 实例
     */
    @Bean
    OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }
}
