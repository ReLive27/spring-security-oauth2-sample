package com.relive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author: ReLive27
 * @date: 2024/5/2 23:36
 */
@Controller
public class LoginSuccessController {

    @GetMapping("/success")
    public String success() {
        return "success";
    }
}
