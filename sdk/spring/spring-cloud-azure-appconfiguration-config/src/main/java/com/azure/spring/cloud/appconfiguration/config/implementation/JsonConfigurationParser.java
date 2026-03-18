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
        String acceptedMainType = "application";
        String acceptedSubType = "json";

        if (!StringUtils.hasText(contentType)) {
            return false;
        }

        // Remove parameters like "; charset=utf-8" if present, without using regex-based split
        String cleanContentType;
        int semicolonIndex = contentType.indexOf(';');
        if (semicolonIndex >= 0) {
            cleanContentType = contentType.substring(0, semicolonIndex).trim();
        } else {
            cleanContentType = contentType.trim();
        }

        if (cleanContentType.isEmpty()) {
            return false;
        }

        int slashIndex = cleanContentType.indexOf('/');
        if (slashIndex <= 0 || slashIndex == cleanContentType.length() - 1) {
            // No slash, slash at start, or no subtype
            return false;
        }

        String mainType = cleanContentType.substring(0, slashIndex);
        String subType = cleanContentType.substring(slashIndex + 1);

        // RFC 7231/6838: tokens cannot contain whitespace
        // Check for internal whitespace (after initial trim of the whole content type)
        if (mainType.contains(" ") || mainType.contains("\t") || subType.contains(" ") || subType.contains("\t")) {
            return false;
        }

        if (!mainType.equalsIgnoreCase(acceptedMainType) || subType.isEmpty()) {
            return false;
        }

        // Handle structured syntax suffixes like "application/vnd.api+json".
        // According to RFC 6839, the suffix is the part after the last '+'.
        int plusIndex = subType.lastIndexOf('+');
        if (plusIndex >= 0 && plusIndex < subType.length() - 1) {
            String suffix = subType.substring(plusIndex + 1);
            return suffix.equalsIgnoreCase(acceptedSubType);
        } else {
            return subType.equalsIgnoreCase(acceptedSubType);
        }
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
