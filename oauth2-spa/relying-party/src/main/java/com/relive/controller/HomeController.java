package com.relive.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * @author: ReLive
 * @date: 2023/3/18 18:47
 */
@RestController
public class HomeController {

    @GetMapping("/home")
    public Map<String, String> home(Authentication authentication) {
        return Collections.singletonMap("name", authentication.getName());
    }
}
