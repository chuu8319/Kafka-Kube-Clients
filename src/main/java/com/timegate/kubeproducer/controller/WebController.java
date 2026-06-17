package com.timegate.kubeproducer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/ui")
    public String index() {
        return "index"; // templates/index.html
    }
}