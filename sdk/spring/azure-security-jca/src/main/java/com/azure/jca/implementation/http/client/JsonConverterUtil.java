// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca.implementation.http.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

public class JsonConverterUtil {

    private static final Logger LOGGER = Logger.getLogger(JsonConverterUtil.class.getName());

    public static Object fromJson(String string, Class<?> resultClass) {
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

    public static String toJson(Object object) {
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
