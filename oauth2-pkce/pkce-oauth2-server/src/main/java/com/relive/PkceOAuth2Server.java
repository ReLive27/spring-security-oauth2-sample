package com.relive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: ReLive
 * @date: 2022/7/4 8:45 下午
 */
@SpringBootApplication
public class PkceOAuth2Server {

    public static void main(String[] args) {
        SpringApplication.run(PkceOAuth2Server.class, args);
    }
}
