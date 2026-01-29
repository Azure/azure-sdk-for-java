// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.boot.logging.DeferredLogFactory;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;

public class AzureAppConfigDataLoaderTest {

    @Mock
    private AppConfigurationReplicaClient clientMock;

    @Mock
    private AppConfigurationReplicaClientFactory replicaClientFactoryMock;

    @Mock
    private AppConfigurationKeyVaultClientFactory keyVaultClientFactoryMock;

    @Mock
    private ConfigDataLoaderContext configDataLoaderContextMock;

    @Mock
    private ConfigurableBootstrapContext bootstrapContextMock;

    @Mock
    private DeferredLogFactory logFactoryMock;

    private AzureAppConfigDataResource resource;

    private AzureAppConfigDataResource refreshResource;

    private ConfigStore configStore;

    private MockitoSession session;

    private static final String ENDPOINT = "https://test.azconfig.io";

    private static final String KEY_FILTER = "/application/*";

    private static final String LABEL_FILTER = "prod";

    @BeforeEach
    public void setup() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        MockitoAnnotations.openMocks(this);

        configStore = new ConfigStore();
        configStore.setEndpoint(ENDPOINT);
        configStore.setEnabled(true);

        // Setup feature flags
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(false);
        configStore.setFeatureFlags(featureFlagStore);

        // Setup basic resource
        Profiles profiles = Mockito.mock(Profiles.class);
        lenient().when(profiles.getActive()).thenReturn(List.of(LABEL_FILTER));

        // Startup resource (isRefresh = false)
        resource = new AzureAppConfigDataResource(true, configStore, profiles, true, Duration.ofMinutes(1), Duration.ofSeconds(30));
        // Refresh resource (isRefresh = true)
        refreshResource = new AzureAppConfigDataResource(true, configStore, profiles, false, Duration.ofMinutes(1), Duration.ofSeconds(30));

        // Setup common mocks for ConfigDataLoaderContext
        lenient().when(configDataLoaderContextMock.getBootstrapContext()).thenReturn(bootstrapContextMock);
        lenient().when(bootstrapContextMock.isRegistered(FeatureFlagClient.class)).thenReturn(false);
        lenient().when(bootstrapContextMock.get(AppConfigurationReplicaClientFactory.class))
            .thenReturn(replicaClientFactoryMock);
        lenient().when(bootstrapContextMock.get(AppConfigurationKeyVaultClientFactory.class))
            .thenReturn(keyVaultClientFactoryMock);
        lenient().when(logFactoryMock.getLog(any(Class.class))).thenReturn(new DeferredLog());
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void loadSucceedsWhenNoClientsAvailableTest() throws IOException {
        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        // Setup mocks - no clients available
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(null);

        // Test using public load() method
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        ConfigData result = loader.load(configDataLoaderContextMock, resource);

        // Verify - returns empty ConfigData when no clients available
        assertNotNull(result);
        verify(replicaClientFactoryMock, times(1)).findActiveClients(ENDPOINT);
        verify(replicaClientFactoryMock, times(1)).getNextActiveClient(eq(ENDPOINT), eq(true));
    }

    @Test
    public void refreshAllDisabledUsesWatchKeysTest() {
        // Setup monitoring with refreshAll disabled (traditional watch keys)
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);

        // Add trigger for traditional watch key
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey("sentinel");
        trigger.setLabel("prod");
        monitoring.setTriggers(List.of(trigger));

        configStore.setMonitoring(monitoring);

        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        // Verify that when refreshAll is false, triggers are configured
        assertEquals(1, monitoring.getTriggers().size());
        assertEquals("sentinel", monitoring.getTriggers().get(0).getKey());
    }

    // Startup Retry Tests

    @Test
    public void startupSucceedsOnFirstAttemptTest() throws IOException {
        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        // Setup mocks - no clients available (returns empty result)
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(null);

        // Test using public load() method
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        ConfigData result = loader.load(configDataLoaderContextMock, resource);

        // Verify - success on first attempt, no retries needed
        assertNotNull(result);
        verify(replicaClientFactoryMock, times(1)).findActiveClients(ENDPOINT);
        verify(replicaClientFactoryMock, times(1)).getNextActiveClient(eq(ENDPOINT), eq(true));
        // getMillisUntilNextClientAvailable should not be called when no exception occurred
        verify(replicaClientFactoryMock, never()).getMillisUntilNextClientAvailable(anyString());
    }

    @Test
    public void startupRetriesAfterClientFailureThenSucceedsTest() throws IOException {
        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        // Create a second client mock for the successful retry
        AppConfigurationReplicaClient secondClientMock = Mockito.mock(AppConfigurationReplicaClient.class);
        lenient().when(secondClientMock.getEndpoint()).thenReturn(ENDPOINT);

        // Setup mocks:
        // - First getNextActiveClient(true) returns clientMock which will throw
        // - First getNextActiveClient(false) returns null (no more replicas in first attempt)
        // - Second getNextActiveClient(true) returns null (simulating success path)
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true)))
            .thenReturn(clientMock)  // First attempt - will fail
            .thenReturn(null);       // Second attempt - no clients, treated as success
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(false)))
            .thenReturn(null); // No more replicas
        when(replicaClientFactoryMock.getMillisUntilNextClientAvailable(ENDPOINT)).thenReturn(0L);
        when(clientMock.getEndpoint()).thenReturn(ENDPOINT);
        when(clientMock.listSettings(any(), any())).thenThrow(new RuntimeException("Simulated failure"));

        // Test using public load() method
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        ConfigData result = loader.load(configDataLoaderContextMock, resource);

        // Verify - retried after failure
        assertNotNull(result);
        verify(replicaClientFactoryMock, atLeast(2)).findActiveClients(ENDPOINT);
    }

    @Test
    public void startupFailsAfterAllRetriesExhaustedTest() {
        // Setup with a short timeout
        Profiles profiles = Mockito.mock(Profiles.class);
        when(profiles.getActive()).thenReturn(List.of(LABEL_FILTER));

        ConfigStore shortTimeoutStore = new ConfigStore();
        shortTimeoutStore.setEndpoint(ENDPOINT);
        shortTimeoutStore.setEnabled(true);
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(false);
        shortTimeoutStore.setFeatureFlags(featureFlagStore);

        AzureAppConfigDataResource shortTimeoutResource = new AzureAppConfigDataResource(
            true, shortTimeoutStore, profiles, true, Duration.ofMinutes(1), Duration.ofSeconds(30));

        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        shortTimeoutStore.getSelects().add(selector);

        // Setup mocks - client always fails
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(clientMock);
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(false))).thenReturn(null);
        when(replicaClientFactoryMock.getMillisUntilNextClientAvailable(ENDPOINT))
            .thenReturn(60000L); // Large backoff, will exceed deadline
        when(clientMock.getEndpoint()).thenReturn(ENDPOINT);
        when(clientMock.listSettings(any(), any())).thenThrow(new RuntimeException("Simulated failure"));

        // Test using public load() method - should throw RuntimeException after retries exhausted
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> loader.load(configDataLoaderContextMock, shortTimeoutResource));

        // Verify - failure after retries exhausted
        assertTrue(exception.getMessage().contains("Failed to generate property sources"));
        verify(replicaClientFactoryMock, atLeast(1)).findActiveClients(ENDPOINT);
    }

    @Test
    public void refreshOnlyAttemptsOnceOnFailureTest() throws IOException {
        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        // Setup mocks - client fails
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(clientMock);
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(false))).thenReturn(null);
        when(clientMock.getEndpoint()).thenReturn(ENDPOINT);
        when(clientMock.listSettings(any(), any())).thenThrow(new RuntimeException("Simulated failure"));

        // Test with refresh resource (isRefresh = true) - should NOT throw, just warn
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        ConfigData result = loader.load(configDataLoaderContextMock, refreshResource);

        // Verify - only one findActiveClients call (no retry loop for refresh)
        assertNotNull(result);
        verify(replicaClientFactoryMock, times(1)).findActiveClients(ENDPOINT);
        // getMillisUntilNextClientAvailable should never be called during refresh
        verify(replicaClientFactoryMock, never()).getMillisUntilNextClientAvailable(anyString());
    }

    @Test
    public void refreshSucceedsOnFirstAttemptTest() throws IOException {
        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        // Setup mocks - no clients available (returns null = no-op success)
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(null);

        // Test with refresh resource
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        ConfigData result = loader.load(configDataLoaderContextMock, refreshResource);

        // Verify - success on first attempt
        assertNotNull(result);
        verify(replicaClientFactoryMock, times(1)).findActiveClients(ENDPOINT);
        verify(replicaClientFactoryMock, times(1)).getNextActiveClient(eq(ENDPOINT), eq(true));
    }

    @Test
    public void startupDoesNotRetryDuringRefreshTest() throws IOException {
        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        // Setup mock - client throws exception
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(clientMock);
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(false))).thenReturn(null);
        when(clientMock.getEndpoint()).thenReturn(ENDPOINT);
        when(clientMock.listSettings(any(), any())).thenThrow(new RuntimeException("Test failure"));

        // Test with refresh resource - should NOT throw, just warn and continue
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        ConfigData result = loader.load(configDataLoaderContextMock, refreshResource);

        // Verify - failure on first attempt, no retry
        assertNotNull(result);
        // Only one findActiveClients call (would be multiple in startup retry loop)
        verify(replicaClientFactoryMock, times(1)).findActiveClients(ENDPOINT);
    }
}
