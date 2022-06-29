package com.azure.spring.cloud.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.resource.ConfigurationClientWrapper;

public class ConnectionManagerTest {

    @Mock
    private ConfigurationClientBuilder builderMock;

    @Mock
    private AppConfigurationCredentialProvider tokenProviderMock;
    
    @Mock
    private TokenCredential credentialMock;

    private ConnectionManager connectionManager;

    private ConfigStore configStore;

    private AppConfigurationProviderProperties providerProperties;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        configStore = new ConfigStore();
        configStore.setEndpoint("test.endpoint");

        configStore.validateAndInit();

        providerProperties = new AppConfigurationProviderProperties();

        connectionManager = new ConnectionManager(configStore, providerProperties, null, null, false, false, null);
    }

    @Test
    public void getStoreIdentifierTest() {
        assertEquals("test.endpoint", connectionManager.getStoreIdentifier());

        configStore.setEndpoint(null);

        List<String> endpoints = new ArrayList<>();
        endpoints.add("first.endpoint");
        endpoints.add("second.endpoint");

        configStore.setEndpoints(endpoints);
        configStore.validateAndInit();

        connectionManager = new ConnectionManager(configStore, providerProperties, null, null, false, false, null);

        assertEquals("first.endpoint", connectionManager.getStoreIdentifier());
    }

    @Test
    public void buildClientFromEndpointTest() {
        ConnectionManager spy = Mockito.spy(connectionManager);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq("test.endpoint"))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);

        ConfigurationClientWrapper clientWrapper = spy.getClient();

        assertNotNull(clientWrapper);
        assertTrue(clientWrapper.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals("test.endpoint", clientWrapper.getEndpoint());
        assertEquals(0, clientWrapper.getFailedAttempts());
    }

    @Test
    public void buildClientFromEndpointWithTokenCredentialTest() {
        connectionManager = new ConnectionManager(configStore, providerProperties, tokenProviderMock, null, false,
            false, null);

        ConnectionManager spy = Mockito.spy(connectionManager);
        Mockito.doReturn(builderMock).when(spy).getBuilder();

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
        when(builderMock.endpoint(Mockito.eq("test.endpoint"))).thenReturn(builder);
        when(builderMock.addPolicy(Mockito.any())).thenReturn(builderMock);
        when(tokenProviderMock.getAppConfigCredential(Mockito.eq("test.endpoint"))).thenReturn(credentialMock);

        ConfigurationClientWrapper clientWrapper = spy.getClient();

        assertNotNull(clientWrapper);
        assertTrue(clientWrapper.getBackoffEndTime().isBefore(Instant.now().plusSeconds(1)));
        assertEquals("test.endpoint", clientWrapper.getEndpoint());
        assertEquals(0, clientWrapper.getFailedAttempts());
        
        verify(tokenProviderMock, times(1)).getAppConfigCredential(Mockito.anyString());
        verify(builderMock, times(1)).credential(Mockito.eq(credentialMock));
    }
}
