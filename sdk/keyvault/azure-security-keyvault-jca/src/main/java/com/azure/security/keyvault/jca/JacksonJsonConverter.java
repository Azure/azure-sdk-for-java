// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * The Jackson JsonConverter.
 */
class JacksonJsonConverter implements JsonConverter {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JacksonJsonConverter.class.getName());

    /**
     * From JSON.
     *
     * @param string the string.
     * @param resultClass the result class.
     * @return the object, or null if the conversion failed.
     */
    @Override
    public Object fromJson(String string, Class<?> resultClass) {
        LOGGER.entering("JacksonJsonConverter", "fromJson", new Object[] { string, resultClass });
        Object result = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            result = objectMapper.readValue(string, resultClass);
        } catch (JsonProcessingException e) {
            LOGGER.log(WARNING, "Unable to convert from JSON", e);
        }
        LOGGER.exiting("JacksonJsonConverter", "fromJson", result);
        return result;
    }

    /**
     * To JSON.
     *
     * @param object the object.
     * @return the JSON string.
     */
    @Override
    public String toJson(Object object) {
        LOGGER.entering("JacksonJsonConverter", "toJson", object);
        String result = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.log(WARNING, "Unable to convert to JSON", e);
        }
        LOGGER.exiting("JacksonJsonConverter", "toJson", result);
        return result;
    }
}
