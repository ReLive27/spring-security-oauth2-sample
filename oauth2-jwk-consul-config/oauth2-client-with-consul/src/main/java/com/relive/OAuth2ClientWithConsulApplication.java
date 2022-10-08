package com.relive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author: ReLive
 * @date: 2022/10/8 12:29
 */
@EnableDiscoveryClient
@SpringBootApplication
public class OAuth2ClientWithConsulApplication {

    public static void main(String[] args) {
        SpringApplication.run(OAuth2ClientWithConsulApplication.class, args);
    }
}
