package com.relive.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ReLive
 * @date: 2022/6/24 11:19 上午
 */
@RestController
public class ArticleController {

    List<String> article = new ArrayList<String>() {{
        add("article1");
        add("article2");
    }};

    @PreAuthorize("hasAuthority('SCOPE_read')")
    @GetMapping("/resource/article/read")
    public Map<String, Object> read(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> result = new HashMap<>(2);
        result.put("principal", jwt.getClaims());
        result.put("article", article);
        return result;
    }

    @PreAuthorize("hasAuthority('SCOPE_write')")
    @GetMapping("/resource/article/write")
    public String write(@RequestParam String name) {
        article.add(name);
        return "success";
    }
}
