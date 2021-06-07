// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

public class JsonMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonMapper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    public static String toJsonString(OAuth2AuthorizedClient authorizedClient) {
        String clientJsonString;
        try {
            clientJsonString = MAPPER.writeValueAsString(authorizedClient);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Error when transfer OAuth2AuthorizedClient to Json", e);
            clientJsonString = "Fail to generate Json String of current OAuth2AuthorizedClient";
        }
        LOGGER.info(clientJsonString);
        return clientJsonString;
    }
}
