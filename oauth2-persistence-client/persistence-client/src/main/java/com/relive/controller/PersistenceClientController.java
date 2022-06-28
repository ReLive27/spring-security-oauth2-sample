package com.relive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

/**
 * @author: ReLive
 * @date: 2022/6/27 7:34 下午
 */
@RestController
public class PersistenceClientController {
    @Autowired
    private WebClient webClient;

    @GetMapping(value = "/client/test")
    public List<String> getArticles(@RegisteredOAuth2AuthorizedClient("messaging-client-authorization-code") OAuth2AuthorizedClient authorizedClient) {
        return this.webClient
                .get()
                .uri("http://127.0.0.1:8090/resource/article")
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(List.class)
                .block();
    }
}
