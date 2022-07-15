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

import com.azure.spring.cloud.config.AppConfigurationRefresh;
import com.azure.spring.cloud.config.health.AppConfigurationHealthIndicator;
import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

public class AppConfigurationHealthIndicatorTest {
    
    @Mock
    private AppConfigurationRefresh refreshMock;
    
    @BeforeEach
    public void setup(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void noConfigurationStores() {
        AppConfigurationProperties properties = new AppConfigurationProperties();
        AppConfigurationRefresh refresh = new AppConfigurationPullRefresh(properties, null, null);
        AppConfigurationHealthIndicator indicator = new AppConfigurationHealthIndicator(refresh);
        
        Health health = indicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals(0, health.getDetails().size());
    }
    
    @Test
    public void heathlyConfigurationStore() {
        String storeName = "singleHealthyStoreIndicatorTest";
        
        AppConfigurationProperties properties = new AppConfigurationProperties();
        List<ConfigStore> stores = new ArrayList<>();
        
        ConfigStore store = new ConfigStore();
        store.setEndpoint(storeName);
        store.setEnabled(true);
        stores.add(store);
        
        properties.setStores(stores);
        
        StateHolder state = new StateHolder();
        
        state.setLoadState(storeName, true);
        
        StateHolder.updateState(state);
        
        AppConfigurationRefresh refresh = new AppConfigurationPullRefresh(properties, null, null);
        AppConfigurationHealthIndicator indicator = new AppConfigurationHealthIndicator(refresh);
        
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
        
        AppConfigurationRefresh refresh = new AppConfigurationPullRefresh(properties, null, null);
        AppConfigurationHealthIndicator indicator = new AppConfigurationHealthIndicator(refresh);
        
        Health health = indicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals(1, health.getDetails().size());
        assertEquals("NOT LOADED", health.getDetails().get(storeName));
    }
    
    @Test
    public void unheathlyConfigurationStore() {
        String storeName = "singleUnhealthyStoreIndicatorTest";
        
        StateHolder state = new StateHolder();
        
        state.setLoadState(storeName, true);
        
        StateHolder.updateState(state);
        
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
