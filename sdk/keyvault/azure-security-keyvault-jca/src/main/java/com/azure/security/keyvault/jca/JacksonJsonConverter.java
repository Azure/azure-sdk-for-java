// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The JSON-B JsonConverter.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
class JacksonJsonConverter implements JsonConverter {

    /**
     * From JSON.
     *
     * @param string the string.
     * @param resultClass the result class.
     * @return the object, or null if the conversion failed.
     */
    @Override
    public Object fromJson(String string, Class<?> resultClass) {
        Object result = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            result = objectMapper.readValue(string, resultClass);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
        String result = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // consider logging.
        }
        return result;
    }
}
