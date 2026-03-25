// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

final class JsonConfigurationParser {

    private static final ObjectMapper MAPPER = JsonMapper.builder().enable(JsonReadFeature.ALLOW_JAVA_COMMENTS).build();

    static boolean isJsonContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return false;
        }

        contentType = contentType.strip().toLowerCase();
        String mimeType = contentType.split(";")[0].strip();

        String[] typeParts = mimeType.split("/");
        if (typeParts.length != 2) {
            return false;
        }

        String mainType = typeParts[0];
        String subType = typeParts[1];
        
        if (!"application".equals(mainType)) {
            return false;
        }

        String[] subTypes = subType.split("\\+");
        // Check if the last part (suffix) is "json"
        if (subTypes.length > 0 && subTypes[subTypes.length - 1].equals("json")) {
            return true;
        }

        return false;
    }

    static Map<String, Object> parseJsonSetting(ConfigurationSetting setting)
        throws InvalidConfigurationPropertyValueException {
        Map<String, Object> settings = new HashMap<>();
        try {
            JsonNode json = MAPPER.readTree(setting.getValue());
            parseSetting(setting.getKey(), json, settings);
        } catch (JsonProcessingException e) {
            throw new InvalidConfigurationPropertyValueException(
                setting.getKey(),
                "<Redacted>",
                "Expected type: JSON String, Number, Array, Object or token 'null', 'true' or 'false'");
        }
        return settings;
    }

    static void parseSetting(String currentKey, JsonNode currentValue, Map<String, Object> settings) {
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
