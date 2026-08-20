package com.khaspper.quickstart;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// This is so spring knows that hey this class it handles HTTP requests bro and returns
// the response as a Json or XML
@RestController
public class HelloWorldController {

    // This says YO BRO!!!!! at path "/hello" we return "Hello Markus!"
    @GetMapping(path = "/hello")
    public String helloWorld() {
        return "Hello Markus!";
    }
}
