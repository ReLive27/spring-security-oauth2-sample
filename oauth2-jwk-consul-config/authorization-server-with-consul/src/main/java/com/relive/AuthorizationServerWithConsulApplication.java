package com.relive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author: ReLive
 * @date: 2022/9/1 12:36
 */
@EnableDiscoveryClient
@SpringBootApplication
public class AuthorizationServerWithConsulApplication {


    public static void main(String[] args) {
        SpringApplication.run(AuthorizationServerWithConsulApplication.class, args);
    }
}
