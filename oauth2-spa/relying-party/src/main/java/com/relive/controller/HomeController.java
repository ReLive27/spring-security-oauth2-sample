package com.relive.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: ReLive
 * @date: 2023/3/18 18:47
 */
@RestController
public class HomeController {

    @GetMapping("/home")
    public String home(Authentication authentication) {
        return authentication.getName();
    }
}
