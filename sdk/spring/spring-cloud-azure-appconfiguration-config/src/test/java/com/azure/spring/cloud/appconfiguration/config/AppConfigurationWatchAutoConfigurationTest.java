// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.bootstrap.BootstrapContext;

import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationReplicaClientFactory;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class AppConfigurationWatchAutoConfigurationTest {

    private AppConfigurationWatchAutoConfiguration autoConfiguration;
    private AppConfigurationProperties properties;
    private BootstrapContext bootstrapContext;
    private AppConfigurationReplicaClientFactory clientFactory;
    private ReplicaLookUp replicaLookUp;

    @BeforeEach
    public void setup() {
        autoConfiguration = new AppConfigurationWatchAutoConfiguration();
        properties = new AppConfigurationProperties();
        properties.setRefreshInterval(Duration.ofSeconds(30));
        
        ConfigStore store = new ConfigStore();
        store.setEndpoint("https://test.azconfig.io");
        List<ConfigStore> stores = new ArrayList<>();
        stores.add(store);
        properties.setStores(stores);
        
        bootstrapContext = mock(BootstrapContext.class);
        clientFactory = mock(AppConfigurationReplicaClientFactory.class);
        replicaLookUp = mock(ReplicaLookUp.class);
    }

    @Test
    public void appConfigurationRefreshBeanIsCreatedWhenDependenciesExist() {
        // Arrange
        when(bootstrapContext.getOrElse(AppConfigurationReplicaClientFactory.class, null))
            .thenReturn(clientFactory);
        when(bootstrapContext.getOrElse(ReplicaLookUp.class, null))
            .thenReturn(replicaLookUp);

        // Act
        AppConfigurationRefresh result = autoConfiguration.appConfigurationRefresh(properties, bootstrapContext);

        // Assert
        assertNotNull(result, "AppConfigurationRefresh bean should be created when dependencies exist");
    }

    @Test
    public void appConfigurationRefreshBeanIsNotCreatedWhenClientFactoryIsMissing() {
        // Arrange
        when(bootstrapContext.getOrElse(AppConfigurationReplicaClientFactory.class, null))
            .thenReturn(null);
        when(bootstrapContext.getOrElse(ReplicaLookUp.class, null))
            .thenReturn(replicaLookUp);

        // Act
        AppConfigurationRefresh result = autoConfiguration.appConfigurationRefresh(properties, bootstrapContext);

        // Assert
        assertNull(result, "AppConfigurationRefresh bean should not be created when clientFactory is missing");
    }

    @Test
    public void appConfigurationRefreshBeanIsNotCreatedWhenReplicaLookUpIsMissing() {
        // Arrange
        when(bootstrapContext.getOrElse(AppConfigurationReplicaClientFactory.class, null))
            .thenReturn(clientFactory);
        when(bootstrapContext.getOrElse(ReplicaLookUp.class, null))
            .thenReturn(null);

        // Act
        AppConfigurationRefresh result = autoConfiguration.appConfigurationRefresh(properties, bootstrapContext);

        // Assert
        assertNull(result, "AppConfigurationRefresh bean should not be created when replicaLookUp is missing");
    }

    @Test
    public void appConfigurationRefreshBeanIsNotCreatedWhenBothDependenciesAreMissing() {
        // Arrange
        when(bootstrapContext.getOrElse(AppConfigurationReplicaClientFactory.class, null))
            .thenReturn(null);
        when(bootstrapContext.getOrElse(ReplicaLookUp.class, null))
            .thenReturn(null);

        // Act
        AppConfigurationRefresh result = autoConfiguration.appConfigurationRefresh(properties, bootstrapContext);

        // Assert
        assertNull(result, "AppConfigurationRefresh bean should not be created when both dependencies are missing");
    }
}
