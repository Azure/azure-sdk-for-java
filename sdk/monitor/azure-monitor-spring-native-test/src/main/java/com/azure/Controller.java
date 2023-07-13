package com.azure;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    public static final String URL = "/controller-url";

    @GetMapping(URL)
    public String check() {
        return "OK!";
    }
}
