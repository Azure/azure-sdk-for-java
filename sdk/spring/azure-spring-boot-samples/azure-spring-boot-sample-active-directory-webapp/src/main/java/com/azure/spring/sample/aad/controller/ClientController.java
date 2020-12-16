// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public class ClientController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);

    @GetMapping("/graph")
    @ResponseBody
    public String graph(
        @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient oAuth2AuthorizedClient
    ) {

        logAuthorizedClient(oAuth2AuthorizedClient);
        return "graph";
    }

    @GetMapping("/office")
    @ResponseBody
    public String office(
        @RegisteredOAuth2AuthorizedClient("office") OAuth2AuthorizedClient oAuth2AuthorizedClient
    ) {
        logAuthorizedClient(oAuth2AuthorizedClient);
        return "office";
    }

    @GetMapping("/azure")
    @ResponseBody
    public String azure(
        @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient oAuth2AuthorizedClient
    ) {
        logAuthorizedClient(oAuth2AuthorizedClient);
        return "azure";
    }

    static void logAuthorizedClient(OAuth2AuthorizedClient authorizedClient) {
        LOGGER.info(
            "clientName = {}",
            Optional.of(authorizedClient)
                    .map(OAuth2AuthorizedClient::getClientRegistration)
                    .map(ClientRegistration::getClientName)
                    .orElse(null)
        );
        LOGGER.info(
            "scopesFromAccessToken = {}",
            Optional.of(authorizedClient)
                    .map(OAuth2AuthorizedClient::getAccessToken)
                    .map(OAuth2AccessToken::getScopes)
                    .orElse(null)
        );
    }
}
