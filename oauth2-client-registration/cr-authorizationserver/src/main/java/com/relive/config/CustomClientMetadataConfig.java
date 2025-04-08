package com.relive.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientRegistration;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientConfigurationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.oidc.converter.OidcClientRegistrationRegisteredClientConverter;
import org.springframework.security.oauth2.server.authorization.oidc.converter.RegisteredClientOidcClientRegistrationConverter;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 自定义客户端元数据配置类，用于扩展 OIDC 动态客户端注册功能，支持处理自定义客户端元数据字段。
 * <p>
 * 通过扩展 Spring Authorization Server 的默认注册转换器，实现对自定义字段的注入和提取，
 * 例如 "require-authorization-consent"、"require-proof-key"。
 *
 * 本配置类适用于构建身份认证系统时需要支持客户端自助注册并扩展客户端元数据字段的场景。
 * 可用于 OAuth2/OIDC 动态注册支持增强。
 *
 * @author ReLive27
 * @date 2024/6/2
 */
public class CustomClientMetadataConfig {

    /**
     * 配置自定义客户端元数据转换器
     *
     * @return 用于注册到 OIDC 客户端注册相关 AuthenticationProvider 的 Consumer 实例
     */
    public static Consumer<List<AuthenticationProvider>> configureCustomClientMetadataConverters() {
        // 定义自定义支持的客户端元数据字段
        List<String> customClientMetadata = Arrays.asList("require-authorization-consent", "require-proof-key");

        return (authenticationProviders) -> {
            CustomRegisteredClientConverter registeredClientConverter =
                    new CustomRegisteredClientConverter(customClientMetadata);
            CustomClientRegistrationConverter clientRegistrationConverter =
                    new CustomClientRegistrationConverter(customClientMetadata);

            // 设置自定义转换器到 Spring Authorization Server 提供的 Provider 中
            authenticationProviders.forEach((authenticationProvider) -> {
                if (authenticationProvider instanceof OidcClientRegistrationAuthenticationProvider) {
                    OidcClientRegistrationAuthenticationProvider provider = (OidcClientRegistrationAuthenticationProvider) authenticationProvider;
                    provider.setRegisteredClientConverter(registeredClientConverter);
                    provider.setClientRegistrationConverter(clientRegistrationConverter);
                }
                if (authenticationProvider instanceof OidcClientConfigurationAuthenticationProvider) {
                    OidcClientConfigurationAuthenticationProvider provider = (OidcClientConfigurationAuthenticationProvider) authenticationProvider;
                    provider.setClientRegistrationConverter(clientRegistrationConverter);
                }
            });
        };
    }

    /**
     * 自定义 RegisteredClient 转换器，将 OIDC 注册请求中的自定义字段写入 ClientSettings
     */
    private static class CustomRegisteredClientConverter
            implements Converter<OidcClientRegistration, RegisteredClient> {

        private final List<String> customClientMetadata;
        private final OidcClientRegistrationRegisteredClientConverter delegate;
        private static final String CLIENT_SETTINGS_NAMESPACE = "settings.client.";

        private CustomRegisteredClientConverter(List<String> customClientMetadata) {
            this.customClientMetadata = customClientMetadata;
            this.delegate = new OidcClientRegistrationRegisteredClientConverter();
        }

        @Override
        public RegisteredClient convert(OidcClientRegistration clientRegistration) {
            // 使用默认转换器创建 RegisteredClient 实例
            RegisteredClient registeredClient = this.delegate.convert(clientRegistration);

            // 提取自定义字段并写入 ClientSettings
            ClientSettings.Builder clientSettingsBuilder = ClientSettings.withSettings(
                    registeredClient.getClientSettings().getSettings());
            if (!CollectionUtils.isEmpty(this.customClientMetadata)) {
                clientRegistration.getClaims().forEach((claim, value) -> {
                    if (this.customClientMetadata.contains(claim)) {
                        clientSettingsBuilder.setting(CLIENT_SETTINGS_NAMESPACE.concat(claim), value);
                    }
                });
            }

            return RegisteredClient.from(registeredClient)
                    .clientSettings(clientSettingsBuilder.build())
                    .build();
        }
    }

    /**
     * 自定义 OIDC ClientRegistration 转换器，将 ClientSettings 中的自定义字段返回给客户端
     */
    private static class CustomClientRegistrationConverter
            implements Converter<RegisteredClient, OidcClientRegistration> {

        private final List<String> customClientMetadata;
        private final RegisteredClientOidcClientRegistrationConverter delegate;
        private static final String CLIENT_SETTINGS_NAMESPACE = "settings.client.";

        private CustomClientRegistrationConverter(List<String> customClientMetadata) {
            this.customClientMetadata = customClientMetadata;
            this.delegate = new RegisteredClientOidcClientRegistrationConverter();
        }

        @Override
        public OidcClientRegistration convert(RegisteredClient registeredClient) {
            // 使用默认转换器生成 OIDC 客户端注册对象
            OidcClientRegistration clientRegistration = this.delegate.convert(registeredClient);

            // 将自定义设置字段添加到返回的 claims 中
            Map<String, Object> claims = new HashMap<>(clientRegistration.getClaims());
            if (!CollectionUtils.isEmpty(this.customClientMetadata)) {
                ClientSettings clientSettings = registeredClient.getClientSettings();
                claims.putAll(this.customClientMetadata.stream()
                        .filter(metadata -> clientSettings.getSetting(CLIENT_SETTINGS_NAMESPACE.concat(metadata)) != null)
                        .collect(Collectors.toMap(name -> CLIENT_SETTINGS_NAMESPACE + name, value -> clientSettings.getSetting(CLIENT_SETTINGS_NAMESPACE.concat(value)))));

            }

            return OidcClientRegistration.withClaims(claims).build();
        }

    }

}
