// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility methods which can be used across different test classes
 */
public final class TestUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestUtils() {
    }

    static String propPair(String propName, String propValue) {
        return String.format("%s=%s", propName, propValue);
    }

    static ConfigurationSetting createItem(String context, String key, String value, String label, String contentType) {
        ConfigurationSetting item = new ConfigurationSetting();
        item.setKey(context + key);
        item.setValue(value);
        item.setLabel(label);
        item.setContentType(contentType);

        return item;
    }

    static FeatureFlagConfigurationSetting createItemFeatureFlag(String context, String key, String value, String label,
        String contentType) {
        FeatureFlagConfigurationSetting item = new FeatureFlagConfigurationSetting(key, true);
        item.setClientFilters(new ArrayList<FeatureFlagFilter>());
        item.setKey(context + key);
        item.setLabel(label);
        item.setContentType(contentType);

        try {
            JsonNode node = MAPPER.readTree(value).get("conditions").get("client_filters");

            for (int i = 0; i < node.size(); i++) {
                JsonNode nodeFilter = node.get(i);
                FeatureFlagFilter filter = new FeatureFlagFilter(nodeFilter.get("Name").asText());

                JsonNode nodeParams = nodeFilter.get("Parameters");
                if (nodeParams != null) {
                    for (int j = 0; j < nodeParams.size(); j++) {
                        // JsonNode param = nodeParams.
                        Map<String, Object> result = MAPPER.convertValue(nodeParams,
                            new TypeReference<Map<String, Object>>() {
                            });
                        Set<String> asdf = result.keySet();
                        for (String paramKey : asdf) {
                            filter.addParameter(paramKey, result.get(paramKey));
                        }
                    }
                }
                item.addClientFilter(filter);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return item;
    }

    static void addStore(AppConfigurationProperties properties, String storeEndpoint, String connectionString) {
        addStore(properties, storeEndpoint, connectionString, null);
    }

    static void addStore(AppConfigurationProperties properties, String storeEndpoint, String connectionString,
        String label) {
        List<ConfigStore> stores = properties.getStores();
        ConfigStore store = new ConfigStore();
        store.setConnectionString(connectionString);
        store.setEndpoint(storeEndpoint);
        store.setLabel(label);
        stores.add(store);
        properties.setStores(stores);
    }
}
