// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationStoreHealth;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class ConnectionManagerTest {

    @Mock
    private AppConfigurationReplicaClientsBuilder clientBuilderMock;

    @Mock
    private AppConfigurationReplicaClient replicaClient1;

    @Mock
    private AppConfigurationReplicaClient replicaClient2;
    
    @Mock
    private ReplicaLookUp replicaLookUpMock;

    private ConnectionManager connectionManager;

    private ConfigStore configStore;
    
    private MockitoSession session;

    @BeforeEach
    public void setup() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        MockitoAnnotations.openMocks(this);

        configStore = new ConfigStore();
        configStore.setEndpoint(TEST_ENDPOINT);

        configStore.validateAndInit();

        connectionManager = null;
    }
    
    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void getStoreIdentifierTest() {
        connectionManager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);

        assertEquals(TEST_ENDPOINT, connectionManager.getMainEndpoint());

        configStore.setEndpoint(null);

        List<String> endpoints = new ArrayList<>();
        endpoints.add("first.endpoint");
        endpoints.add("second.endpoint");

        configStore.setEndpoints(endpoints);
        configStore.validateAndInit();

        connectionManager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);

        assertEquals("first.endpoint", connectionManager.getMainEndpoint());
    }

    @Test
    public void backoffTest() {
        configStore = new ConfigStore();
        List<String> endpoints = new ArrayList<>();

        endpoints.add("https://fake.test.config.io");
        endpoints.add("https://fake.test.geo.config.io");

        configStore.setEndpoints(endpoints);

        configStore.validateAndInit();

        connectionManager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);

        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        clients.add(replicaClient1);
        clients.add(replicaClient2);

        when(clientBuilderMock.buildClients(Mockito.eq(configStore))).thenReturn(clients);
        when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        when(replicaClient2.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));

        String originEndpoint = configStore.getEndpoint();
        String replicaEndpoint = endpoints.get(1);

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

    @Test
    public void updateSyncTokenTest() {
        String fakeToken = "fakeToken";
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);

        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(1));
        clients.add(replicaClient1);

        when(clientBuilderMock.buildClients(Mockito.eq(configStore))).thenReturn(clients);
        when(replicaClient1.getEndpoint()).thenReturn(TEST_ENDPOINT);

        List<AppConfigurationReplicaClient> availableClients = manager.getAvailableClients();
        assertEquals(1, availableClients.size());

        manager.updateSyncToken(TEST_ENDPOINT, fakeToken);

        verify(replicaClient1, times(1)).updateSyncToken(Mockito.eq(fakeToken));
    }
    
    @Test
    public void getAvailableClientsNotLoadedTest() {
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);

        assertEquals(0, manager.getAvailableClients().size());
        assertEquals(AppConfigurationStoreHealth.NOT_LOADED, manager.getHealth());
    }
}
