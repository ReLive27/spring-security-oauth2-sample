package com.relive.config;

import com.relive.authentication.DeviceClientAuthenticationConverter;
import com.relive.authentication.DeviceClientAuthenticationProvider;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2024/1/25 16:01
 */
public class DeviceClientAuthenticationConfigurer extends AbstractHttpConfigurer<DeviceClientAuthenticationConfigurer, HttpSecurity> {

    private AuthenticationConverter deviceClientAuthenticationConverter;

    private AuthenticationProvider deviceClientAuthenticationProvider;


    public DeviceClientAuthenticationConfigurer deviceClientAuthenticationConverter(AuthenticationConverter deviceClientAuthenticationConverter) {
        Assert.notNull(deviceClientAuthenticationConverter, "deviceClientAuthenticationConverter can not be null");
        this.deviceClientAuthenticationConverter = deviceClientAuthenticationConverter;
        return this;
    }

    public DeviceClientAuthenticationConfigurer deviceClientAuthenticationProvider(AuthenticationProvider deviceClientAuthenticationProvider) {
        Assert.notNull(deviceClientAuthenticationProvider, "deviceClientAuthenticationProvider can not be null");
        this.deviceClientAuthenticationProvider = deviceClientAuthenticationProvider;
        return this;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        AuthorizationServerSettings authorizationServerSettings = getAuthorizationServerSettings(http);

        if (this.deviceClientAuthenticationConverter == null) {
            this.deviceClientAuthenticationConverter = new DeviceClientAuthenticationConverter(
                    authorizationServerSettings.getDeviceAuthorizationEndpoint());
        }

        if (this.deviceClientAuthenticationProvider == null) {
            RegisteredClientRepository registeredClientRepository = getRegisteredClientRepository(http);
            this.deviceClientAuthenticationProvider = new DeviceClientAuthenticationProvider(registeredClientRepository);
        }

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .clientAuthentication(clientAuthentication ->
                        clientAuthentication
                                .authenticationConverter(deviceClientAuthenticationConverter)
                                .authenticationProvider(deviceClientAuthenticationProvider)
                );
    }

    private static AuthorizationServerSettings getAuthorizationServerSettings(HttpSecurity httpSecurity) {
        AuthorizationServerSettings authorizationServerSettings = httpSecurity.getSharedObject(AuthorizationServerSettings.class);
        if (authorizationServerSettings == null) {
            authorizationServerSettings = getOptionalBean(httpSecurity, AuthorizationServerSettings.class);
            if (authorizationServerSettings == null) {
                authorizationServerSettings = AuthorizationServerSettings.builder().build();
            }
        }
        return authorizationServerSettings;
    }

    private static RegisteredClientRepository getRegisteredClientRepository(HttpSecurity httpSecurity) {
        RegisteredClientRepository registeredClientRepository = httpSecurity.getSharedObject(RegisteredClientRepository.class);
        if (registeredClientRepository == null) {
            registeredClientRepository = getOptionalBean(httpSecurity, RegisteredClientRepository.class);
            if (registeredClientRepository == null) {
                registeredClientRepository = new InMemoryRegisteredClientRepository();
            }
        }
        return registeredClientRepository;
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
