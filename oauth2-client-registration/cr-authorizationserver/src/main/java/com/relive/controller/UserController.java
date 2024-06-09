package com.relive.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2024/6/9 23:47
 */
@RestController
public class UserController {

    @GetMapping("/profile")
    public Map<String, Object> profile(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaims();
    }
}
