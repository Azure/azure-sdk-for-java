// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static com.azure.spring.cloud.config.AppConfigurationConstants.EMPTY_LABEL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.implementation.AppConfigurationRefreshUtil.RefreshEventData;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;

public class AppConfigurationRefreshUtilTest {

    private static final String KEY_FILTER = "/application/*";

    @Mock
    private ConfigurationClientWrapper clientMock;

    @Mock
    private ClientFactory clientFactoryMock;

    @Mock
    private PagedIterable<ConfigurationSetting> flagsPagedIterableMock;

    @Mock
    private ConfigurationClientWrapper clientOriginMock;

    @Mock
    private StateHolder currentStateMock;

    private ConfigStore configStore;

    private AppConfigurationProviderProperties appProperties;

    private String endpoint;

    private RefreshEventData eventData = new RefreshEventData();

    private List<ConfigurationClientWrapper> clients = new ArrayList<>();

    private List<ConfigStore> stores = new ArrayList<>();

    private List<ConfigurationSetting> watchKeys = generateWatchKeys();

    private List<ConfigurationSetting> watchKeysFeatureFlags = generateFeatureFlagWatchKeys();

    private AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();

    private FeatureFlagStore featureStore = new FeatureFlagStore();

    @BeforeEach
    public void setup(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        StateHolder state = new StateHolder();
        state.setNextForcedRefresh(Duration.ofMinutes(10));
        StateHolder.updateState(state);

        configStore = new ConfigStore();

        FeatureFlagStore ffStore = new FeatureFlagStore();
        ffStore.setEnabled(true);
        configStore.setFeatureFlags(ffStore);

        appProperties = new AppConfigurationProviderProperties();
        appProperties.setDefaultMaxBackoff((long) 1);
        appProperties.setDefaultMinBackoff((long) 0);

        monitoring.setEnabled(true);
        featureStore.setEnabled(true);
    }

    @Test
    public void refreshWithoutTimeWatchKeyConfigStoreNotLoaded(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        assertFalse(
            AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
    }

    @Test
    public void refreshWithoutTimeWatchKeyConfigStoreWatchKeyNotReturned(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        State newState = new State(watchKeys, Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        // Config Store doesn't return a watch key change.
        when(clientMock.getWatchKey(Mockito.eq(KEY_FILTER), Mockito.eq(EMPTY_LABEL))).thenReturn(watchKeys.get(0));
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
        }
    }

    @Test
    public void refreshWithoutTimeWatchKeyConfigStoreWatchKeyNoChange(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        ConfigurationSetting updatedWatchKey = new ConfigurationSetting().setKey(KEY_FILTER).setLabel(EMPTY_LABEL)
            .setETag("updated");

        State newState = new State(watchKeys, Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        // Config Store does return a watch key change.
        when(clientMock.getWatchKey(Mockito.eq(KEY_FILTER), Mockito.eq(EMPTY_LABEL))).thenReturn(updatedWatchKey);
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            assertTrue(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
        }
    }

    @Test
    public void refreshWithoutTimeFeatureFlagDisabled(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        configStore.getFeatureFlags().setEnabled(false);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(false);

            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
        }
    }

    @Test
    public void refreshWithoutTimeFeatureFlagNotLoaded(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        configStore.getFeatureFlags().setEnabled(true);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(false);

            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
        }
    }

    @Test
    public void refreshWithoutTimeFeatureFlagNoChange(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        State newState = new State(watchKeysFeatureFlags, Math.toIntExact(Duration.ofMinutes(10).getSeconds()),
            endpoint);

        List<ConfigurationSetting> listedKeys = new ArrayList<>();
        listedKeys.add(watchKeysFeatureFlags.get(0));

        // Config Store doesn't return a watch key change.
        when(clientMock.listSettings(Mockito.any(SettingSelector.class))).thenReturn(flagsPagedIterableMock);
        when(flagsPagedIterableMock.iterator()).thenReturn(listedKeys.iterator());

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
        }

    }

    @Test
    public void refreshWithoutTimeFeatureFlagNoWatchKeyReturned(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        ConfigurationSetting updatedWatchKey = new FeatureFlagConfigurationSetting("Alpha", false)
            .setETag("updated");

        List<ConfigurationSetting> listedKeys = new ArrayList<>();
        listedKeys.add(updatedWatchKey);

        State newState = new State(watchKeysFeatureFlags, Math.toIntExact(Duration.ofMinutes(10).getSeconds()),
            endpoint);

        // Config Store does return a watch key change.
        when(clientMock.listSettings(Mockito.any(SettingSelector.class))).thenReturn(flagsPagedIterableMock);
        when(flagsPagedIterableMock.iterator()).thenReturn(listedKeys.iterator());
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            assertTrue(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
        }
    }

    @Test
    public void refreshWithoutTimeFeatureFlagWasDeleted(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        // Config Store doesn't return a value, Feature Flag was deleted
        when(clientMock.listSettings(Mockito.any(SettingSelector.class))).thenReturn(flagsPagedIterableMock);
        when(flagsPagedIterableMock.iterator()).thenReturn(new ArrayList<ConfigurationSetting>().iterator());

        State newState = new State(watchKeysFeatureFlags, Math.toIntExact(Duration.ofMinutes(10).getSeconds()),
            endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            assertTrue(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
        }
    }

    @Test
    public void refreshWithoutTimeFeatureFlagWasAdded(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        ConfigurationSetting extraFeatureFlag = new FeatureFlagConfigurationSetting("Beta", false)
            .setETag("new");

        List<ConfigurationSetting> listedKeys = generateFeatureFlagWatchKeys();
        listedKeys.add(extraFeatureFlag);
        State newState = new State(watchKeysFeatureFlags, Math.toIntExact(Duration.ofMinutes(10).getSeconds()),
            endpoint);

        // Config Store returns an new feature flag
        when(clientMock.listSettings(Mockito.any(SettingSelector.class))).thenReturn(flagsPagedIterableMock);
        when(flagsPagedIterableMock.iterator()).thenReturn(listedKeys.iterator());
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            assertTrue(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, clientMock, clientFactoryMock));
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestNotEnabled(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        clients.add(clientOriginMock);
        configStore.setEndpoint(endpoint);
        stores.add(configStore);

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(false);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            // Monitor is disabled
            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(appProperties, clientFactoryMock, stores,
                Duration.ofMinutes(10));
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestNotLoaded(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        clients.add(clientOriginMock);
        configStore.setEndpoint(endpoint);
        configStore.setMonitoring(monitoring);
        stores.add(configStore);

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(false);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(appProperties, clientFactoryMock, stores,
                Duration.ofMinutes(10));
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestNotRefreshTime(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        clients.add(clientOriginMock);
        configStore.setEndpoint(endpoint);
        configStore.setMonitoring(monitoring);
        stores.add(configStore);

        // Not Refresh Time
        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(appProperties, clientFactoryMock, stores,
                Duration.ofMinutes(10));
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestFailedRequest(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        clients.add(clientOriginMock);
        configStore.setEndpoint(endpoint);
        configStore.setMonitoring(monitoring);
        stores.add(configStore);

        // Refresh Time, but failed watch request
        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(appProperties, clientFactoryMock, stores,
                Duration.ofMinutes(10));
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(1)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestRefreshTimeNoChange(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        clients.add(clientOriginMock);
        configStore.setEndpoint(endpoint);
        configStore.setMonitoring(monitoring);
        stores.add(configStore);

        // Refresh Time, but no change
        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);
        when(clientOriginMock.getWatchKey(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(generateWatchKeys().get(0));

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(appProperties, clientFactoryMock, stores,
                Duration.ofMinutes(10));
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(1)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestTriggerRefresh(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        clients.add(clientOriginMock);
        configStore.setEndpoint(endpoint);
        configStore.setMonitoring(monitoring);
        stores.add(configStore);

        // Refresh Time, trigger refresh
        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        ConfigurationSetting refeshKey = new ConfigurationSetting().setKey(KEY_FILTER).setLabel(EMPTY_LABEL)
            .setETag("new");

        when(clientOriginMock.getWatchKey(Mockito.anyString(), Mockito.anyString())).thenReturn(refeshKey);

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);
            stateHolderMock.when(() -> StateHolder.getCurrentState()).thenReturn(currentStateMock);

            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(appProperties, clientFactoryMock, stores,
                Duration.ofMinutes(10));
            assertTrue(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(1)).getWatchKey(Mockito.anyString(), Mockito.anyString());
            verify(currentStateMock, times(1)).updateStateRefresh(Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void refreshStoresCheckFeatureFlagTestNotLoaded(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        clients.add(clientOriginMock);
        configStore.setEndpoint(endpoint);
        configStore.setFeatureFlags(featureStore);
        stores.add(configStore);

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateFeatureFlagWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()),
            endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(false);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            // Monitor is disabled
            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(appProperties, clientFactoryMock, stores,
                Duration.ofMinutes(10));
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckFeatureFlagTestNotRefreshTime(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        clients.add(clientOriginMock);
        configStore.setEndpoint(endpoint);
        configStore.setFeatureFlags(featureStore);
        stores.add(configStore);

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateFeatureFlagWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()),
            endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            // Monitor is disabled
            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(appProperties, clientFactoryMock, stores,
                Duration.ofMinutes(10));
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckFeatureFlagTestNoChange(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        clients.add(clientOriginMock);
        configStore.setEndpoint(endpoint);
        configStore.setFeatureFlags(featureStore);
        monitoring.setEnabled(false);
        configStore.setMonitoring(monitoring);
        stores.add(configStore);

        List<ConfigurationSetting> listedKeys = new ArrayList<>();
        listedKeys.add(watchKeysFeatureFlags.get(0));

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);
        when(clientOriginMock.listSettings(Mockito.any())).thenReturn(flagsPagedIterableMock);
        when(flagsPagedIterableMock.iterator()).thenReturn(listedKeys.iterator());

        State newState = new State(generateFeatureFlagWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()),
            endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);
            stateHolderMock.when(() -> StateHolder.getCurrentState()).thenReturn(currentStateMock);

            // Monitor is disabled
            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(appProperties, clientFactoryMock, stores,
                Duration.ofMinutes(10));
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
            verify(currentStateMock, times(1)).updateStateRefresh(Mockito.any(), Mockito.any());

        }
    }

    @Test
    public void refreshStoresCheckFeatureFlagTestTriggerRefresh(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        clients.add(clientOriginMock);
        configStore.setEndpoint(endpoint);
        configStore.setFeatureFlags(featureStore);
        stores.add(configStore);

        List<ConfigurationSetting> listedKeys = new ArrayList<>();

        FeatureFlagConfigurationSetting updated = (FeatureFlagConfigurationSetting) watchKeysFeatureFlags.get(0);
        updated.setETag("new");

        listedKeys.add(updated);

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);
        when(clientOriginMock.listSettings(Mockito.any())).thenReturn(flagsPagedIterableMock);
        when(flagsPagedIterableMock.iterator()).thenReturn(listedKeys.iterator());

        State newState = new State(generateFeatureFlagWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()),
            endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);
            stateHolderMock.when(() -> StateHolder.getCurrentState()).thenReturn(currentStateMock);

            // Monitor is disabled
            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(appProperties, clientFactoryMock, stores,
                Duration.ofMinutes(10));
            assertTrue(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
            verify(currentStateMock, times(1)).updateStateRefresh(Mockito.any(), Mockito.any());
        }
    }

    private List<ConfigurationSetting> generateWatchKeys() {
        List<ConfigurationSetting> watchKeys = new ArrayList<>();

        ConfigurationSetting currentWatchKey = new ConfigurationSetting().setKey(KEY_FILTER).setLabel(EMPTY_LABEL)
            .setETag("current");

        watchKeys.add(currentWatchKey);
        return watchKeys;
    }

    private List<ConfigurationSetting> generateFeatureFlagWatchKeys() {
        List<ConfigurationSetting> watchKeys = new ArrayList<>();

        FeatureFlagConfigurationSetting currentWatchKey = new FeatureFlagConfigurationSetting("Alpha", false)
            .setETag("current");
        watchKeys.add(currentWatchKey);
        return watchKeys;
    }
}
