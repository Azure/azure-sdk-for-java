// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

public class ClientFactoryTest {

    private ClientFactory clientFactory;

    @Mock
    private ConnectionManager connectionManagerMock;

    private AppConfigurationProperties properties;

    private AppConfigurationProviderProperties clientProperties;

    private String originEndpoint = "clientfactorytest.azconfig.io";

    private String replica1 = "clientfactorytest-replica1.azconfig.io";

    private String noReplicaEndpoint = "noReplica.azconfig.io";

    private String invalidReplica = "invalidreplica.azconfig.io";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        List<ConfigStore> stores = new ArrayList<>();

        ConfigStore store = new ConfigStore();
        List<String> endpoints = new ArrayList<>();

        endpoints.add(originEndpoint);
        endpoints.add(replica1);

        store.setEndpoint(originEndpoint);
        store.setEndpoints(endpoints);
        stores.add(store);

        ConfigStore storeNoReplica = new ConfigStore();
        storeNoReplica.setEndpoint(noReplicaEndpoint);
        stores.add(storeNoReplica);

        properties = new AppConfigurationProperties();
        properties.setStores(stores);

        HashMap<String, ConnectionManager> connections = new HashMap<>();
        connections.put(originEndpoint, connectionManagerMock);

        clientFactory = new ClientFactory(properties, clientProperties, null, null, false, false);
    }

    @Test
    public void findOriginTest() {
        assertEquals(originEndpoint, clientFactory.findOriginForEndpoint(originEndpoint));
        assertEquals(originEndpoint, clientFactory.findOriginForEndpoint(replica1));
        assertEquals(noReplicaEndpoint, clientFactory.findOriginForEndpoint(noReplicaEndpoint));

        // If a replica isn't found return itself
        assertEquals(invalidReplica, clientFactory.findOriginForEndpoint(invalidReplica));
    }

    @Test
    public void hasReplicasTest() {
        assertTrue(clientFactory.hasReplicas(originEndpoint));
        assertTrue(clientFactory.hasReplicas(replica1));
        assertFalse(clientFactory.hasReplicas(invalidReplica));
        assertFalse(clientFactory.hasReplicas(noReplicaEndpoint));
    }

}
