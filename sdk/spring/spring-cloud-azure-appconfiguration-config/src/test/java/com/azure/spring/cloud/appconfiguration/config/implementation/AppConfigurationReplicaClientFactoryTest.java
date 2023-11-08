// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class AppConfigurationReplicaClientFactoryTest {

    private AppConfigurationReplicaClientFactory clientFactory;

    @Mock
    private AppConfigurationReplicaClientsBuilder clientBuilderMock;

    private final String originEndpoint = "clientFactoryTest.azconfig.io";

    private final String replica1 = "clientFactoryTest-replica1.azconfig.io";

    private final String noReplicaEndpoint = "noReplica.azconfig.io";

    private final String invalidReplica = "invalidReplica.azconfig.io";

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

        clientFactory = new AppConfigurationReplicaClientFactory(clientBuilderMock, stores);
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
