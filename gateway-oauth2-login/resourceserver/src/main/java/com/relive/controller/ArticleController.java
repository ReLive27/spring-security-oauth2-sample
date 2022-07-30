package com.relive.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: ReLive
 * @date: 2022/6/24 11:19 上午
 */
@RestController
public class ArticleController {

    @GetMapping("/resource/article")
    public Map<String, Object> article(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> result = new HashMap<>(2);
        result.put("principal", jwt.getClaims());
        result.put("article", Arrays.asList("article1", "article2", "article3"));
        return result;
    }
}
