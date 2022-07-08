package com.relive.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author: ReLive
 * @date: 2022/6/24 4:20 下午
 */
@RestController
public class UserInfoController {

    @PostMapping("/userInfo")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }
}
