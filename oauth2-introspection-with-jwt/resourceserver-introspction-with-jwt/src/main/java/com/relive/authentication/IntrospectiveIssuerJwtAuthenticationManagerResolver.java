package com.relive.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: ReLive
 * @date: 2022/11/20 20:29
 */
@Slf4j
public class IntrospectiveIssuerJwtAuthenticationManagerResolver implements AuthenticationManagerResolver<String> {

    private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();

    @Override
    public AuthenticationManager resolve(String issuer) {
        //TODO 内存或数据库存储类

        //TODO 通过issusr查询clientid 和clientsecret，为空则返回null

        //TODO 定义CachingOpaqueTokenIntrospector，里面封装Cache和OpaqueTokenIntrospector

        //TODO 通过clientID和ClientSecret实例化CachingOpaqueTokenIntrospector

        //TODO 创建OpaqueTokenAuthenticationProvider

        if (true) {
            AuthenticationManager authenticationManager = this.authenticationManagers.computeIfAbsent(issuer,
                    (k) -> {
                        log.debug("Constructing AuthenticationManager");
                        //TODO 生成OpaqueTokenIntrospector
                        return new OpaqueTokenAuthenticationProvider(null)::authenticate;
                    });
            log.debug(LogMessage.format("Resolved AuthenticationManager for issuer '%s'", issuer).toString());
            return authenticationManager;

        } else {
            log.debug("Did not resolve AuthenticationManager since issuer is not trusted");
        }
        return null;
    }
}
