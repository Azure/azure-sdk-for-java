// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.implementation.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module;

import java.util.HashMap;
import java.util.Map;

public class SerializerUtils {
    private static final ObjectMapper OBJECT_MAPPER;
    private static final TypeReference<Map<String, OAuth2AuthorizedClient>> TYPE_REFERENCE =
        new TypeReference<Map<String, OAuth2AuthorizedClient>>() {
        };

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new OAuth2ClientJackson2Module());
        // Use to handle problem: OAuth2ClientJackson2Module does not support self-defined ClientRegistration type.
        // For example: "on_behalf_on" or "azure_delegated".
        // TODO(rujche) Delete this after OAuth2ClientJackson2Module support self-defined ClientRegistration type.
        OBJECT_MAPPER.registerModule(new AADOAuth2ClientJackson2Module());
        OBJECT_MAPPER.registerModule(new CoreJackson2Module());
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    public static String serializeOAuth2AuthorizedClientMap(Map<String, OAuth2AuthorizedClient> authorizedClients) {
        String result;
        try {
            result = OBJECT_MAPPER.writeValueAsString(authorizedClients);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    public static Map<String, OAuth2AuthorizedClient> deserializeOAuth2AuthorizedClientMap(String authorizedClientsString) {
        if (authorizedClientsString == null) {
            return new HashMap<>();
        }
        Map<String, OAuth2AuthorizedClient> authorizedClients;
        try {
            authorizedClients = OBJECT_MAPPER.readValue(authorizedClientsString, TYPE_REFERENCE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        return authorizedClients;
    }
}
