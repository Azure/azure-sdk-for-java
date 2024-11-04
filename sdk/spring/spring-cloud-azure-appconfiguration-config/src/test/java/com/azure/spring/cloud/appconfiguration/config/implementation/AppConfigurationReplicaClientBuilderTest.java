// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING_GEO;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_ENDPOINT;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_ENDPOINT_GEO;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.service.implementation.appconfiguration.ConfigurationClientBuilderFactory;

public class AppConfigurationReplicaClientBuilderTest {

    @Mock
    private ConfigurationClientBuilder builderMock;

    @Mock
    private TokenCredential credentialMock;

    @Mock
    private ConfigurationClientCustomizer modifierMock;

    AppConfigurationReplicaClientsBuilder clientBuilder;

    private ConfigStore configStore;

    @Mock
    private ConfigurationClientBuilderFactory clientFactoryMock;

    @Mock
    private Environment envMock;

    private MockitoSession session;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();

        configStore = new ConfigStore();
        configStore.setEndpoint(TEST_ENDPOINT);

        configStore.validateAndInit();

        clientBuilder = null;
        when(envMock.getActiveProfiles()).thenReturn(new String[0]);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void buildClientFromEndpointTest() {
        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setEnvironment(envMock);
        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);

        when(clientFactoryMock.build()).thenReturn(builderMock);
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
    public void buildClientFromConnectionStringTest() {
        configStore.setEndpoint(null);
        configStore.setConnectionString(TEST_CONN_STRING);
        configStore.validateAndInit();

        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setEnvironment(envMock);
        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);

        when(clientFactoryMock.build()).thenReturn(builderMock);
        when(builderMock.connectionString(Mockito.anyString())).thenReturn(builderMock);

        List<AppConfigurationReplicaClient> clients = spy.buildClients(configStore);

        assertNotNull(clients.get(0));
        assertTrue(clients.get(0).getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, clients.get(0).getEndpoint());
        assertEquals(0, clients.get(0).getFailedAttempts());
        assertEquals(1, clients.size());
    }

    @Test
    public void modifyClientTest() {
        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setClientProvider(modifierMock);
        clientBuilder.setEnvironment(envMock);

        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);
        when(clientFactoryMock.build()).thenReturn(builderMock);

        AppConfigurationReplicaClient replicaClient = spy.buildClients(configStore).get(0);

        assertNotNull(replicaClient);
        assertTrue(replicaClient.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, replicaClient.getEndpoint());
        assertEquals(0, replicaClient.getFailedAttempts());

        verify(modifierMock, times(1)).customize(Mockito.eq(builderMock), Mockito.eq(TEST_ENDPOINT));
    }

    @Test
    public void buildClientsFromMultipleEndpointsTest() {
        configStore = new ConfigStore();
        List<String> endpoints = new ArrayList<>();

        endpoints.add(TEST_ENDPOINT);
        endpoints.add(TEST_ENDPOINT_GEO);

        configStore.setEndpoints(endpoints);

        configStore.validateAndInit();

        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setEnvironment(envMock);

        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);
        when(clientFactoryMock.build()).thenReturn(builderMock);

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

        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setEnvironment(envMock);

        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);
        
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);
        when(clientFactoryMock.build()).thenReturn(builderMock);
        when(builderMock.connectionString(Mockito.anyString())).thenReturn(builderMock);

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

        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setEnvironment(envMock);

        String message = assertThrows(IllegalArgumentException.class,
            () -> clientBuilder.buildClients(configStore).get(0)).getMessage();

        assertEquals("More than 1 connection method was set for connecting to App Configuration.", message);
    }

    @Test
    public void buildClientTest() {
        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setEnvironment(envMock);
        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);

        when(clientFactoryMock.build()).thenReturn(builderMock);
        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        AppConfigurationReplicaClient replicaClient = spy.buildClient("https://fake.test.config.io", configStore);

        assertNotNull(replicaClient);
        assertTrue(replicaClient.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, replicaClient.getEndpoint());
        assertEquals(0, replicaClient.getFailedAttempts());
    }

    @Test
    public void buildClientConnectionStringTest() {
        configStore.setConnectionString(TEST_CONN_STRING);
        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setEnvironment(envMock);
        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);

        when(clientFactoryMock.build()).thenReturn(builderMock);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);
        when(builderMock.connectionString(Mockito.anyString())).thenReturn(builderMock);

        AppConfigurationReplicaClient replicaClient = spy.buildClient("https://fake.test.config.io", configStore);

        assertNotNull(replicaClient);
        assertTrue(replicaClient.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, replicaClient.getEndpoint());
        assertEquals(0, replicaClient.getFailedAttempts());
    }

    @Test
    public void buildClientConnectionStringsTest() {
        configStore.setConnectionStrings(List.of(TEST_CONN_STRING, TEST_CONN_STRING_GEO));
        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setEnvironment(envMock);
        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);

        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);
        when(builderMock.connectionString(Mockito.anyString())).thenReturn(builderMock);
        when(clientFactoryMock.build()).thenReturn(builderMock);

        AppConfigurationReplicaClient replicaClient = spy.buildClient("https://fake.test.config.io", configStore);

        assertNotNull(replicaClient);
        assertTrue(replicaClient.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals(TEST_ENDPOINT, replicaClient.getEndpoint());
        assertEquals(0, replicaClient.getFailedAttempts());
    }

    @Test
    public void buildClientConnectionStringInvalidTest() {
        configStore.setConnectionString(TEST_CONN_STRING);
        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setEnvironment(envMock);
        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> spy.buildClient("fake.test.config.io", configStore));

        assertEquals("java.net.MalformedURLException: no protocol: fake.test.config.io", exception.getMessage());
    }

    @Test
    public void buildClientConnectionStringInvalid2Test() {
        configStore.setConnectionString("Not A Connection String");
        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setEnvironment(envMock);
        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> spy.buildClient("fake.test.config.io", configStore));

        assertEquals("invalid connection string segment count", exception.getMessage());
    }

    @Test
    public void buildClientConnectionStringInvalid3Test() {
        configStore.setConnectionString("Not;A;Connection String");
        clientBuilder = new AppConfigurationReplicaClientsBuilder(0, clientFactoryMock, false);
        clientBuilder.setEnvironment(envMock);
        AppConfigurationReplicaClientsBuilder spy = Mockito.spy(clientBuilder);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> spy.buildClient("fake.test.config.io", configStore));

        assertEquals(
            "Could not parse 'connectionString'. Expected format: 'endpoint={endpoint};id={id};secret={secret}'. Actual:Not;A;Connection String",
            exception.getMessage());
    }

}
