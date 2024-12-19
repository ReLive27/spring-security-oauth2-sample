package com.relive.authentication;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Map;

/**
 * {@code DeviceClientAuthenticationToken} 是设备客户端身份验证的自定义令牌。
 *
 * <p>该类继承自 {@link OAuth2ClientAuthenticationToken}，主要用于 OAuth 2.0 设备授权流程中的客户端认证。</p>
 *
 * <p>设备客户端身份验证的特点：</p>
 * <ul>
 *     <li>客户端不需要使用客户端密钥（即无凭证认证）。</li>
 *     <li>认证过程中必须提供 {@code client_id}。</li>
 *     <li>可能包含额外参数 {@link Map}，例如设备授权请求中的信息。</li>
 * </ul>
 *
 * <p>该类提供两种构造函数：</p>
 * <ul>
 *     <li>一种基于 {@code clientId} 和认证方法（适用于请求阶段）。</li>
 *     <li>一种基于已注册客户端 {@link RegisteredClient}（适用于验证通过后的阶段）。</li>
 * </ul>
 *
 * @author: ReLive27
 * @date: 2024/1/23 22:42
 */
public class DeviceClientAuthenticationToken extends OAuth2ClientAuthenticationToken {

    /**
     * 构造函数。
     *
     * @param clientId                   客户端 ID，表示发起请求的客户端。
     * @param clientAuthenticationMethod 客户端认证方法，通常为 {@link ClientAuthenticationMethod#NONE}。
     * @param credentials                凭据，可以为 {@code null}。
     * @param additionalParameters       额外参数，例如设备授权请求中的参数，允许为 {@code null}。
     */
    public DeviceClientAuthenticationToken(String clientId, ClientAuthenticationMethod clientAuthenticationMethod,
                                           @Nullable Object credentials, @Nullable Map<String, Object> additionalParameters) {
        super(clientId, clientAuthenticationMethod, credentials, additionalParameters);
    }

    /**
     * 构造函数，用于基于已注册客户端创建。
     *
     * @param registeredClient           已注册客户端，表示通过验证的客户端。
     * @param clientAuthenticationMethod 客户端认证方法，通常为 {@link ClientAuthenticationMethod#NONE}。
     * @param credentials                凭据，可以为 {@code null}。
     */
    public DeviceClientAuthenticationToken(RegisteredClient registeredClient, ClientAuthenticationMethod clientAuthenticationMethod,
                                           @Nullable Object credentials) {
        super(registeredClient, clientAuthenticationMethod, credentials);
    }
}
