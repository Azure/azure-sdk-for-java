// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class AppConfigurationReplicaClientFactoryTest {

    private AppConfigurationReplicaClientFactory clientFactory;

    @Mock
    private AppConfigurationReplicaClientsBuilder clientBuilderMock;
    
    @Mock
    private ReplicaLookUp replicaLookUpMock;

    private final String originEndpoint = "clientFactoryTest.azconfig.io";

    private final String replica1 = "clientFactoryTest-replica1.azconfig.io";

    private final String noReplicaEndpoint = "noReplica.azconfig.io";

    private final String invalidReplica = "invalidReplica.azconfig.io";
    
    private MockitoSession session;

    @BeforeEach
    public void setup() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
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

        clientFactory = new AppConfigurationReplicaClientFactory(clientBuilderMock, stores, replicaLookUpMock);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
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
