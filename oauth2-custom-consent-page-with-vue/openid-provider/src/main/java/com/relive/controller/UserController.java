package com.relive.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: ReLive
 * @date: 2023/3/15 19:12
 */
@RestController
public class UserController {

    @GetMapping("/user/info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", HttpServletResponse.SC_OK);
        result.put("data", Collections.singletonMap("name", jwt.getClaim("sub")));
        return result;

    }
}
