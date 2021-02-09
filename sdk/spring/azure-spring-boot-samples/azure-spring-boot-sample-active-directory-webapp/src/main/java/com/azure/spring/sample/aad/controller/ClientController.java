// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.controller;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.azure.spring.sample.aad.utils.JsonMapper.toJsonString;

@Controller
public class ClientController {

    @GetMapping("/")
    public String index(
        Model model,
        OAuth2AuthenticationToken authentication,
        @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient azureClient
    ) {
        model.addAttribute("userName", authentication.getName());
        model.addAttribute("clientName", azureClient.getClientRegistration().getClientName());
        return "index";
    }

    @GetMapping("/graph")
    @ResponseBody
    public String graph(
        @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graphClient
    ) {
        // toJsonString() is just a demo.
        // oAuth2AuthorizedClient contains access_token. We can use this access_token to access resource server.
        return toJsonString(graphClient);
    }

    @GetMapping("/office")
    @ResponseBody
    public String office(
        @RegisteredOAuth2AuthorizedClient("office") OAuth2AuthorizedClient officeClient
    ) {
        return toJsonString(officeClient);
    }
}
