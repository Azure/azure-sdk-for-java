// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;
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

    @Mock
    private StateHolder stateHolderMock;

    private AzureAppConfigDataResource resource;

    private AzureAppConfigDataResource refreshResource;

    private ConfigStore configStore;

    private MockitoSession session;

    private static final String ENDPOINT = "https://test.azconfig.io";

    private static final String KEY_FILTER = "/application/";

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
        lenient().when(bootstrapContextMock.get(StateHolder.class)).thenReturn(stateHolderMock);
        lenient().when(logFactoryMock.getLog(any(Class.class))).thenReturn(new DeferredLog());
        lenient().when(clientMock.getTracingInfo())
            .thenReturn(new TracingInfo(false, 0, Configuration.getGlobalConfiguration()));
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void startupFailsWhenNoClientsAvailableTest() {
        Profiles profiles = Mockito.mock(Profiles.class);
        AzureAppConfigDataResource shortTimeoutResource = new AzureAppConfigDataResource(
            true, configStore, profiles, true, Duration.ofMinutes(1), Duration.ofMillis(100));
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(null);
        when(replicaClientFactoryMock.getMillisUntilNextClientAvailable(ENDPOINT)).thenReturn(30_000L);

        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        RuntimeException exception = assertTimeout(Duration.ofSeconds(7),
            () -> assertThrows(RuntimeException.class,
                () -> loader.load(configDataLoaderContextMock, shortTimeoutResource)));
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertTrue(exception.getCause().getMessage().contains("No Azure App Configuration clients available"));
        assertTrue(exception.getCause().getMessage().contains(ENDPOINT));
        verify(stateHolderMock, never()).setLoadState(ENDPOINT, true);
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

        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(clientMock);
        when(clientMock.listSettings(any(), any())).thenReturn(List.of(
            new ConfigurationSetting().setKey("/application/setting").setValue("hello")));

        // Test using public load() method
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        ConfigData result = loader.load(configDataLoaderContextMock, resource);

        // Verify - success on first attempt, no retries needed
        assertNotNull(result);
        assertEquals(1, result.getPropertySources().size());
        assertEquals("hello", result.getPropertySources().get(0).getProperty("setting"));
        verify(stateHolderMock).setLoadState(ENDPOINT, true);
        verify(replicaClientFactoryMock, times(1)).findActiveClients(ENDPOINT);
        verify(replicaClientFactoryMock, times(1)).getNextActiveClient(eq(ENDPOINT), eq(true));
    }

    @Test
    public void startupSucceedsWhenStoreHasNoSettingsTest() throws IOException {
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(clientMock);
        when(clientMock.listSettings(any(), any())).thenReturn(List.of());

        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        ConfigData result = loader.load(configDataLoaderContextMock, resource);

        assertEquals(1, result.getPropertySources().size());
        assertNull(result.getPropertySources().get(0).getProperty("setting"));
        verify(stateHolderMock).setLoadState(ENDPOINT, true);
        verify(replicaClientFactoryMock, never()).getMillisUntilNextClientAvailable(ENDPOINT);
    }

    @Test
    public void startupRetriesAfterClientFailureThenSucceedsTest() throws IOException {
        Profiles profiles = Mockito.mock(Profiles.class);
        when(profiles.getActive()).thenReturn(List.of(LABEL_FILTER));
        
        ConfigStore fastTimeoutStore = new ConfigStore();
        fastTimeoutStore.setEndpoint(ENDPOINT);
        fastTimeoutStore.setEnabled(true);
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(false);
        fastTimeoutStore.setFeatureFlags(featureFlagStore);
        
        AzureAppConfigDataResource fastResource = new AzureAppConfigDataResource(
            true, fastTimeoutStore, profiles, true, Duration.ofMinutes(1), Duration.ofSeconds(2));
        
        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        fastTimeoutStore.getSelects().add(selector);

        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true)))
            .thenReturn(clientMock, null, clientMock);
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(false)))
            .thenReturn(null);
        when(replicaClientFactoryMock.getMillisUntilNextClientAvailable(ENDPOINT)).thenReturn(5L);
        when(clientMock.getEndpoint()).thenReturn(ENDPOINT);
        when(clientMock.listSettings(any(), any()))
            .thenThrow(new RuntimeException("Simulated failure"))
            .thenReturn(List.of(new ConfigurationSetting().setKey("/application/setting").setValue("hello")));

        // Test using public load() method
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        ConfigData result = loader.load(configDataLoaderContextMock, fastResource);

        // Verify - retried after failure
        assertNotNull(result);
        assertEquals(1, result.getPropertySources().size());
        assertEquals("hello", result.getPropertySources().get(0).getProperty("setting"));
        verify(replicaClientFactoryMock, times(3)).findActiveClients(ENDPOINT);
        verify(replicaClientFactoryMock, times(2)).getMillisUntilNextClientAvailable(ENDPOINT);
        verify(clientMock, times(2)).listSettings(any(), any());
        verify(stateHolderMock, times(1)).setLoadState(ENDPOINT, true);
    }

    @Test
    public void startupFailsAfterAllRetriesExhaustedTest() {
        // Setup with a short timeout
        Profiles profiles = Mockito.mock(Profiles.class);
        lenient().when(profiles.getActive()).thenReturn(List.of(LABEL_FILTER));

        ConfigStore shortTimeoutStore = new ConfigStore();
        shortTimeoutStore.setEndpoint(ENDPOINT);
        shortTimeoutStore.setEnabled(true);
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(false);
        shortTimeoutStore.setFeatureFlags(featureFlagStore);

        AzureAppConfigDataResource shortTimeoutResource = new AzureAppConfigDataResource(
            true, shortTimeoutStore, profiles, true, Duration.ofMinutes(1), Duration.ofSeconds(1));

        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        shortTimeoutStore.getSelects().add(selector);

        RuntimeException loadFailure = new RuntimeException("Simulated failure");
        lenient().when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(clientMock);
        lenient().when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(false))).thenReturn(null);
        when(replicaClientFactoryMock.getMillisUntilNextClientAvailable(ENDPOINT)).thenReturn(10L);
        lenient().when(clientMock.getEndpoint()).thenReturn(ENDPOINT);
        lenient().when(clientMock.listSettings(any(), any())).thenThrow(loadFailure);

        // Test using public load() method - should throw RuntimeException after retries exhausted
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> loader.load(configDataLoaderContextMock, shortTimeoutResource));

        // Verify - failure after retries exhausted
        assertTrue(exception.getMessage().contains("Failed to generate property sources"));
        assertSame(loadFailure, exception.getCause());
        verify(replicaClientFactoryMock, atLeast(2)).findActiveClients(ENDPOINT);
        verify(stateHolderMock, never()).setLoadState(ENDPOINT, true);
    }

    @Test
    public void startupPreservesMonitoringFailureWhileClientsAreBackedOffTest() {
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey("sentinel");
        trigger.setLabel(LABEL_FILTER);
        monitoring.setTriggers(List.of(trigger));
        configStore.setMonitoring(monitoring);

        Profiles profiles = Mockito.mock(Profiles.class);
        when(profiles.getActive()).thenReturn(List.of(LABEL_FILTER));
        AzureAppConfigDataResource shortTimeoutResource = new AzureAppConfigDataResource(
            true, configStore, profiles, true, Duration.ofMinutes(1), Duration.ofSeconds(1));

        RuntimeException loadFailure = new RuntimeException("Missing sentinel");
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true)))
            .thenReturn(clientMock).thenReturn(null);
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(false))).thenReturn(null);
        when(replicaClientFactoryMock.getMillisUntilNextClientAvailable(ENDPOINT)).thenReturn(10L);
        when(clientMock.getEndpoint()).thenReturn(ENDPOINT);
        when(clientMock.listSettings(any(), any())).thenReturn(List.of(
            new ConfigurationSetting().setKey("/application/setting").setValue("hello")));
        when(clientMock.getWatchKey(eq("sentinel"), eq(LABEL_FILTER), any())).thenThrow(loadFailure);

        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> loader.load(configDataLoaderContextMock, shortTimeoutResource));

        assertSame(loadFailure, exception.getCause());
        verify(replicaClientFactoryMock, atLeast(2)).findActiveClients(ENDPOINT);
        verify(replicaClientFactoryMock).backoffClient(ENDPOINT, ENDPOINT);
        verify(clientMock, times(1)).listSettings(any(), any());
        verify(stateHolderMock, never()).setLoadState(ENDPOINT, true);
    }

    @Test
    public void startupPreservesFailureWhenRetryWaitIsInterruptedTest() {
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);
        RuntimeException loadFailure = new RuntimeException("Simulated failure");
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(clientMock);
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(false))).thenReturn(null);
        when(replicaClientFactoryMock.getMillisUntilNextClientAvailable(ENDPOINT)).thenReturn(30_000L);
        when(clientMock.getEndpoint()).thenReturn(ENDPOINT);
        when(clientMock.listSettings(any(), any())).thenThrow(loadFailure);
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);

        Thread.currentThread().interrupt();
        try {
            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> loader.load(configDataLoaderContextMock, resource));

            assertSame(loadFailure, exception.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
            verify(replicaClientFactoryMock, times(1)).findActiveClients(ENDPOINT);
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    public void startupFailsWhenTimeoutExpiresBeforeFirstAttemptTest() {
        Profiles profiles = Mockito.mock(Profiles.class);
        AzureAppConfigDataResource expiredResource = new AzureAppConfigDataResource(
            true, configStore, profiles, true, Duration.ofMinutes(1), Duration.ZERO);
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> loader.load(configDataLoaderContextMock, expiredResource));

        assertTrue(exception.getCause().getMessage().contains("startup timeout expired"));
        verify(replicaClientFactoryMock, never()).findActiveClients(ENDPOINT);
    }

    @Test
    public void refreshOnlyAttemptsOnceAndThrowsOnFailureTest() {
        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        // Setup mocks - client fails
        lenient().when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(clientMock);
        lenient().when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(false))).thenReturn(null);
        lenient().when(clientMock.getEndpoint()).thenReturn(ENDPOINT);
        lenient().when(clientMock.listSettings(any(), any())).thenThrow(new RuntimeException("Simulated failure"));

        // Test with refresh resource (isRefresh = true)
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> loader.load(configDataLoaderContextMock, refreshResource));

        // Verify - refresh fails and does not enter the startup retry/backoff loop
        assertTrue(exception.getMessage().contains("Failed to refresh property sources"));
        verify(replicaClientFactoryMock, times(1)).findActiveClients(ENDPOINT);
    }

    @Test
    public void refreshSucceedsOnFirstAttemptTest() throws IOException {
        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(clientMock);
        when(clientMock.listSettings(any(), any())).thenReturn(List.of(
            new ConfigurationSetting().setKey("/application/setting").setValue("hello")));

        // Test with refresh resource
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        ConfigData result = loader.load(configDataLoaderContextMock, refreshResource);

        // Verify - success on first attempt
        assertNotNull(result);
        assertEquals(1, result.getPropertySources().size());
        assertEquals("hello", result.getPropertySources().get(0).getProperty("setting"));
        verify(replicaClientFactoryMock, times(1)).findActiveClients(ENDPOINT);
        verify(replicaClientFactoryMock, times(1)).getNextActiveClient(eq(ENDPOINT), eq(true));
    }

    @Test
    public void refreshFailsWhenNoClientsAvailableTest() {
        when(replicaClientFactoryMock.getNextActiveClient(eq(ENDPOINT), eq(true))).thenReturn(null);
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> loader.load(configDataLoaderContextMock, refreshResource));

        assertTrue(exception.getMessage().contains("Failed to refresh property sources"));
        assertTrue(exception.getCause().getMessage().contains("No Azure App Configuration clients available"));
        verify(replicaClientFactoryMock, times(1)).findActiveClients(ENDPOINT);
        verify(replicaClientFactoryMock, never()).getMillisUntilNextClientAvailable(ENDPOINT);
        verify(stateHolderMock, never()).setLoadState(ENDPOINT, true);
    }

    @Test
    public void refreshFailureDoesNotUseStartupRetryTest() {
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

        // Test with refresh resource
        AzureAppConfigDataLoader loader = new AzureAppConfigDataLoader(logFactoryMock);
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> loader.load(configDataLoaderContextMock, refreshResource));

        // Verify - refresh fails on the first attempt without entering the startup retry loop
        assertTrue(exception.getMessage().contains("Failed to refresh property sources"));
        verify(replicaClientFactoryMock, times(1)).findActiveClients(ENDPOINT);
    }
}
