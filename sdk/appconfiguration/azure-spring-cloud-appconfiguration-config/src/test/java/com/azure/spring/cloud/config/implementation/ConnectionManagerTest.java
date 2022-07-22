// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING_GEO;
import static com.azure.spring.cloud.config.TestConstants.TEST_ENDPOINT;
import static com.azure.spring.cloud.config.TestConstants.TEST_ENDPOINT_GEO;
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
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

public class ConnectionManagerTest {

    @Mock
    private ConfigurationClientBuilder builderMock;

    @Mock
    private AppConfigurationCredentialProvider tokenProviderMock;

    @Mock
    private TokenCredential credentialMock;

    @Mock
    private ConfigurationClientBuilderSetup modifierMock;

    private ConnectionManager connectionManager;

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

        connectionManager = null;
    }

    @Test
    public void getStoreIdentifierTest() {
        connectionManager = new ConnectionManager(configStore, providerProperties, null, null, false, false, null);

        assertEquals(TEST_ENDPOINT, connectionManager.getOriginEndpoint());

        configStore.setEndpoint(null);

        List<String> endpoints = new ArrayList<>();
        endpoints.add("first.endpoint");
        endpoints.add("second.endpoint");

        configStore.setEndpoints(endpoints);
        configStore.validateAndInit();

        connectionManager = new ConnectionManager(configStore, providerProperties, null, null, false, false, null);

        assertEquals("first.endpoint", connectionManager.getOriginEndpoint());
    }

    @Test
    public void buildClientFromEndpointTest() {
        connectionManager = new ConnectionManager(configStore, providerProperties, tokenProviderMock, null, false,
            false, null);
        ConnectionManager spy = Mockito.spy(connectionManager);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        ConfigurationClientWrapper clientWrapper = spy.getAvalibleClients().get(0);

        assertNotNull(clientWrapper);
        assertTrue(clientWrapper.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, clientWrapper.getEndpoint());
        assertEquals(0, clientWrapper.getFailedAttempts());
    }

    @Test
    public void buildClientFromEndpointWithTokenCredentialTest() {
        connectionManager = new ConnectionManager(configStore, providerProperties, tokenProviderMock, null, false,
            false, null);

        ConnectionManager spy = Mockito.spy(connectionManager);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);
        when(tokenProviderMock.getAppConfigCredential(Mockito.eq(TEST_ENDPOINT))).thenReturn(credentialMock);

        ConfigurationClientWrapper clientWrapper = spy.getAvalibleClients().get(0);

        assertNotNull(clientWrapper);
        assertTrue(clientWrapper.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, clientWrapper.getEndpoint());
        assertEquals(0, clientWrapper.getFailedAttempts());

        verify(tokenProviderMock, times(1)).getAppConfigCredential(Mockito.anyString());
        verify(builderMock, times(1)).credential(Mockito.eq(credentialMock));
    }

    @Test
    public void buildClientFromEndpointClientIdTest() {
        String clientId = "1234-5678-9012-3456";
        connectionManager = new ConnectionManager(configStore, providerProperties, null, null, false, false, clientId);

        ConnectionManager spy = Mockito.spy(connectionManager);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        ConfigurationClientWrapper clientWrapper = spy.getAvalibleClients().get(0);

        assertNotNull(clientWrapper);
        assertTrue(clientWrapper.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, clientWrapper.getEndpoint());
        assertEquals(0, clientWrapper.getFailedAttempts());

        verify(builderMock, times(1)).credential(Mockito.any(ManagedIdentityCredential.class));
    }

    @Test
    public void buildClientFromConnectionStringTest() {
        configStore.setEndpoint(null);
        configStore.setConnectionString(TEST_CONN_STRING);
        configStore.validateAndInit();

        connectionManager = new ConnectionManager(configStore, providerProperties, null, null, false, false, null);
        ConnectionManager spy = Mockito.spy(connectionManager);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq("test.endpoint"))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        ConfigurationClientWrapper clientWrapper = spy.getAvalibleClients().get(0);

        assertNotNull(clientWrapper);
        assertTrue(clientWrapper.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, clientWrapper.getEndpoint());
        assertEquals(0, clientWrapper.getFailedAttempts());
    }

    @Test
    public void modifyClientTest() {
        connectionManager = new ConnectionManager(configStore, providerProperties, tokenProviderMock, modifierMock,
            false, false, null);
        ConnectionManager spy = Mockito.spy(connectionManager);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        ConfigurationClientWrapper clientWrapper = spy.getAvalibleClients().get(0);

        assertNotNull(clientWrapper);
        assertTrue(clientWrapper.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, clientWrapper.getEndpoint());
        assertEquals(0, clientWrapper.getFailedAttempts());

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

        connectionManager = new ConnectionManager(configStore, providerProperties, tokenProviderMock, null, false,
            false, null);
        ConnectionManager spy = Mockito.spy(connectionManager);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        List<ConfigurationClientWrapper> clients = spy.getAvalibleClients();

        assertEquals(2, clients.size());

        ConfigurationClientWrapper clientWrapper = clients.get(0);

        assertNotNull(clientWrapper);
        assertTrue(clientWrapper.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, clientWrapper.getEndpoint());
        assertEquals(0, clientWrapper.getFailedAttempts());

        clientWrapper.updateBackoffEndTime(Instant.now().plusSeconds(100000));

        clients = spy.getAvalibleClients();

        assertEquals(1, clients.size());

        clientWrapper = clients.get(0);

        assertNotNull(clientWrapper);
        assertTrue(clientWrapper.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT_GEO, clientWrapper.getEndpoint());
        assertEquals(0, clientWrapper.getFailedAttempts());
    }

    @Test
    public void buildClientsFromMultipleConnectionStringsTest() {
        configStore = new ConfigStore();
        List<String> connectionStrings = new ArrayList<>();

        connectionStrings.add(TEST_CONN_STRING);
        connectionStrings.add(TEST_CONN_STRING_GEO);

        configStore.setConnectionStrings(connectionStrings);

        configStore.validateAndInit();

        connectionManager = new ConnectionManager(configStore, providerProperties, tokenProviderMock, null, false,
            false, null);
        ConnectionManager spy = Mockito.spy(connectionManager);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        List<ConfigurationClientWrapper> clients = spy.getAvalibleClients();

        assertEquals(2, clients.size());

        ConfigurationClientWrapper clientWrapper = clients.get(0);

        assertNotNull(clientWrapper);
        assertTrue(clientWrapper.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, clientWrapper.getEndpoint());
        assertEquals(0, clientWrapper.getFailedAttempts());

        clientWrapper.updateBackoffEndTime(Instant.now().plusSeconds(100000));

        clients = spy.getAvalibleClients();

        assertEquals(1, clients.size());

        clientWrapper = clients.get(0);

        assertNotNull(clientWrapper);
        assertTrue(clientWrapper.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT_GEO, clientWrapper.getEndpoint());
        assertEquals(0, clientWrapper.getFailedAttempts());
    }

    @Test
    public void endpointAndConnectionString() {
        List<String> endpoints = new ArrayList<>();

        endpoints.add(TEST_ENDPOINT);
        endpoints.add(TEST_ENDPOINT_GEO);

        configStore.setEndpoints(endpoints);
        configStore.setConnectionString(TEST_CONN_STRING);
        configStore.validateAndInit();

        connectionManager = new ConnectionManager(configStore, providerProperties, tokenProviderMock, null, false,
            false, null);

        String message = assertThrows(IllegalArgumentException.class,
            () -> connectionManager.getAvalibleClients().get(0)).getMessage();

        assertEquals("More than 1 Conncetion method was set for connecting to App Configuration.", message);
    }
}
