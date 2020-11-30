// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class HomeController {

    @GetMapping("/file")
    @ResponseBody
    @PreAuthorize("hasRole('SCOPE_File.read')")
    public String group1() {
        return "file read success.";
    }

    @GetMapping("/user")
    @ResponseBody
    @PreAuthorize("hasRole('SCOPE_User.read')")
    public String group2() {
        return "user read success.";
    }

}
