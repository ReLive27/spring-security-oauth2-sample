package com.relive.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author: ReLive27
 * @date: 2024/3/5 22:27
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/success")
    public String success(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "success";
    }
}
