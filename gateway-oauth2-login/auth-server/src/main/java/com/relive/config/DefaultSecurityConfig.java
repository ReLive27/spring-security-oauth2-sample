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
 * Default Spring Web Security Configuration
 *
 * @author: ReLive
 * @date: 2022/6/23 7:26 下午
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class DefaultSecurityConfig {

    @Autowired
    UserRepositoryOAuth2UserHandler userHandler;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests ->
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
     * User information container class, used to obtain user information during Form authentication.
     *
     * @param userRepository
     * @return
     */
    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return new JdbcUserDetailsService(userRepository);
    }

    /**
     * Extended OAuth2 login mapping permission information.
     *
     * @param oAuth2ClientRoleRepository
     * @return
     */
    @Bean
    OAuth2UserService<OAuth2UserRequest, OAuth2User> auth2UserService(OAuth2ClientRoleRepository oAuth2ClientRoleRepository) {
        return new AuthorityMappingOAuth2UserService(oAuth2ClientRoleRepository);
    }

    /**
     * Persistent GitHub Client.
     *
     * @param jdbcTemplate
     * @return
     */
    @Bean
    ClientRegistrationRepository clientRegistrationRepository(JdbcTemplate jdbcTemplate) {
        JdbcClientRegistrationRepository jdbcClientRegistrationRepository = new JdbcClientRegistrationRepository(jdbcTemplate);
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("github")
                .clientId("123456")
                .clientSecret("123456")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .scope(new String[]{"read:user"})
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("login")
                .clientName("GitHub").build();

        jdbcClientRegistrationRepository.save(clientRegistration);
        return jdbcClientRegistrationRepository;
    }

    /**
     * Responsible for OAuth2AuthorizedClient persistence between web requests.
     *
     * @param jdbcTemplate
     * @param clientRegistrationRepository
     * @return
     */
    @Bean
    OAuth2AuthorizedClientService authorizedClientService(
            JdbcTemplate jdbcTemplate,
            ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }

    /**
     * Used to save and persist authorized clients between requests.
     *
     * @param authorizedClientService
     * @return
     */
    @Bean
    OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }
}
