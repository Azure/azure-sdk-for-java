// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HomeController {

    @GetMapping("/webapiB")
    @ResponseBody
    @PreAuthorize("hasAuthority('SCOPE_WebApiB.ExampleScope')")
    public String file() {
        return "Response from WebApiB.";
    }

    @GetMapping("/user")
    @ResponseBody
    @PreAuthorize("hasAuthority('SCOPE_User.Read')")
    public String user() {
        return "User read success.";
    }

}
