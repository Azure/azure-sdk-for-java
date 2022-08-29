// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING_GEO;
import static com.azure.spring.cloud.config.TestConstants.TEST_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;
import com.azure.spring.cloud.config.properties.ConfigStore;

public class ConnectionManagerTest {

    @Mock
    private AppConfigurationReplicaClientsBuilder clientBuilderMock;

    @Mock
    private AppConfigurationReplicaClient replicaClient1;

    @Mock
    private AppConfigurationReplicaClient replicaClient2;

    private ConnectionManager connectionManager;

    private ConfigStore configStore;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        configStore = new ConfigStore();
        configStore.setEndpoint(TEST_ENDPOINT);

        configStore.validateAndInit();

        connectionManager = null;
    }

    @Test
    public void getStoreIdentifierTest() {
        connectionManager = new ConnectionManager(clientBuilderMock, configStore);

        assertEquals(TEST_ENDPOINT, connectionManager.getOriginEndpoint());

        configStore.setEndpoint(null);

        List<String> endpoints = new ArrayList<>();
        endpoints.add("first.endpoint");
        endpoints.add("second.endpoint");

        configStore.setEndpoints(endpoints);
        configStore.validateAndInit();

        connectionManager = new ConnectionManager(clientBuilderMock, configStore);

        assertEquals("first.endpoint", connectionManager.getOriginEndpoint());
    }

    @Test
    @Disabled("Disabled until connection string support is added.")
    public void backoffTest() {
        configStore = new ConfigStore();
        List<String> connectionStrings = new ArrayList<>();

        connectionStrings.add(TEST_CONN_STRING);
        connectionStrings.add(TEST_CONN_STRING_GEO);
        
        //configStore.setConnectionStrings(connectionStrings);

        configStore.validateAndInit();

        connectionManager = new ConnectionManager(clientBuilderMock, configStore);

        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        clients.add(replicaClient1);
        clients.add(replicaClient2);

        when(clientBuilderMock.buildClients(Mockito.eq(configStore))).thenReturn(clients);
        when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        when(replicaClient2.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));

        String originEndpoint = configStore.getEndpoint();
        String replicaEndpoint = AppConfigurationReplicaClientsBuilder
            .getEndpointFromConnectionString(configStore.getConnectionStrings().get(1));

        when(replicaClient1.getEndpoint()).thenReturn(originEndpoint);
        when(replicaClient2.getEndpoint()).thenReturn(replicaEndpoint);

        List<String> expectedEndpoints = new ArrayList<>();
        expectedEndpoints.add(originEndpoint);
        expectedEndpoints.add(replicaEndpoint);

        assertEquals(2, connectionManager.getAvailableClients().size());
        assertEquals(2, connectionManager.getAllEndpoints().size());
        assertTrue(connectionManager.getAllEndpoints().containsAll(expectedEndpoints));
        assertEquals(AppConfigurationStoreHealth.UP, connectionManager.getHealth());

        connectionManager.backoffClient(originEndpoint);

        expectedEndpoints.remove(1);

        when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().plusSeconds(1000));

        assertEquals(1, connectionManager.getAvailableClients().size());
        assertEquals(2, connectionManager.getAllEndpoints().size());
        assertTrue(connectionManager.getAllEndpoints().containsAll(expectedEndpoints));
        assertEquals(AppConfigurationStoreHealth.UP, connectionManager.getHealth());

        connectionManager.backoffClient(originEndpoint);

        assertEquals(1, connectionManager.getAvailableClients().size());
        assertEquals(2, connectionManager.getAllEndpoints().size());
        assertTrue(connectionManager.getAllEndpoints().containsAll(expectedEndpoints));
        assertEquals(AppConfigurationStoreHealth.UP, connectionManager.getHealth());

        connectionManager.backoffClient(replicaEndpoint);
        expectedEndpoints.remove(0);

        when(replicaClient2.getBackoffEndTime()).thenReturn(Instant.now().plusSeconds(1000));

        assertEquals(0, connectionManager.getAvailableClients().size());
        assertEquals(2, connectionManager.getAllEndpoints().size());
        assertTrue(connectionManager.getAllEndpoints().containsAll(expectedEndpoints));
        assertEquals(AppConfigurationStoreHealth.DOWN, connectionManager.getHealth());
    }
}
