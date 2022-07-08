package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

/**
 * @author: ReLive
 * @date: 2022/7/8 3:37 下午
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

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

    @Bean
    DefaultOAuth2UserService defaultOAuth2UserService() {
        DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
        defaultOAuth2UserService.setRequestEntityConverter(new Converter<OAuth2UserRequest, RequestEntity<?>>() {
            @Override
            public RequestEntity<?> convert(OAuth2UserRequest userRequest) {
                ClientRegistration clientRegistration = userRequest.getClientRegistration();
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                URI uri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri()).build().toUri();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(userRequest.getAccessToken().getTokenValue());
                RequestEntity request = new RequestEntity(headers, HttpMethod.POST, uri);
                return request;
            }
        });
        return defaultOAuth2UserService;
    }

    @Bean
    GrantedAuthoritiesMapper userAuthoritiesMapper() {
        //角色映射关系，授权服务器ADMIN角色对应客户端OPERATION角色
        Map<String, String> roleMapping = new HashMap<>();
        roleMapping.put("ROLE_ADMIN", "ROLE_OPERATION");
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            authorities.forEach(authority -> {
                if (OAuth2UserAuthority.class.isInstance(authority)) {
                    OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority) authority;
                    Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();
                    List<String> role = (List) userAttributes.get("role");
                    role.stream().map(roleMapping::get)
                            .filter(StringUtils::hasText)
                            .map(SimpleGrantedAuthority::new)
                            .forEach(mappedAuthorities::add);
                }
            });
            return mappedAuthorities;
        };
    }

    @Bean
    public UserDetailsService users() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("SYSTEM")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
