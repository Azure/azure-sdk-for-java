// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

final class JsonConfigurationParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static boolean isJsonContentType(String contentType) {
        String acceptedMainType = "application";
        String acceptedSubType = "json";

        if (!StringUtils.hasText(contentType)) {
            return false;
        }

        if (contentType.contains("/")) {
            String mainType = contentType.split("/")[0];
            String subType = contentType.split("/")[1];

            if (mainType.equalsIgnoreCase(acceptedMainType)) {
                if (subType.contains("+")) {
                    List<String> subtypes = Arrays.asList(subType.split("\\+"));
                    return subtypes.contains(acceptedSubType);
                } else {
                    return subType.equalsIgnoreCase(acceptedSubType);
                }
            }
        }

        return false;
    }

    static HashMap<String, Object> parseJsonSetting(ConfigurationSetting setting)
        throws JsonProcessingException {
        HashMap<String, Object> settings = new HashMap<>();
        JsonNode json = MAPPER.readTree(setting.getValue());
        parseSetting(setting.getKey(), json, settings);
        return settings;
    }

    static void parseSetting(String currentKey, JsonNode currentValue, HashMap<String, Object> settings) {
        switch (currentValue.getNodeType()) {
            case ARRAY:
                for (int i = 0; i < currentValue.size(); i++) {
                    String newKey = currentKey + "[" + i + "]";
                    parseSetting(newKey, currentValue.get(i), settings);
                }
                break;
            case OBJECT:
                Iterator<String> fieldNames = currentValue.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    String newKey = currentKey + "." + fieldName;
                    parseSetting(newKey, currentValue.get(fieldName), settings);
                }
                break;
            default:
                settings.put(currentKey, currentValue.asText());
                break;

        }
    }

}
