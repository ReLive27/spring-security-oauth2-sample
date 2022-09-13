package com.relive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author: ReLive
 * @date: 2022/9/12 22:14
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ResourceWithConsulApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceWithConsulApplication.class, args);
    }
}
