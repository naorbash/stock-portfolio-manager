package com.fyber.junior.developer.assignment.stock.rest.resources;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/")
public class HomeResource {

    @GetMapping()
    public String home(){
        return "Your App Is Up And Running...";
    }
}
