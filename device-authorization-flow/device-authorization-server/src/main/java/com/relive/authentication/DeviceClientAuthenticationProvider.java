package com.relive.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

/**
 * {@code DeviceClientAuthenticationProvider} 实现了 {@link AuthenticationProvider} 接口，
 * 用于验证设备授权客户端的身份（无客户端凭证，基于 client_id）。
 *
 * <p>该 Provider 支持以下功能：</p>
 * <ul>
 *     <li>校验客户端认证方法是否为 {@code none}（无认证）。</li>
 *     <li>验证 client_id 是否存在并匹配已注册客户端。</li>
 *     <li>确保客户端支持无认证方法 {@link ClientAuthenticationMethod#NONE}。</li>
 * </ul>
 *
 * <p>如果验证失败，则会抛出 {@link OAuth2AuthenticationException} 异常，错误信息符合 RFC 6749 标准。</p>
 *
 * @author: ReLive27
 * @date: 2024/1/23 22:41
 */
@Slf4j
public class DeviceClientAuthenticationProvider implements AuthenticationProvider {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-3.2.1";

    /**
     * 客户端注册仓库，用于查找已注册的客户端。
     */
    private final RegisteredClientRepository registeredClientRepository;

    /**
     * 构造函数，初始化 {@link RegisteredClientRepository}。
     *
     * @param registeredClientRepository 客户端注册仓库，不能为 {@code null}。
     */
    public DeviceClientAuthenticationProvider(RegisteredClientRepository registeredClientRepository) {
        Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
        this.registeredClientRepository = registeredClientRepository;
    }

    /**
     * 对设备客户端认证请求进行身份验证。
     *
     * @param authentication 设备客户端认证请求，包含 {@code client_id} 和认证方法。
     * @return 如果验证成功，返回新的 {@link DeviceClientAuthenticationToken} 实例；
     * 如果认证方法不支持，返回 {@code null}。
     * @throws OAuth2AuthenticationException 如果验证失败（如 {@code client_id} 无效或不支持的认证方法）。
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        DeviceClientAuthenticationToken deviceClientAuthentication =
                (DeviceClientAuthenticationToken) authentication;

        // 仅支持无客户端认证方法（none）
        if (!ClientAuthenticationMethod.NONE.equals(deviceClientAuthentication.getClientAuthenticationMethod())) {
            return null;
        }

        // 校验 client_id
        String clientId = deviceClientAuthentication.getPrincipal().toString();
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throwInvalidClient(OAuth2ParameterNames.CLIENT_ID);
        }

        // 校验客户端是否支持 "none" 认证方法
        if (!registeredClient.getClientAuthenticationMethods().contains(
                deviceClientAuthentication.getClientAuthenticationMethod())) {
            throwInvalidClient("authentication_method");
        }

        if (log.isTraceEnabled()) {
            log.trace("Validated device client authentication parameters");
        }

        return new DeviceClientAuthenticationToken(registeredClient,
                deviceClientAuthentication.getClientAuthenticationMethod(), null);
    }

    /**
     * 确定是否支持指定的认证类型。
     *
     * @param authentication 认证类型。
     * @return 如果支持 {@link DeviceClientAuthenticationToken} 类型，返回 {@code true}。
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return DeviceClientAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 抛出无效客户端异常，附带错误描述和 URI 链接。
     *
     * @param parameterName 失败的具体参数（如 {@code client_id} 或认证方法）。
     * @throws OAuth2AuthenticationException 包装的 OAuth2 错误。
     */
    private static void throwInvalidClient(String parameterName) {
        OAuth2Error error = new OAuth2Error(
                OAuth2ErrorCodes.INVALID_CLIENT,
                "Device client authentication failed: " + parameterName,
                ERROR_URI
        );
        throw new OAuth2AuthenticationException(error);
    }
}
