package com.azure.spring.cloud.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

public class ConnectionManagerTest {
    
    private ConnectionManager connectionManager;
    
    private ConfigStore configStore;
    
    private AppConfigurationProviderProperties providerProperties;
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        configStore = new ConfigStore();
        configStore.setEndpoint("test.endpoint");
        
        configStore.validateAndInit();
        
        providerProperties = new AppConfigurationProviderProperties();

        
        connectionManager = new ConnectionManager(configStore, providerProperties, null, null, false, false, null);
    }
    
    @Test
    public void getStoreIdentifierTest() {
        assertEquals("test.endpoint", connectionManager.getStoreIdentifier());
        
        configStore.setEndpoint(null);
        
        List<String> endpoints = new ArrayList<>();
        endpoints.add("first.endpoint");
        endpoints.add("second.endpoint");
        
        configStore.setEndpoints(endpoints);
        configStore.validateAndInit();
        
        connectionManager = new ConnectionManager(configStore, providerProperties, null, null, false, false, null);

        assertEquals("first.endpoint", connectionManager.getStoreIdentifier());
    }
    

}
