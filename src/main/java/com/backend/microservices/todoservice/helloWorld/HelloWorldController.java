package com.backend.microservices.todoservice.helloWorld;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class HelloWorldController {

    @GetMapping("/welcome")
    public HelloWorldBean WelcomeMessage(){
        return new HelloWorldBean("Hello World");
    }

    @GetMapping("/welcome/{name}")
    public HelloWorldBean WelcomeBYID(@PathVariable String name){
        return new HelloWorldBean("Hello World"+name);
    }
}
