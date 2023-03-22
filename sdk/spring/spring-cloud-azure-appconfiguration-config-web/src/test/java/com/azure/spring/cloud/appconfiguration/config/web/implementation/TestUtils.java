// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.web.implementation;

import java.util.ArrayList;
import java.util.List;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

/**
 * Utility methods which can be used across different test classes
 */
public final class TestUtils {

    private TestUtils() {
    }

    public static String propPair(String propName, String propValue) {
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

    static void addStore(AppConfigurationProperties properties, String storeEndpoint, String connectionString) {
        addStore(properties, storeEndpoint, connectionString, null);
    }

    static void addStore(AppConfigurationProperties properties, String storeEndpoint, String connectionString,
        String label) {
        List<ConfigStore> stores = properties.getStores();
        ConfigStore store = new ConfigStore();
        store.setConnectionString(connectionString);
        store.setEndpoint(storeEndpoint);
        AppConfigurationKeyValueSelector selectedKeys = new AppConfigurationKeyValueSelector().setKeyFilter("/application/").setLabelFilter(label);
        List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();
        selects.add(selectedKeys);
        store.setSelects(selects);
        stores.add(store);
        properties.setStores(stores);
    }
}
