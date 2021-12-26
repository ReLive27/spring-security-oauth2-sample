package com.relive.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: ReLive
 * @date: 2021/12/25 2:09 下午
 */
@RestController
public class ResourceServerTestController {

    @GetMapping("/resource/test")
    public String getArticles() {
        return "resources access success";
    }
}
