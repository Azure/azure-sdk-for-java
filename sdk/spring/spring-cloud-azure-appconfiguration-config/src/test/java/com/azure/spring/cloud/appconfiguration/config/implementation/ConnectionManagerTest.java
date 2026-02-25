// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationStoreHealth;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_ENDPOINT;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;

public class ConnectionManagerTest {

    @Mock
    private AppConfigurationReplicaClientsBuilder clientBuilderMock;

    @Mock
    private AppConfigurationReplicaClient replicaClient1;

    @Mock
    private AppConfigurationReplicaClient replicaClient2;
    
    @Mock
    private AppConfigurationReplicaClient autoFailoverClient;
    
    @Mock
    private ReplicaLookUp replicaLookUpMock;
    
    @Mock
    private AppConfigurationStoreMonitoring monitoringMock;
    
    @Mock
    private FeatureFlagStore featureFlagStoreMock;

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
        assertEquals(AppConfigurationStoreHealth.UP, connectionManager.getHealth());

        connectionManager.backoffClient(originEndpoint);

        expectedEndpoints.remove(1);

        when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().plusSeconds(1000));

        assertEquals(1, connectionManager.getAvailableClients().size());
        assertEquals(AppConfigurationStoreHealth.UP, connectionManager.getHealth());

        connectionManager.backoffClient(originEndpoint);

        assertEquals(1, connectionManager.getAvailableClients().size());
        assertEquals(AppConfigurationStoreHealth.UP, connectionManager.getHealth());

        connectionManager.backoffClient(replicaEndpoint);
        expectedEndpoints.remove(0);

        when(replicaClient2.getBackoffEndTime()).thenReturn(Instant.now().plusSeconds(1000));

        assertEquals(0, connectionManager.getAvailableClients().size());
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
    
    /**
     * Tests the getNextActiveClient method when the activeClients list is empty.
     */
    @Test
    public void getNextActiveClientEmptyTest() {
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        
        // No active clients available
        assertNull(manager.getNextActiveClient(false));
        
        // Verify lastActiveClient is empty
        try {
            java.lang.reflect.Field lastActiveClientField = ConnectionManager.class.getDeclaredField("lastActiveClient");
            lastActiveClientField.setAccessible(true);
            assertEquals("", lastActiveClientField.get(manager));
        } catch (Exception e) {
            throw new RuntimeException("Test verification failed", e);
        }
    }
    
    /**
     * Tests the getNextActiveClient method when load balancing is disabled.
     */
    @Test
    public void getNextActiveClientWithoutLoadBalancingTest() {
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        
        // Set up: Load balancing disabled, one client available
        configStore.setLoadBalancingEnabled(false);
        
        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        clients.add(replicaClient1);
        
        // Set the private field activeClients
        try {
            java.lang.reflect.Field activeClientsField = ConnectionManager.class.getDeclaredField("activeClients");
            activeClientsField.setAccessible(true);
            activeClientsField.set(manager, clients);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test", e);
        }
        
        // When load balancing is disabled, should return the first client without removing it
        assertSame(replicaClient1, manager.getNextActiveClient(false));
        
        // activeClients list should still have the client
        try {
            java.lang.reflect.Field activeClientsField = ConnectionManager.class.getDeclaredField("activeClients");
            activeClientsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<AppConfigurationReplicaClient> remainingClients = 
                (List<AppConfigurationReplicaClient>) activeClientsField.get(manager);
            assertEquals(1, remainingClients.size());
        } catch (Exception e) {
            throw new RuntimeException("Test verification failed", e);
        }
    }
    
    /**
     * Tests the getNextActiveClient method when load balancing is enabled.
     */
    @Test
    public void getNextActiveClientWithLoadBalancingTest() {
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        
        // Set up: Load balancing enabled, two clients available
        configStore.setLoadBalancingEnabled(true);
        
        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        clients.add(replicaClient1);
        clients.add(replicaClient2);
        
        when(replicaClient1.getEndpoint()).thenReturn("endpoint1");
        
        // Set the private field activeClients
        try {
            java.lang.reflect.Field activeClientsField = ConnectionManager.class.getDeclaredField("activeClients");
            activeClientsField.setAccessible(true);
            activeClientsField.set(manager, clients);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test", e);
        }
        
        // With load balancing enabled, should return and remove the first client
        assertSame(replicaClient1, manager.getNextActiveClient(false));
        
        // Verify lastActiveClient is set correctly
        try {
            java.lang.reflect.Field lastActiveClientField = ConnectionManager.class.getDeclaredField("lastActiveClient");
            lastActiveClientField.setAccessible(true);
            assertEquals("endpoint1", lastActiveClientField.get(manager));
        } catch (Exception e) {
            throw new RuntimeException("Test verification failed", e);
        }
        
        // activeClients list should now only have the second client
        try {
            java.lang.reflect.Field activeClientsField = ConnectionManager.class.getDeclaredField("activeClients");
            activeClientsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<AppConfigurationReplicaClient> remainingClients = 
                (List<AppConfigurationReplicaClient>) activeClientsField.get(manager);
            assertEquals(1, remainingClients.size());
            assertSame(replicaClient2, remainingClients.get(0));
        } catch (Exception e) {
            throw new RuntimeException("Test verification failed", e);
        }
    }
    
    /**
     * Test the rotation behavior of findActiveClients by testing getNextActiveClient before and after
     * emptying activeClients to force findActiveClients to run.
     */
    @Test
    public void testFindActiveClientsRotation() {
        // Setup test data
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        configStore.setLoadBalancingEnabled(true);
        
        List<AppConfigurationReplicaClient> testClients = new ArrayList<>();
        testClients.add(replicaClient1);
        testClients.add(replicaClient2);
        
        // Make the stubbings lenient to avoid unnecessary stubbing exceptions
        Mockito.lenient().when(replicaClient1.getEndpoint()).thenReturn("endpoint1");
        Mockito.lenient().when(replicaClient2.getEndpoint()).thenReturn("endpoint2");
        Mockito.lenient().when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        Mockito.lenient().when(replicaClient2.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        
        // Set up clientBuilder to return our test clients
        when(clientBuilderMock.buildClients(Mockito.any())).thenReturn(testClients);
        
        try {
            // Set lastActiveClient to endpoint1
            java.lang.reflect.Field lastActiveClientField = ConnectionManager.class.getDeclaredField("lastActiveClient");
            lastActiveClientField.setAccessible(true);
            lastActiveClientField.set(manager, "endpoint1");
            
            // Make sure clients field is null so getAvailableClients builds the list from clientBuilder
            java.lang.reflect.Field clientsField = ConnectionManager.class.getDeclaredField("clients");
            clientsField.setAccessible(true);
            clientsField.set(manager, null);
            
            // Call getAvailableClients to verify the setup and populate the clients list
            List<AppConfigurationReplicaClient> availableClients = manager.getAvailableClients();
            assertEquals(2, availableClients.size(), "Should have 2 available clients");
            
            // Set empty activeClients list to force findActiveClients to run
            java.lang.reflect.Field activeClientsField = ConnectionManager.class.getDeclaredField("activeClients");
            activeClientsField.setAccessible(true);
            activeClientsField.set(manager, new ArrayList<>());
            
            // Call getNextActiveClient - this will trigger findActiveClients internally
            AppConfigurationReplicaClient nextClient = manager.getNextActiveClient(false);
            // There are only 2 clients and the active list hasn't been reset.
            assertNull(nextClient);
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }    /**
     * Test the behavior of findActiveClients when load balancing is disabled.
     */
    @Test
    public void testFindActiveClientsWithoutLoadBalancing() {
        // Setup test data
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        configStore.setLoadBalancingEnabled(false);
        
        List<AppConfigurationReplicaClient> testClients = new ArrayList<>();
        testClients.add(replicaClient1);
        testClients.add(replicaClient2);
        
        // Make the stubbings lenient to avoid unnecessary stubbing exceptions
        Mockito.lenient().when(replicaClient1.getEndpoint()).thenReturn("endpoint1");
        Mockito.lenient().when(replicaClient2.getEndpoint()).thenReturn("endpoint2");
        Mockito.lenient().when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        Mockito.lenient().when(replicaClient2.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        
        // Set the clients field directly for the test
        try {
            // Set clients field
            java.lang.reflect.Field clientsField = ConnectionManager.class.getDeclaredField("clients");
            clientsField.setAccessible(true);
            clientsField.set(manager, new ArrayList<>(testClients));
            
            // Set activeClients to empty to force findActiveClients to run
            java.lang.reflect.Field activeClientsField = ConnectionManager.class.getDeclaredField("activeClients");
            activeClientsField.setAccessible(true);
            activeClientsField.set(manager, new ArrayList<>());
            
            // Call findActiveClients directly
            manager.findActiveClients();
            
            // Call getNextActiveClient - this should return the first client
            AppConfigurationReplicaClient nextClient = manager.getNextActiveClient(false);
            assertEquals("endpoint1", nextClient.getEndpoint(), "Should return first client without removing it");
            
            // Verify activeClients still has both clients (no removal happens when load balancing is disabled)
            @SuppressWarnings("unchecked")
            List<AppConfigurationReplicaClient> remainingClients = 
                (List<AppConfigurationReplicaClient>) activeClientsField.get(manager);
            assertEquals(2, remainingClients.size(), "Both clients should still be in the list");
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }


    /**
     * Tests the getMonitoring method.
     */
    @Test
    public void getMonitoringTest() {
        configStore.setMonitoring(monitoringMock);
        configStore.setFeatureFlags(featureFlagStoreMock);
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        
        assertEquals(monitoringMock, manager.getMonitoring());
        assertEquals(featureFlagStoreMock, manager.getFeatureFlagStore());
    }
    
    /**
     * Tests auto-failover client functionality when all configured clients are backed off.
     */
    @Test
    public void getAvailableClientsWithAutoFailoverTest() {
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        
        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        clients.add(replicaClient1);
        
        // All regular clients are backed off
        when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().plusSeconds(1000));
        when(clientBuilderMock.buildClients(Mockito.eq(configStore))).thenReturn(clients);
        
        // Mock auto-failover endpoints
        List<String> autoFailoverEndpoints = new ArrayList<>();
        String failoverEndpoint = "https://failover.test.config.io";
        autoFailoverEndpoints.add(failoverEndpoint);
        when(replicaLookUpMock.getAutoFailoverEndpoints(Mockito.eq(TEST_ENDPOINT))).thenReturn(autoFailoverEndpoints);
        
        // Mock auto-failover client
        when(autoFailoverClient.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        when(clientBuilderMock.buildClient(Mockito.eq(failoverEndpoint), Mockito.eq(configStore))).thenReturn(autoFailoverClient);
        
        List<AppConfigurationReplicaClient> availableClients = manager.getAvailableClients();
        
        assertEquals(1, availableClients.size());
        assertEquals(autoFailoverClient, availableClients.get(0));
        assertEquals(AppConfigurationStoreHealth.UP, manager.getHealth());
    }
    
    /**
     * Tests auto-failover client backoff functionality.
     */
    @Test
    public void backoffAutoFailoverClientTest() {
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        
        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        when(clientBuilderMock.buildClients(Mockito.eq(configStore))).thenReturn(clients);
        
        // Set up auto-failover scenario
        List<String> autoFailoverEndpoints = new ArrayList<>();
        String failoverEndpoint = "https://failover.test.config.io";
        autoFailoverEndpoints.add(failoverEndpoint);
        when(replicaLookUpMock.getAutoFailoverEndpoints(Mockito.eq(TEST_ENDPOINT))).thenReturn(autoFailoverEndpoints);
        
        when(autoFailoverClient.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        when(autoFailoverClient.getFailedAttempts()).thenReturn(1);
        when(clientBuilderMock.buildClient(Mockito.eq(failoverEndpoint), Mockito.eq(configStore))).thenReturn(autoFailoverClient);
        
        // First call should add the auto-failover client
        List<AppConfigurationReplicaClient> availableClients = manager.getAvailableClients();
        assertEquals(1, availableClients.size());
        
        // Now backoff the auto-failover client
        manager.backoffClient(failoverEndpoint);
        
        verify(autoFailoverClient, times(1)).updateBackoffEndTime(Mockito.any(Instant.class));
        verify(autoFailoverClient, times(1)).getFailedAttempts();
    }

    /**
     * Tests getNextActiveClient when useLastActive is true and lastActiveClient exists.
     */
    @Test
    public void getNextActiveClientWithLastActiveTest() {
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        configStore.setLoadBalancingEnabled(true);
        
        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        clients.add(replicaClient1);
        clients.add(replicaClient2);
        
        when(replicaClient1.getEndpoint()).thenReturn("endpoint1");
        when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        when(replicaClient2.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        
        when(clientBuilderMock.buildClients(Mockito.any())).thenReturn(clients);

        manager.findActiveClients();
        // When useLastActive is true and lastActiveClient matches, should return that client
        AppConfigurationReplicaClient result = manager.getNextActiveClient(false);
        assertEquals("endpoint1", result.getEndpoint());

        result = manager.getNextActiveClient(true);
        assertEquals("endpoint1", result.getEndpoint());
    }
    
    /**
     * Tests findActiveClients with complex rotation scenario.
     */
    @Test
    public void findActiveClientsComplexRotationTest() {
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        configStore.setLoadBalancingEnabled(true);
        
        // Create a third client for more complex rotation testing
        AppConfigurationReplicaClient replicaClient3 = Mockito.mock(AppConfigurationReplicaClient.class);
        
        List<AppConfigurationReplicaClient> testClients = new ArrayList<>();
        testClients.add(replicaClient1);
        testClients.add(replicaClient2);
        testClients.add(replicaClient3);

        when(clientBuilderMock.buildClients(Mockito.eq(configStore))).thenReturn(testClients);

        when(replicaClient1.getEndpoint()).thenReturn("endpoint1");
        when(replicaClient2.getEndpoint()).thenReturn("endpoint2");
        when(replicaClient3.getEndpoint()).thenReturn("endpoint3");
        when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        when(replicaClient2.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        when(replicaClient3.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        
        manager.findActiveClients();
            
        assertEquals("endpoint1", manager.getNextActiveClient(false).getEndpoint());
        assertEquals("endpoint2", manager.getNextActiveClient(false).getEndpoint());
        assertEquals("endpoint3", manager.getNextActiveClient(false).getEndpoint());
    }
    
    /**
     * Tests load balancing behavior with mixed available and backed-off clients.
     */
    @Test
    public void getAvailableClientsWithLoadBalancingMixedBackoffTest() {
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        configStore.setLoadBalancingEnabled(true);
        
        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        clients.add(replicaClient1);
        clients.add(replicaClient2);
        
        // First client is backed off, second is available
        when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().plusSeconds(1000));
        when(replicaClient2.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        when(clientBuilderMock.buildClients(Mockito.eq(configStore))).thenReturn(clients);
        
        // Mock auto-failover endpoints (but they should also be backed off)
        List<String> autoFailoverEndpoints = new ArrayList<>();
        String failoverEndpoint = "https://failover.test.config.io";
        autoFailoverEndpoints.add(failoverEndpoint);
        when(replicaLookUpMock.getAutoFailoverEndpoints(Mockito.eq(TEST_ENDPOINT))).thenReturn(autoFailoverEndpoints);
        
        when(autoFailoverClient.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(30));
        when(clientBuilderMock.buildClient(Mockito.eq(failoverEndpoint), Mockito.eq(configStore))).thenReturn(autoFailoverClient);
        
        List<AppConfigurationReplicaClient> availableClients = manager.getAvailableClients();
        
        // Should include both the available regular client and the auto-failover client when load balancing is enabled
        assertEquals(2, availableClients.size());
        assertEquals(AppConfigurationStoreHealth.UP, manager.getHealth());
    }
    
    /**
     * Tests single client scenario without load balancing.
     */
    @Test 
    public void getAvailableClientsSingleClientTest() {
        ConnectionManager manager = new ConnectionManager(clientBuilderMock, configStore, replicaLookUpMock);
        configStore.setLoadBalancingEnabled(false);
        
        List<AppConfigurationReplicaClient> clients = new ArrayList<>();
        clients.add(replicaClient1);
        
        when(replicaClient1.getBackoffEndTime()).thenReturn(Instant.now().minusSeconds(60));
        when(clientBuilderMock.buildClients(Mockito.eq(configStore))).thenReturn(clients);
        // For single client without load balancing, auto-failover won't be called unless client is unavailable
        
        List<AppConfigurationReplicaClient> availableClients = manager.getAvailableClients();
        
        assertEquals(1, availableClients.size());
        assertEquals(replicaClient1, availableClients.get(0));
        assertEquals(AppConfigurationStoreHealth.UP, manager.getHealth());
    }

    
}
