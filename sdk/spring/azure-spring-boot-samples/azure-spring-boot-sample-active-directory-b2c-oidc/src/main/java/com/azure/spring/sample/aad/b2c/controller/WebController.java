// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.b2c.controller;

import com.azure.spring.autoconfigure.b2c.AADB2CProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @Autowired
    AADB2CProperties aadb2CProperties;

    private void initializeModel(Model model, OAuth2AuthenticationToken token) {
        if (token != null) {
            final OAuth2User user = token.getPrincipal();

            model.addAttribute("grant_type", user.getAuthorities());
            model.addAllAttributes(user.getAttributes());
            model.addAttribute("name", user.getName());
        }
        model.addAttribute("aadb2c_profileedit", aadb2CProperties.getUserFlows().getProfileEdit());
        model.addAttribute("aadb2c_passwordreset", aadb2CProperties.getUserFlows().getPasswordReset());
    }

    @GetMapping(value = { "/", "/home", "/greeting" })
    public String index(Model model, OAuth2AuthenticationToken token) {
        initializeModel(model, token);
        return "home";
    }

}
