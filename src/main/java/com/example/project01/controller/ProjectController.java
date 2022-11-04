package com.example.project01.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectController {

    @GetMapping("/hi")
    public String meetYou(Model model) {
        model.addAttribute("username","지현");
        return "greetings";
    }
}
