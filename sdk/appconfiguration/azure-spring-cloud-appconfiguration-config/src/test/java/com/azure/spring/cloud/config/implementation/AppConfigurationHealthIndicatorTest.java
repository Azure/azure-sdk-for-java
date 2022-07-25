// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.spring.cloud.config.AppConfigurationRefresh;
import com.azure.spring.cloud.config.health.AppConfigurationHealthIndicator;
import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

public class AppConfigurationHealthIndicatorTest {

    @Mock
    private AppConfigurationRefresh refreshMock;

    @Mock
    private ConfigurationClient client;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void noConfigurationStores() {
        AppConfigurationHealthIndicator indicator = new AppConfigurationHealthIndicator(refreshMock);
        Map<String, AppConfigurationStoreHealth> storeHealth = new HashMap<>();

        when(refreshMock.getAppConfigurationStoresHealth()).thenReturn(storeHealth);

        Health health = indicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals(0, health.getDetails().size());
    }

    @Test
    public void heathlyConfigurationStore() {
        String storeName = "singleHealthyStoreIndicatorTest";

        AppConfigurationHealthIndicator indicator = new AppConfigurationHealthIndicator(refreshMock);
        Map<String, AppConfigurationStoreHealth> storeHealth = new HashMap<>();

        List<ConfigurationClientWrapper> clients = new ArrayList<>();

        clients.add(new ConfigurationClientWrapper(storeName, client));

        storeHealth.put(storeName, AppConfigurationStoreHealth.UP);

        when(refreshMock.getAppConfigurationStoresHealth()).thenReturn(storeHealth);

        Health health = indicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals(1, health.getDetails().size());
        assertEquals("UP", health.getDetails().get(storeName));
    }

    @Test
    public void unloadedConfigurationStore() {
        String storeName = "singleUnloadedStoreIndicatorTest";

        AppConfigurationProperties properties = new AppConfigurationProperties();
        List<ConfigStore> stores = new ArrayList<>();

        ConfigStore store = new ConfigStore();
        store.setEndpoint(storeName);
        store.setEnabled(true);
        stores.add(store);

        properties.setStores(stores);

        AppConfigurationHealthIndicator indicator = new AppConfigurationHealthIndicator(refreshMock);
        
        Map<String,  AppConfigurationStoreHealth> mockHealth = new HashMap<String, AppConfigurationStoreHealth>();
        
        mockHealth.put(storeName, AppConfigurationStoreHealth.NOT_LOADED);
        
        when(refreshMock.getAppConfigurationStoresHealth()).thenReturn(mockHealth);

        Health health = indicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals(1, health.getDetails().size());
        assertEquals("NOT LOADED", health.getDetails().get(storeName));
    }

    @Test
    public void unheathlyConfigurationStore() {
        String storeName = "singleUnhealthyStoreIndicatorTest";

        AppConfigurationHealthIndicator indicator = new AppConfigurationHealthIndicator(refreshMock);

        Map<String, AppConfigurationStoreHealth> healthStatus = new HashMap<>();

        healthStatus.put(storeName, AppConfigurationStoreHealth.DOWN);

        when(refreshMock.getAppConfigurationStoresHealth()).thenReturn(healthStatus);

        Health health = indicator.health();
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(1, health.getDetails().size());
        assertEquals("DOWN", health.getDetails().get(storeName));
    }

}
