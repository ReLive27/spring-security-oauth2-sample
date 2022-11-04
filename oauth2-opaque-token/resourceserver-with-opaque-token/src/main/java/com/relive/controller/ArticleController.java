package com.relive.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: ReLive
 * @date: 2022/11/1 19:17
 */
@RestController
public class ArticleController {

    @GetMapping("/resource/article")
    public Map<String, Object> foo(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal) {
        Map<String, Object> result = new HashMap<>();
        result.put("sub", principal.getAttribute("sub"));
        result.put("articles", Arrays.asList("Effective Java", "Spring In Action"));

        return result;
    }
}
