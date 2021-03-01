// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.b2c.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {

    @GetMapping(value = { "/", "/home" })
    public String index(Model model, OAuth2AuthenticationToken token) {
        initializeModel(model, token);
        return "home";
    }

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

    private void initializeModel(Model model, OAuth2AuthenticationToken token) {
        if (token != null) {
            final OAuth2User user = token.getPrincipal();
            model.addAllAttributes(user.getAttributes());
            model.addAttribute("grant_type", user.getAuthorities());
            model.addAttribute("name", user.getName());
        }
    }
}
