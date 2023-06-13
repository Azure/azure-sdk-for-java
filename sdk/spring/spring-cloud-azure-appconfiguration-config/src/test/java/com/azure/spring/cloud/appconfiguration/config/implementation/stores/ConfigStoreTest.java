// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.stores;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class ConfigStoreTest {

    @Test
    public void invalidLabel() {
        ConfigStore configStore = new ConfigStore();
        AppConfigurationKeyValueSelector selectedKeys =
            new AppConfigurationKeyValueSelector().setKeyFilter("/application/").setLabelFilter("*");
        List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();
        selects.add(selectedKeys);
        configStore.setSelects(selects);

        assertThrows(IllegalArgumentException.class, configStore::validateAndInit);
    }

    @Test
    public void invalidKey() {
        ConfigStore configStore = new ConfigStore();
        AppConfigurationKeyValueSelector selectedKeys =
            new AppConfigurationKeyValueSelector().setKeyFilter("/application/*");
        List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();
        selects.add(selectedKeys);
        configStore.setSelects(selects);

        assertThrows(IllegalArgumentException.class, configStore::validateAndInit);
    }

    @Test
    public void invalidEndpoint() {
        ConfigStore configStore = new ConfigStore();
        configStore.validateAndInit();
        configStore.setConnectionString("Endpoint=a^a;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==");

        assertThrows(IllegalStateException.class, configStore::validateAndInit);
    }

    @Test
    public void getLabelsTest() {
        ConfigStore configStore = new ConfigStore();
        configStore.validateAndInit();

        assertEquals("\0", configStore.getSelects().get(0).getLabelFilter(new ArrayList<>())[0]);

        AppConfigurationKeyValueSelector selectedKeys =
            new AppConfigurationKeyValueSelector().setKeyFilter("/application/").setLabelFilter("dev");
        List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();
        selects.add(selectedKeys);
        configStore.setSelects(selects);
        assertEquals("dev", configStore.getSelects().get(0).getLabelFilter(new ArrayList<>())[0]);

        selectedKeys = new AppConfigurationKeyValueSelector().setKeyFilter("/application/").setLabelFilter("dev,test");
        selects = new ArrayList<>();
        selects.add(selectedKeys);
        configStore.setSelects(selects);
        assertEquals("test", configStore.getSelects().get(0).getLabelFilter(new ArrayList<>())[0]);
        assertEquals("dev", configStore.getSelects().get(0).getLabelFilter(new ArrayList<>())[1]);

        selectedKeys = new AppConfigurationKeyValueSelector().setKeyFilter("/application/").setLabelFilter(",");
        selects = new ArrayList<>();
        selects.add(selectedKeys);
        configStore.setSelects(selects);
        assertEquals("\0", configStore.getSelects().get(0).getLabelFilter(new ArrayList<>())[0]);
    }

    @Test
    public void testContainsEndpoint() {
        ConfigStore store = new ConfigStore();
        store.setEndpoint("endpoint");
        store.validateAndInit();
        assertTrue(store.containsEndpoint("endpoint"));
        assertFalse(store.containsEndpoint("invalidEndpoint"));

        store = new ConfigStore();
        List<String> endpoints = new ArrayList<>();
        endpoints.add("endpoint");
        endpoints.add("secondEndpoint");
        store.setEndpoints(endpoints);
        store.validateAndInit();
        assertTrue(store.containsEndpoint("endpoint"));
        assertTrue(store.containsEndpoint("secondEndpoint"));
        assertFalse(store.containsEndpoint("invalidEndpoint"));
    }

    @Test
    public void testValidateConnectionString() {
        ConfigStore store = new ConfigStore();
        store.setConnectionString("Endpoint=https://endpoint.io;Id=identifier;Secret=secret=");
        store.validateAndInit();

        store = new ConfigStore();
        List<String> connectionStrings = new ArrayList<>();
        connectionStrings.add("Endpoint=https://endpoint.io;Id=identifier;Secret=secret=");
        connectionStrings.add("Endpoint=https://endpoint2.io;Id=identifier;Secret=secret=");
        store.setConnectionStrings(connectionStrings);
        store.validateAndInit();
    }

    @Test
    public void testValidateConnectionStringInvalid() {
        ConfigStore store = new ConfigStore();
        List<String> connectionStrings = new ArrayList<>();
        connectionStrings.add("Endpoint=endpoint;Id=identifier;Secret=secret=");
        store.setConnectionStrings(connectionStrings);
        assertThrows(IllegalStateException.class, () -> store.validateAndInit());
    }
}
