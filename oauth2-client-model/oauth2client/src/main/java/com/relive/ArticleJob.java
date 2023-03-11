package com.relive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;


/**
 * @author: ReLive
 * @date: 2022/7/2 10:47 下午
 */
@Slf4j
@Service
public class ArticleJob {

    @Autowired
    private WebClient webClient;

    /**
     * Call the resource server task and execute it every 2 seconds
     */
    @Scheduled(cron = "0/2 * * * * ? ")
    public void exchange() {
        List list = this.webClient
                .get()
                .uri("http://127.0.0.1:8090/resource/article")
                .attributes(clientRegistrationId("messaging-client-model"))
                .retrieve()
                .bodyToMono(List.class)
                .block();
        log.info("Call resource server execution result: " + list);
    }
}
