// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * The Jackson JsonConverter.
 */
public final class JsonConverterUtil {
    private static final ObjectMapper FROM_JSON_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final ObjectMapper TO_JSON_MAPPER = new ObjectMapper();

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JsonConverterUtil.class.getName());

    /**
     * From JSON.
     *
     * @param string the string.
     * @param resultClass the result class.
     * @return the object, or null if the conversion failed.
     */
    public static Object fromJson(String string, Class<?> resultClass) {
        LOGGER.entering("JsonConverterUtil", "fromJson", new Object[] { string, resultClass });
        Object result = null;
        try {
            result = FROM_JSON_MAPPER.readValue(string, resultClass);
        } catch (JsonProcessingException e) {
            LOGGER.log(WARNING, "Unable to convert from JSON", e);
        }
        LOGGER.exiting("JsonConverterUtil", "fromJson", result);
        return result;
    }

    /**
     * To JSON.
     *
     * @param object the object.
     * @return the JSON string.
     */
    public static String toJson(Object object) {
        LOGGER.entering("JsonConverterUtil", "toJson", object);
        String result = null;
        try {
            result = TO_JSON_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.log(WARNING, "Unable to convert to JSON", e);
        }
        LOGGER.exiting("JsonConverterUtil", "toJson", result);
        return result;
    }
}
