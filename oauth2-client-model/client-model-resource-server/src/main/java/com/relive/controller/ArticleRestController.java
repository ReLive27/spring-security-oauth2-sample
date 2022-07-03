package com.relive.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author: ReLive
 * @date: 2022/7/2 11:02 下午
 */
@RestController
public class ArticleRestController {

    @GetMapping("/resource/article")
    public List<String> article() {
        return Arrays.asList("article1", "article2", "article3");
    }
}
