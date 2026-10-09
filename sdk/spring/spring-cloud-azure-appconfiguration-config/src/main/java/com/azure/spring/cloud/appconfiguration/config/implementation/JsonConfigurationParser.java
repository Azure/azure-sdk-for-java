// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public final class JsonConfigurationParser {

    private static final ObjectMapper MAPPER = JsonMapper.builder().enable(JsonReadFeature.ALLOW_JAVA_COMMENTS).build();

    public static boolean isJsonContentType(String contentType) {
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
        } catch (JacksonException e) {
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
                for (Map.Entry<String, JsonNode> property : currentValue.properties()) {
                    String newKey = currentKey + "." + property.getKey();
                    parseSetting(newKey, property.getValue(), settings);
                }
                break;
            default:
                settings.put(currentKey, currentValue.asString());
                break;

        }
    }

}
