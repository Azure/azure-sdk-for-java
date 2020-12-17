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

    public static final String toJsonString(OAuth2AuthorizedClient authorizedClient) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String clientJsonString = mapper.writeValueAsString(authorizedClient);
        LOGGER.info(clientJsonString);
        return clientJsonString;
    }
}
