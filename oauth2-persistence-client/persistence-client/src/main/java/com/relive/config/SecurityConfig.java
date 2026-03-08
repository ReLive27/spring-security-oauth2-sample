package com.relive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * OAuth2 客户端安全配置类（持久化客户端示例）
 *
 * <p>
 * 本配置类演示如何使用 JDBC 持久化存储客户端注册信息和授权客户端信息。
 * 与内存存储不同，JDBC 存储确保客户端信息在服务器重启后不会丢失。
 *
 * <p>
 * 主要配置：
 * <ul>
 *     <li>使用 JDBC 存储客户端注册信息（ClientRegistrationRepository）</li>
 *     <li>使用 JDBC 存储授权客户端信息（OAuth2AuthorizedClientService）</li>
 *     <li>配置 WebClient 支持 OAuth2 认证</li>
 *     <li>配置授权客户端管理器</li>
 * </ul>
 *
 * @author ReLive
 * @date 2022/6/27 2:23 下午
 */
@Configuration
@EnableWebSecurity(debug = false)
public class SecurityConfig {


    /**
     * 配置安全过滤器链
     *
     * <p>
     * 设置所有请求需要认证，启用表单登录和 OAuth2 客户端功能。
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                .formLogin(login -> {
                    login.loginPage("/login").permitAll();
                })
                .oauth2Client(withDefaults());
        return http.build();
    }

    /**
     * 配置用户详情服务
     *
     * @return UserDetailsService 用户详情服务
     */
    @Bean
    UserDetailsService users() {
        UserDetails user = User.withUsername("admin")
                .password("{noop}password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * 配置 JDBC 客户端注册存储库
     *
     * <p>
     * 使用数据库持久化存储 OAuth2 客户端注册信息，
     * 使得客户端配置在服务器重启后仍然保留。
     *
     * @param jdbcTemplate JDBC 模板
     * @return ClientRegistrationRepository 客户端注册存储库
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcClientRegistrationRepository(jdbcTemplate);
    }

    /**
     * 配置 JDBC 授权客户端服务
     *
     * <p>
     * 使用数据库持久化存储授权客户端信息（访问令牌、刷新令牌等），
     * 使得用户的授权状态在服务器重启后仍然保留。
     *
     * @param jdbcTemplate               JDBC 模板
     * @param clientRegistrationRepository 客户端注册存储库
     * @return OAuth2AuthorizedClientService 授权客户端服务
     */
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            JdbcTemplate jdbcTemplate,
            ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }

    /**
     * 配置授权客户端存储库
     *
     * <p>
     * 使用基于已认证主体的授权客户端存储库，将授权信息关联到当前用户。
     *
     * @param authorizedClientService 授权客户端服务
     * @return OAuth2AuthorizedClientRepository 授权客户端存储库
     */
    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

    /**
     * 配置支持 OAuth2 的 WebClient
     *
     * <p>
     * 创建自动附加访问令牌的 WebClient，用于向受保护的资源服务器发起请求。
     *
     * @param authorizedClientManager 授权客户端管理器
     * @return WebClient Web 客户端
     */
    @Bean
    WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        return WebClient.builder()
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }

    /**
     * 配置授权客户端管理器
     *
     * <p>
     * 配置支持授权码和刷新令牌流程的客户端提供程序。
     *
     * @param clientRegistrationRepository  客户端注册存储库
     * @param authorizedClientRepository    授权客户端存储库
     * @return OAuth2AuthorizedClientManager 授权客户端管理器
     */
    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                          OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder
                .builder()
                .authorizationCode()
                .refreshToken()
                .build();
        DefaultOAuth2AuthorizedClientManager authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

}
