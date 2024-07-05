// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility methods which can be used across different test classes
 */
public final class TestUtils {
    private static final ClientLogger LOGGER = new ClientLogger(TestUtils.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestUtils() {
    }

    public static String propPair(String propName, String propValue) {
        return String.format("%s=%s", propName, propValue);
    }

    static ConfigurationSetting createItem(String keyFilter, String key, String value, String label,
        String contentType) {
        ConfigurationSetting item = new ConfigurationSetting();
        item.setKey(keyFilter + key);
        item.setValue(value);
        item.setLabel(label);
        item.setContentType(contentType);

        return item;
    }

    static FeatureFlagConfigurationSetting createItemFeatureFlag(String prefix, String key, String value, String label,
         String contentType) {
        return createItemFeatureFlag(prefix, key, value, label, contentType, null);
    }

    static FeatureFlagConfigurationSetting createItemFeatureFlag(String prefix, String key, String value, String label,
        String contentType, String eTag) {
        FeatureFlagConfigurationSetting item = new FeatureFlagConfigurationSetting(key, true);
        item.setValue(value);
        item.setClientFilters(new ArrayList<>());
        item.setKey(prefix + key);
        item.setLabel(label);
        item.setContentType(contentType);
        item.setETag(eTag);

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
                        Set<String> parameters = result.keySet();
                        for (String paramKey : parameters) {
                            filter.addParameter(paramKey, result.get(paramKey));
                        }
                    }
                }
                item.addClientFilter(filter);
            }
        } catch (JsonProcessingException e) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Failed to create FeatureFlagConfigurationSetting.", e);
        }
        return item;
    }

    static SecretReferenceConfigurationSetting createSecretReference(String keyFilter, String key, String value,
        String label, String contentType) {
        SecretReferenceConfigurationSetting item = new SecretReferenceConfigurationSetting(key, value);
        item.setKey(keyFilter + key);
        item.setLabel(label);
        item.setContentType(contentType);

        return item;
    }

    static void addStore(AppConfigurationProperties properties, String storeEndpoint, String connectionString,
        String keyFilter) {
        addStore(properties, storeEndpoint, connectionString, keyFilter, null);
    }

    static void addStore(AppConfigurationProperties properties, String storeEndpoint, String connectionString,
        String keyFilter,
        String label) {
        List<ConfigStore> stores = properties.getStores();
        ConfigStore store = new ConfigStore();
        store.setConnectionString(connectionString);
        store.setEndpoint(storeEndpoint);
        AppConfigurationKeyValueSelector selectedKeys = new AppConfigurationKeyValueSelector().setKeyFilter(keyFilter)
            .setLabelFilter(label);
        List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();
        selects.add(selectedKeys);
        store.setSelects(selects);
        stores.add(store);
        properties.setStores(stores);
    }
}
