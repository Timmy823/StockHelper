package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HomeController {
    
    @RequestMapping("/home") 
    public String home() {
        // test
        return "home";
    }
}

