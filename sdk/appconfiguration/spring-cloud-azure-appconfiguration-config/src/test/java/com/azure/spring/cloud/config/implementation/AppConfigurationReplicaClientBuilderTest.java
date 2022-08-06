// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_CONN_STRING_GEO;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_ENDPOINT;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_ENDPOINT_GEO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.spring.cloud.config.AppConfigurationCredentialProvider;
import com.azure.spring.cloud.config.ConfigurationClientBuilderSetup;
import com.azure.spring.cloud.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.implementation.properties.ConfigStore;

public class AppConfigurationReplicaClientBuilderTest {

    @Mock
    private ConfigurationClientBuilder builderMock;

    @Mock
    private AppConfigurationCredentialProvider tokenProviderMock;

    @Mock
    private TokenCredential credentialMock;

    @Mock
    private ConfigurationClientBuilderSetup modifierMock;

    AppConfigurationReplicaClientsBuilder clientBuilder;

    private ConfigStore configStore;

    private AppConfigurationProviderProperties providerProperties;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        configStore = new ConfigStore();
        configStore.setEndpoint(TEST_ENDPOINT);

        configStore.validateAndInit();

        providerProperties = new AppConfigurationProviderProperties();
        providerProperties.setDefaultMaxBackoff((long) 1000);
        providerProperties.setDefaultMinBackoff((long) 1000);

        clientBuilder = null;
    }

    @Test
    public void buildClientFromEndpointTest() {
        clientBuilder = new AppConfigurationReplicaClientsBuilder(tokenProviderMock, null, false, "", 0);
        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        AppConfigurationReplicaClient replicaClient = spy.buildClients(configStore).get(0);

        assertNotNull(replicaClient);
        assertTrue(replicaClient.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, replicaClient.getEndpoint());
        assertEquals(0, replicaClient.getFailedAttempts());
    }

    @Test
    public void buildClientFromEndpointWithTokenCredentialTest() {
        clientBuilder = new AppConfigurationReplicaClientsBuilder(tokenProviderMock, null, false, "", 0);

        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);
        when(tokenProviderMock.getAppConfigCredential(Mockito.eq(TEST_ENDPOINT))).thenReturn(credentialMock);

        AppConfigurationReplicaClient replicaClient = spy.buildClients(configStore).get(0);

        assertNotNull(replicaClient);
        assertTrue(replicaClient.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, replicaClient.getEndpoint());
        assertEquals(0, replicaClient.getFailedAttempts());

        verify(tokenProviderMock, times(1)).getAppConfigCredential(Mockito.anyString());
        verify(builderMock, times(1)).credential(Mockito.eq(credentialMock));
    }

    @Test
    public void buildClientFromEndpointClientIdTest() {
        String clientId = "1234-5678-9012-3456";
        clientBuilder = new AppConfigurationReplicaClientsBuilder(tokenProviderMock, null, false, clientId, 0);

        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        AppConfigurationReplicaClient replicaClient = spy.buildClients(configStore).get(0);

        assertNotNull(replicaClient);
        assertTrue(replicaClient.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, replicaClient.getEndpoint());
        assertEquals(0, replicaClient.getFailedAttempts());

        verify(builderMock, times(1)).credential(Mockito.any(ManagedIdentityCredential.class));
    }

    @Test
    public void buildClientFromConnectionStringTest() {
        configStore.setEndpoint(null);
        configStore.setConnectionString(TEST_CONN_STRING);
        configStore.validateAndInit();

        clientBuilder = new AppConfigurationReplicaClientsBuilder(tokenProviderMock, null, false, "", 0);
        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq("test.endpoint"))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        List<AppConfigurationReplicaClient> clients = spy.buildClients(configStore);

        assertNotNull(clients.get(0));
        assertTrue(clients.get(0).getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, clients.get(0).getEndpoint());
        assertEquals(0, clients.get(0).getFailedAttempts());
        assertEquals(1, clients.size());
    }

    @Test
    public void modifyClientTest() {
        clientBuilder = new AppConfigurationReplicaClientsBuilder(tokenProviderMock, modifierMock, false, "", 0);

        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        AppConfigurationReplicaClient replicaClient = spy.buildClients(configStore).get(0);

        assertNotNull(replicaClient);
        assertTrue(replicaClient.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, replicaClient.getEndpoint());
        assertEquals(0, replicaClient.getFailedAttempts());

        verify(modifierMock, times(1)).setup(Mockito.eq(builderMock), Mockito.eq(TEST_ENDPOINT));
    }

    @Test
    public void buildClientsFromMultipleEndpointsTest() {
        configStore = new ConfigStore();
        List<String> endpoints = new ArrayList<>();

        endpoints.add(TEST_ENDPOINT);
        endpoints.add(TEST_ENDPOINT_GEO);

        configStore.setEndpoints(endpoints);

        configStore.validateAndInit();

        clientBuilder = new AppConfigurationReplicaClientsBuilder(tokenProviderMock, null, false, "", 0);

        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        List<AppConfigurationReplicaClient> clients = spy.buildClients(configStore);

        assertEquals(2, clients.size());
    }

    @Test
    public void buildClientsFromMultipleConnectionStringsTest() {
        configStore = new ConfigStore();
        List<String> connectionStrings = new ArrayList<>();

        connectionStrings.add(TEST_CONN_STRING);
        connectionStrings.add(TEST_CONN_STRING_GEO);

        configStore.setConnectionStrings(connectionStrings);

        configStore.validateAndInit();

        clientBuilder = new AppConfigurationReplicaClientsBuilder(tokenProviderMock, null, false, "", 0);

        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        List<AppConfigurationReplicaClient> clients = spy.buildClients(configStore);

        assertEquals(2, clients.size());
    }

    @Test
    public void endpointAndConnectionString() {
        List<String> endpoints = new ArrayList<>();

        endpoints.add(TEST_ENDPOINT);
        endpoints.add(TEST_ENDPOINT_GEO);

        configStore.setEndpoints(endpoints);
        configStore.setConnectionString(TEST_CONN_STRING);
        configStore.validateAndInit();

        clientBuilder = new AppConfigurationReplicaClientsBuilder(tokenProviderMock, null, false, "", 0);

        String message = assertThrows(IllegalArgumentException.class,
            () -> clientBuilder.buildClients(configStore).get(0)).getMessage();

        assertEquals("More than 1 Conncetion method was set for connecting to App Configuration.", message);
    }

}
