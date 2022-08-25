package com.relive.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

/**
 * @author: ReLive
 * @date: 2022/8/20 16:20
 */
@RestController
@RequiredArgsConstructor
public class OAuth2ClientController {

    private final WebClient webClient;

    @GetMapping(value = "/client/article")
    public Map<String, Object> getArticles(@RegisteredOAuth2AuthorizedClient("messaging-client-authorization-code") OAuth2AuthorizedClient authorizedClient) {
        return this.webClient
                .get()
                .uri("http://127.0.0.1:8090/resource/article")
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
