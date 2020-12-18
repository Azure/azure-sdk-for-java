// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.utils;

import com.azure.spring.sample.aad.controller.ClientController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

public class JsonMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static final String toJsonString(OAuth2AuthorizedClient authorizedClient) {
        String clientJsonString = "Json String for OAuth2AuthorizedClient";

        try {
            clientJsonString = MAPPER.writeValueAsString(authorizedClient);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Error with transfer OAuth2AuthorizedClient to Json");
            clientJsonString = "Fail to generate Json String of current OAuth2AuthorizedClient";
        }

        LOGGER.info(clientJsonString);
        return clientJsonString;
    }
}
