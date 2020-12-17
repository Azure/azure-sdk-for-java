// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ClientController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);

    @GetMapping("/graph")
    @ResponseBody
    public String graph(
        @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient oAuth2AuthorizedClient
    ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String clientJsonString = mapper.writeValueAsString(oAuth2AuthorizedClient);
        LOGGER.info(clientJsonString);
        return clientJsonString;
    }

    @GetMapping("/office")
    @ResponseBody
    public String office(
        @RegisteredOAuth2AuthorizedClient("office") OAuth2AuthorizedClient oAuth2AuthorizedClient
    ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String clientJsonString = mapper.writeValueAsString(oAuth2AuthorizedClient);
        LOGGER.info(clientJsonString);
        return clientJsonString;
    }

    @GetMapping("/azure")
    @ResponseBody
    public String azure(
        @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient oAuth2AuthorizedClient
    ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String clientJsonString = mapper.writeValueAsString(oAuth2AuthorizedClient);
        LOGGER.info(clientJsonString);
        return clientJsonString;
    }
}
