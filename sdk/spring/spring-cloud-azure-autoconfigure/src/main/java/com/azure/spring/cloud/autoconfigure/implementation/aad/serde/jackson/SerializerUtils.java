// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.serde.jackson;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.jackson.OAuth2ClientJacksonModule;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;
import java.util.Map;

public final class SerializerUtils {
    private static final JsonMapper OBJECT_MAPPER;
    private static final TypeReference<Map<String, OAuth2AuthorizedClient>> TYPE_REFERENCE =
        new TypeReference<Map<String, OAuth2AuthorizedClient>>() { };

    static {
        OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new OAuth2ClientJacksonModule())
            .build();
    }

    private SerializerUtils() {
    }

    /**
     * Serialize {@link Map} to {@link String}.
     * @param authorizedClients the map to be serialized. It will not be modified in this method.
     * @return The serialized {@link String}.
     */
    public static String serializeOAuth2AuthorizedClientMap(Map<String, OAuth2AuthorizedClient> authorizedClients) {
        String result;
        try {
            result = OBJECT_MAPPER.writerFor(TYPE_REFERENCE).writeValueAsString(authorizedClients);
        } catch (JacksonException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    /**
     * Deserialize {@link String} to {@link Map}.
     * @param authorizedClientsString the String to be deserialized
     * @return The deserialized {@link Map}. Return {@link Collections#emptyMap()} if authorizedClientsString is null.
     */
    public static Map<String, OAuth2AuthorizedClient> deserializeOAuth2AuthorizedClientMap(String authorizedClientsString) {
        if (authorizedClientsString == null) {
            return Collections.emptyMap();
        }
        Map<String, OAuth2AuthorizedClient> authorizedClients;
        try {
            authorizedClients = OBJECT_MAPPER.readValue(authorizedClientsString, TYPE_REFERENCE);
        } catch (JacksonException e) {
            throw new IllegalStateException(e);
        }
        return authorizedClients;
    }
}
