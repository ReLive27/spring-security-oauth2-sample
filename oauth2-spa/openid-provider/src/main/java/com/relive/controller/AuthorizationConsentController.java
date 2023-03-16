package com.relive.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.*;

/**
 * @author: ReLive
 * @date: 2023/3/16 19:12
 */
@RestController
@RequiredArgsConstructor
public class AuthorizationConsentController {
    private final RegisteredClientRepository registeredClientRepository;

    @GetMapping(value = "/oauth2/consent")
    public Map<String, Object> consent(Principal principal,
                                       @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
                                       @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
                                       @RequestParam(OAuth2ParameterNames.STATE) String state) {

        Set<String> scopesToApprove = new LinkedHashSet<>();
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        Set<String> scopes = registeredClient.getScopes();
        for (String requestedScope : StringUtils.delimitedListToStringArray(scope, " ")) {
            if (scopes.contains(requestedScope)) {
                scopesToApprove.add(requestedScope);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("clientId", clientId);
        data.put("clientName", registeredClient.getClientName());
        data.put("state", state);
        data.put("scopes", scopesToApprove);
        data.put("principalName", principal.getName());
        data.put("redirectUri", registeredClient.getRedirectUris().iterator().next());

        return Collections.singletonMap("data", data);
    }
}
