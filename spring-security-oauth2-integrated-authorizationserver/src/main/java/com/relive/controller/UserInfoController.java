package com.relive.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: ReLive
 * @date: 2021/12/25 4:00 下午
 */
@RestController
public class UserInfoController {

    @PostMapping("/oauth2/userInfo")
    public Map<String, Object> authentication(Authentication authentication) {
        String name = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", name);
        userInfo.put("authorities", authorities);
        return userInfo;
    }
}
