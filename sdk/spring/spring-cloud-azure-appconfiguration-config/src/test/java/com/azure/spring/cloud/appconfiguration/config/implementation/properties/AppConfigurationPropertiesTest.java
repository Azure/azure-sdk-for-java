// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_ENDPOINT;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_ENDPOINT_GEO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AppConfigurationPropertiesTest {

    private static final String NO_ENDPOINT_CONN_STRING = "Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";

    private static final String NO_ID_CONN_STRING = "Endpoint=https://fake.test.config.io;Secret=ZmFrZS1jb25uLXNlY3JldA==";

    private static final String NO_SECRET_CONN_STRING = "Endpoint=https://fake.test.config.io;Id=fake-conn-id;";

    private static final String VALID_KEY = "/application/";

    private static final String ILLEGAL_LABELS = "*,my-label";
    
    private AppConfigurationProperties properties;


    @BeforeEach
    public void setup() {
        properties = new AppConfigurationProperties();
        properties.setStores(List.of(new ConfigStore()));
    }

    @Test
    public void validInputShouldCreatePropertiesBean() {
        ConfigStore store = properties.getStores().get(0);
        store.setConnectionString(TEST_CONN_STRING);
        store.validateAndInit();
    }

    @Test
    public void endpointMustExistInConnectionString() {
        testConnStringFields(NO_ENDPOINT_CONN_STRING);
    }

    @Test
    public void idMustExistInConnectionString() {
        testConnStringFields(NO_ID_CONN_STRING);
    }

    @Test
    public void secretMustExistInConnectionString() {
        testConnStringFields(NO_SECRET_CONN_STRING);
    }

    private void testConnStringFields(String connString) {
        ConfigStore store = properties.getStores().get(0);
        store.setConnectionString(connString);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> properties.validateAndInit());
        assertEquals("Connection string does not follow format Endpoint=([^;]+);Id=([^;]+);Secret=([^;]+).", e.getMessage());
    }

    @Test
    public void asteriskShouldNotBeIncludedInTheLabels() {
        ConfigStore store = properties.getStores().get(0);
        store.setConnectionString(TEST_CONN_STRING);
        AppConfigurationKeyValueSelector select = new AppConfigurationKeyValueSelector();
        select.setKeyFilter(VALID_KEY);
        select.setLabelFilter(ILLEGAL_LABELS);
        store.setSelects(List.of(select));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> properties.validateAndInit());
        assertEquals("LabelFilter must not contain asterisk(*)", e.getMessage());
    }

    @Test
    public void storeNameCanBeInitIfConnectionStringConfigured() {
        ConfigStore store = properties.getStores().get(0);
        store.setConnectionString(TEST_CONN_STRING);
        store.setEndpoint("");
        store.validateAndInit();
        assertEquals(1, properties.getStores().size());
        assertEquals("https://fake.test.config.io", properties.getStores().get(0).getEndpoint());
    }

    @Test
    public void duplicateConnectionStringIsNotAllowed() {
        properties = new AppConfigurationProperties();
        properties.setStores(List.of(new ConfigStore(), new ConfigStore()));
        
        ConfigStore store = properties.getStores().get(0);
        store.setConnectionString(TEST_CONN_STRING);
        ConfigStore newStore = properties.getStores().get(1);
        newStore.setConnectionString(TEST_CONN_STRING);
        
        java.lang.IllegalArgumentException e = assertThrows(java.lang.IllegalArgumentException.class, () -> properties.validateAndInit());
        assertEquals("Duplicate store name exists.", e.getMessage());
    }

    @Test
    public void minValidWatchTime() {
        ConfigStore store = properties.getStores().get(0);
        store.setConnectionString(TEST_CONN_STRING);
        properties.setRefreshInterval(Duration.ofSeconds(1));
        properties.validateAndInit();
    }

    @Test
    public void multipleEndpointsTest() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        ConfigStore store = new ConfigStore();
        List<String> endpoints = new ArrayList<>();
        endpoints.add(TEST_ENDPOINT);
        endpoints.add(TEST_ENDPOINT_GEO);

        store.setEndpoints(endpoints);
        List<ConfigStore> stores = new ArrayList<>();
        stores.add(store);

        properties.setStores(stores);
        properties.validateAndInit();

        endpoints.clear();
        endpoints.add(TEST_ENDPOINT);
        endpoints.add(TEST_ENDPOINT);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> properties.validateAndInit());
        assertEquals("Duplicate store name exists.", e.getMessage());
    }
}