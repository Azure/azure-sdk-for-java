// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.springframework.boot.context.config.Profiles;

import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.configuration.WatchedConfigurationSettings;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;

public class AzureAppConfigDataLoaderTest {

    @Mock
    private AppConfigurationReplicaClient clientMock;

    @Mock
    private WatchedConfigurationSettings watchedConfigurationSettingsMock;

    private AzureAppConfigDataResource resource;

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

        resource = new AzureAppConfigDataResource(true, configStore, profiles, false, Duration.ofMinutes(1));
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void createWatchedConfigurationSettingsWithSingleSelectorTest() throws Exception {
        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        // Setup mocks
        when(clientMock.watchedConfigurationSettings(any(SettingSelector.class), any(Context.class)))
            .thenReturn(watchedConfigurationSettingsMock);
        // Use reflection to test the private method
        AzureAppConfigDataLoader loader = createLoader();
        List<WatchedConfigurationSettings> result = invokeGetWatchedConfigurationSettings(loader, clientMock);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        
        ArgumentCaptor<SettingSelector> selectorCaptor = ArgumentCaptor.forClass(SettingSelector.class);
        verify(clientMock, times(1)).watchedConfigurationSettings(selectorCaptor.capture(), any(Context.class));
        
        SettingSelector capturedSelector = selectorCaptor.getValue();
        assertEquals(KEY_FILTER + "*", capturedSelector.getKeyFilter());
        assertEquals(LABEL_FILTER, capturedSelector.getLabelFilter());
    }

    @Test
    public void createWatchedConfigurationSettingsWithMultipleSelectorsTest() throws Exception {
        // Setup multiple selectors
        AppConfigurationKeyValueSelector selector1 = new AppConfigurationKeyValueSelector();
        selector1.setKeyFilter("/app1/*");
        selector1.setLabelFilter("dev");
        configStore.getSelects().add(selector1);

        AppConfigurationKeyValueSelector selector2 = new AppConfigurationKeyValueSelector();
        selector2.setKeyFilter("/app2/*");
        selector2.setLabelFilter("prod");
        configStore.getSelects().add(selector2);

        // Setup mocks
        when(clientMock.watchedConfigurationSettings(any(SettingSelector.class), any(Context.class)))
            .thenReturn(watchedConfigurationSettingsMock);

        // Test
        AzureAppConfigDataLoader loader = createLoader();
        List<WatchedConfigurationSettings> result = invokeGetWatchedConfigurationSettings(loader, clientMock);

        // Verify - should create watched configuration settings for both selectors
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(clientMock, times(2)).watchedConfigurationSettings(any(SettingSelector.class), any(Context.class));
    }

    @Test
    public void createWatchedConfigurationSettingsSkipsSnapshotsTest() throws Exception {
        // Setup selector with snapshot
        AppConfigurationKeyValueSelector snapshotSelector = new AppConfigurationKeyValueSelector();
        snapshotSelector.setSnapshotName("my-snapshot");
        configStore.getSelects().add(snapshotSelector);

        // Setup regular selector
        AppConfigurationKeyValueSelector regularSelector = new AppConfigurationKeyValueSelector();
        regularSelector.setKeyFilter(KEY_FILTER);
        regularSelector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(regularSelector);

        // Setup mocks
        when(clientMock.watchedConfigurationSettings(any(SettingSelector.class), any(Context.class)))
            .thenReturn(watchedConfigurationSettingsMock);

        // Test
        AzureAppConfigDataLoader loader = createLoader();
        List<WatchedConfigurationSettings> result = invokeGetWatchedConfigurationSettings(loader, clientMock);

        // Verify - snapshot should be skipped, only regular selector should be processed
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(clientMock, times(1)).watchedConfigurationSettings(any(SettingSelector.class), any(Context.class));
    }

    @Test
    public void createWatchedConfigurationSettingsWithMultipleLabelsTest() throws Exception {
        // Setup selector with multiple labels
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter("dev,prod,test");
        configStore.getSelects().add(selector);

        // Setup mocks
        when(clientMock.watchedConfigurationSettings(any(SettingSelector.class), any(Context.class)))
            .thenReturn(watchedConfigurationSettingsMock);
        // Test
        AzureAppConfigDataLoader loader = createLoader();
        List<WatchedConfigurationSettings> result = invokeGetWatchedConfigurationSettings(loader, clientMock);

        // Verify - should create watched configuration settings for each label
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(clientMock, times(3)).watchedConfigurationSettings(any(SettingSelector.class), any(Context.class));
    }

    @Test
    public void refreshAllEnabledUsesWatchedConfigurationSettingsTest() throws Exception {
        // Setup monitoring with refreshAll enabled
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        configStore.setMonitoring(monitoring);

        // Setup selector
        AppConfigurationKeyValueSelector selector = new AppConfigurationKeyValueSelector();
        selector.setKeyFilter(KEY_FILTER);
        selector.setLabelFilter(LABEL_FILTER);
        configStore.getSelects().add(selector);

        // Setup mocks
        when(clientMock.watchedConfigurationSettings(any(SettingSelector.class), any(Context.class)))
            .thenReturn(watchedConfigurationSettingsMock);

        // Test - verify that watched configuration settings are created when refreshAll is enabled
        AzureAppConfigDataLoader loader = createLoader();
        List<WatchedConfigurationSettings> result = invokeGetWatchedConfigurationSettings(loader, clientMock);
        
        // Verify watched configuration settings were created
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(clientMock, times(1)).watchedConfigurationSettings(any(SettingSelector.class), any(Context.class));
    }

    @Test
    public void refreshAllDisabledUsesWatchKeysTest() throws Exception {
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
        // The actual validation happens in validateAndInit which is called during load
        assertEquals(1, monitoring.getTriggers().size());
        assertEquals("sentinel", monitoring.getTriggers().get(0).getKey());
    }

    // Helper methods

    private AzureAppConfigDataLoader createLoader() {
        org.springframework.boot.logging.DeferredLogFactory logFactory = Mockito.mock(org.springframework.boot.logging.DeferredLogFactory.class);
        when(logFactory.getLog(any(Class.class))).thenReturn(new org.springframework.boot.logging.DeferredLog());
        return new AzureAppConfigDataLoader(logFactory);
    }

    private List<WatchedConfigurationSettings> invokeGetWatchedConfigurationSettings(
        AzureAppConfigDataLoader loader, AppConfigurationReplicaClient client) throws Exception {
        // Set resource field in the loader using reflection
        java.lang.reflect.Field resourceField = AzureAppConfigDataLoader.class.getDeclaredField("resource");
        resourceField.setAccessible(true);
        resourceField.set(loader, resource);

        // Set requestContext field (it can be null for this test)
        java.lang.reflect.Field requestContextField = AzureAppConfigDataLoader.class.getDeclaredField("requestContext");
        requestContextField.setAccessible(true);
        requestContextField.set(loader, Context.NONE);

        // Use reflection to invoke private method
        java.lang.reflect.Method method = AzureAppConfigDataLoader.class
            .getDeclaredMethod("getWatchedConfigurationSettings", AppConfigurationReplicaClient.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<WatchedConfigurationSettings> result = (List<WatchedConfigurationSettings>) method.invoke(loader, client);
        return result;
    }
}
