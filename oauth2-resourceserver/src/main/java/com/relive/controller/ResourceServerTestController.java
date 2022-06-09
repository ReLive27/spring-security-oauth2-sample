package com.relive.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author: ReLive
 * @date: 2021/12/25 2:09 下午
 */
@RestController
public class ResourceServerTestController {

    @GetMapping("/resource/test")
    public Map<String, Object> getArticles(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }
}
