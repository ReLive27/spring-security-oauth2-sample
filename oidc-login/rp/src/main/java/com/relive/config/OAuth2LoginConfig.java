package com.relive.config;

import com.relive.repository.OAuth2ClientRoleRepository;
import com.relive.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author: ReLive
 * @date: 2022/6/24 4:02 下午
 */
@Configuration(proxyBeanMethods = false)
public class OAuth2LoginConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin(from -> {
                    from.defaultSuccessUrl("/home");
                })
                .oauth2Login(Customizer.withDefaults())
                .csrf().disable();
        return http.build();
    }

    /**
     * OIDC 用户信息服务扩展，实现角色映射
     *
     * @return
     */
    @Bean
    OAuth2UserService<OidcUserRequest, OidcUser> oidcRoleMappingUserService(OAuth2ClientRoleRepository oAuth2ClientRoleRepository) {
        return new OidcRoleMappingUserService(oAuth2ClientRoleRepository);
    }

    /**
     * 定义JDBC 客户端注册存储库
     *
     * @param jdbcTemplate
     * @return
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcClientRegistrationRepository(jdbcTemplate);
    }

    /**
     * 负责{@link org.springframework.security.oauth2.client.OAuth2AuthorizedClient}在 Web 请求之间进行持久化
     *
     * @param jdbcTemplate
     * @param clientRegistrationRepository
     * @return
     */
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            JdbcTemplate jdbcTemplate,
            ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }

    /**
     * OAuth2AuthorizedClientRepository 是一个容器类，用于在请求之间保存和持久化授权客户端
     *
     * @param authorizedClientService
     * @return
     */
    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

    /**
     * 加载用户数据接口的JDBC实现
     *
     * @return
     * @see org.springframework.security.authentication.dao.DaoAuthenticationProvider
     */
    @Bean
    UserDetailsService jdbcUserDetailsService(UserRepository userRepository) {
        return new JdbcUserDetailsService(userRepository);
    }
}
