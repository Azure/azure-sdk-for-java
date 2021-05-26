// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.b2c.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {

    @ResponseBody
    @GetMapping(value = { "/write" })
    @PreAuthorize("hasAuthority('APPROLE_Test.write')")
    public String write() {
        return "Write success.";
    }

    @ResponseBody
    @GetMapping(value = { "/read" })
    @PreAuthorize("hasAuthority('APPROLE_Test.read')")
    public String read() {
        return "Read success.";
    }

    @ResponseBody
    @GetMapping(value = { "/log" })
    @PreAuthorize("hasAuthority('SCOPE_Test.Log')")
    public String log() {
        return "Test log read success.";
    }
}
