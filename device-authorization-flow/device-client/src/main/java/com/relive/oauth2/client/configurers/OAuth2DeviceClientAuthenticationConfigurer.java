package com.relive.oauth2.client.configurers;

import com.relive.oauth2.client.OAuth2DeviceAuthenticationFilter;
import com.relive.oauth2.client.OAuth2DeviceAuthorizationRequestFilter;
import com.relive.oauth2.client.authentication.OAuth2DeviceAuthenticationProvider;
import com.relive.oauth2.client.authentication.OAuth2DeviceAuthorizationRequestProvider;
import com.relive.oauth2.client.authentication.OAuth2DeviceCodeAuthenticationProvider;
import com.relive.oauth2.client.endpoint.DefaultDeviceCodeTokenResponseClient;
import com.relive.oauth2.client.endpoint.OAuth2DeviceCodeGrantRequest;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2024/5/2 14:27
 */
public class OAuth2DeviceClientAuthenticationConfigurer extends AbstractHttpConfigurer<OAuth2DeviceClientAuthenticationConfigurer, HttpSecurity> {


    @Override
    public void init(HttpSecurity http) throws Exception {
        OAuth2DeviceAuthorizationRequestProvider deviceAuthorizationRequestProvider = new OAuth2DeviceAuthorizationRequestProvider();
        http.authenticationProvider(this.postProcess(deviceAuthorizationRequestProvider));

        OAuth2AccessTokenResponseClient<OAuth2DeviceCodeGrantRequest> accessTokenResponseClient = new DefaultDeviceCodeTokenResponseClient();
        OAuth2DeviceCodeAuthenticationProvider deviceCodeAuthenticationProvider = new OAuth2DeviceCodeAuthenticationProvider(accessTokenResponseClient);
        http.authenticationProvider(this.postProcess(deviceCodeAuthenticationProvider));

        OAuth2UserService<OAuth2UserRequest, OAuth2User> userService = new DefaultOAuth2UserService();
        OAuth2DeviceAuthenticationProvider deviceAuthenticationProvider = new OAuth2DeviceAuthenticationProvider(accessTokenResponseClient, userService);
        http.authenticationProvider(this.postProcess(deviceAuthenticationProvider));

        super.init(http);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);

        ClientRegistrationRepository clientRegistrationRepository = getClientRegistrationRepository(http);
        OAuth2DeviceAuthorizationRequestFilter deviceAuthorizationRequestFilter = new OAuth2DeviceAuthorizationRequestFilter(authenticationManager, clientRegistrationRepository);

        http.addFilterBefore(this.postProcess(deviceAuthorizationRequestFilter), UsernamePasswordAuthenticationFilter.class);

        OAuth2AuthorizedClientService oAuth2AuthorizedClientService = getOAuth2AuthorizedClientService(http);
        OAuth2DeviceAuthenticationFilter deviceAuthenticationFilter = new OAuth2DeviceAuthenticationFilter(clientRegistrationRepository, oAuth2AuthorizedClientService);
        deviceAuthenticationFilter.setAuthenticationManager(authenticationManager);

        deviceAuthenticationFilter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());

        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler("/success");
        deviceAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);

        http.addFilterBefore(this.postProcess(deviceAuthenticationFilter), UsernamePasswordAuthenticationFilter.class);
        super.configure(http);
    }

    static ClientRegistrationRepository getClientRegistrationRepository(HttpSecurity httpSecurity) {
        return getOptionalBean(httpSecurity, ClientRegistrationRepository.class);
    }

    static OAuth2AuthorizedClientService getOAuth2AuthorizedClientService(HttpSecurity httpSecurity) {
        return getOptionalBean(httpSecurity, OAuth2AuthorizedClientService.class);
    }

    public static <T> T getOptionalBean(HttpSecurity httpSecurity, Class<T> type) {
        Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                httpSecurity.getSharedObject(ApplicationContext.class), type);
        if (beansMap.size() > 1) {
            throw new NoUniqueBeanDefinitionException(type, beansMap.size(),
                    "Expected single matching bean of type '" + type.getName() + "' but found " +
                            beansMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(beansMap.keySet()));
        }
        return (!beansMap.isEmpty() ? beansMap.values().iterator().next() : null);
    }
}
