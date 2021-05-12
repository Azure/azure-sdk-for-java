// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.entity.ContentType;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class JsonConfigurationParser {

    static boolean isJsonContentType(String contentType) {
        String acceptedMainType = "application";
        String acceptedSubType = "json";

        if (!StringUtils.hasText(contentType)) {
            return false;
        }

        ContentType ct = ContentType.parse(contentType);
        String type = ct.getMimeType();

        if (type.contains("/")) {
            String mainType = type.split("/")[0];
            String subType = type.split("/")[1];

            if (mainType.equalsIgnoreCase(acceptedMainType)) {
                if (subType.contains("+")) {
                    List<String> subtypes = Arrays.asList(subType.split("\\+"));
                    return subtypes.contains(acceptedSubType);
                } else if (subType.equalsIgnoreCase(acceptedSubType)) {
                    return true;
                }
            }
        }

        return false;
    }

    static HashMap<String, Object> parseJsonSetting(ConfigurationSetting setting)
        throws JsonMappingException, JsonProcessingException {
        HashMap<String, Object> settings = new HashMap<String, Object>();
        ObjectMapper jsonMapper = new ObjectMapper();
        JsonNode json = jsonMapper.readTree(setting.getValue());
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
                settings.put(currentKey, currentValue);
                break;
        }
    }

}
