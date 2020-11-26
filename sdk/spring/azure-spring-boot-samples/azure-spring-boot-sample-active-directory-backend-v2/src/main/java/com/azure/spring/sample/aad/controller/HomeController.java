// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("group1")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_group1')")
    public String group1() {
        return "group1 message";
    }

    @GetMapping("group2")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_group2')")
    public String group2() {
        return "group2 message";
    }

    @GetMapping("/")
    public String index(
        Model model,
        OAuth2AuthenticationToken authentication,
        @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient
    ) {
        model.addAttribute("userName", authentication.getName());
        model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());
        return "index";
    }
}
