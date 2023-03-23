package com.relive.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: ReLive
 * @date: 2023/3/16 19:12
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin
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
        data.put("scopes", withDescription(scopesToApprove));
        data.put("principalName", principal.getName());
        data.put("redirectUri", registeredClient.getRedirectUris().iterator().next());

        Map<String, Object> result = new HashMap<>();
        result.put("code", HttpServletResponse.SC_OK);
        result.put("data", data);
        return result;
    }

    private static Set<ScopeWithDescription> withDescription(Set<String> scopes) {
        Set<ScopeWithDescription> scopeWithDescriptions = new LinkedHashSet<>();
        for (String scope : scopes) {
            if (OidcScopes.OPENID.equals(scope)) {
                continue;
            }
            scopeWithDescriptions.add(new ScopeWithDescription(scope));

        }
        return scopeWithDescriptions;
    }

    public static class ScopeWithDescription {
        private static final String DEFAULT_DESCRIPTION = "We are unable to provide information about this permission";
        private static final Map<String, String> scopeDescriptions = new HashMap<>();

        static {
            scopeDescriptions.put(
                    "profile",
                    "Use your profile picture and nickname"
            );
            scopeDescriptions.put(
                    "email",
                    "Get your email"
            );
        }

        public final String scope;
        public final String description;
        public final boolean disabled;

        ScopeWithDescription(String scope) {
            this.scope = scope;
            this.description = scopeDescriptions.getOrDefault(scope, DEFAULT_DESCRIPTION);
            this.disabled = true;
        }
    }
}
