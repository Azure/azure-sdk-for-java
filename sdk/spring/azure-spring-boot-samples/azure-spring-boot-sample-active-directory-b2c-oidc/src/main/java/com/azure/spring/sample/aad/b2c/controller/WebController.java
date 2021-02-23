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

import java.util.Map;

@Controller
public class WebController {

    @Autowired
    AADB2CProperties aadb2CProperties;

    private void initializeModel(Model model, OAuth2AuthenticationToken token) {
        if (token != null) {
            final OAuth2User user = token.getPrincipal();

            model.addAllAttributes(user.getAttributes());
            model.addAttribute("grant_type", user.getAuthorities());
            model.addAttribute("name", user.getName());
        }
        Map.Entry<String, String> profileEditEntry = aadb2CProperties.getProfileEdit();
        Map.Entry<String, String> passwordResetEntry = aadb2CProperties.getPasswordReset();
        model.addAttribute("aadb2c_profileedit", null == profileEditEntry ? null : profileEditEntry.getValue());
        model.addAttribute("aadb2c_passwordreset", null == passwordResetEntry? null : passwordResetEntry.getValue());
    }

    @GetMapping(value = { "/", "/home" })
    public String index(Model model, OAuth2AuthenticationToken token) {
        initializeModel(model, token);
        return "home";
    }
}
