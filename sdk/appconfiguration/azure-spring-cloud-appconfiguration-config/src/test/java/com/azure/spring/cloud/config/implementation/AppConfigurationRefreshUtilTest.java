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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.implementation.AppConfigurationRefreshUtil.RefreshEventData;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;

public class AppConfigurationRefreshUtilTest {

    private static final String KEY_FILTER = "/application/*";

    @Mock
    private AppConfigurationReplicaClient clientMock;

    @Mock
    private AppConfigurationReplicaClientFactory clientFactoryMock;

    @Mock
    private List<ConfigurationSetting> configurationListMock;

    @Mock
    private AppConfigurationReplicaClient clientOriginMock;

    @Mock
    private StateHolder currentStateMock;

    @Mock
    private ConnectionManager connectionManagerMock;

    private String endpoint;

    private RefreshEventData eventData = new RefreshEventData();

    private List<AppConfigurationReplicaClient> clients = new ArrayList<>();

    private List<ConfigurationSetting> watchKeys = generateWatchKeys();

    private List<ConfigurationSetting> watchKeysFeatureFlags = generateFeatureFlagWatchKeys();

    private AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();

    private FeatureFlagStore featureStore = new FeatureFlagStore();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        featureStore.setEnabled(true);

        monitoring.setEnabled(true);
        featureStore.setEnabled(true);
    }

    @Test
    public void refreshWithoutTimeWatchKeyConfigStoreNotLoaded(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        when(clientMock.getWatchKey(Mockito.eq(KEY_FILTER), Mockito.eq(EMPTY_LABEL))).thenReturn(watchKeys.get(0));
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(false);

            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
        }
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
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
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

            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
        }
    }

    @Test
    public void refreshWithoutTimeFeatureFlagDisabled(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        featureStore.setEnabled(false);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(false);

            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
        }
    }

    @Test
    public void refreshWithoutTimeFeatureFlagNotLoaded(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        featureStore.setEnabled(true);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(false);

            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
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
        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class)))
            .thenReturn(configurationListMock);
        when(configurationListMock.iterator()).thenReturn(listedKeys.iterator());

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
        }

    }

    @Test
    public void refreshWithoutTimeFeatureFlagNoWatchKeyReturned(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        ConfigurationSetting updatedWatchKey = new FeatureFlagConfigurationSetting("Alpha", false)
            .setETag("updated").setLabel(EMPTY_LABEL);

        List<ConfigurationSetting> listedKeys = new ArrayList<>();
        listedKeys.add(updatedWatchKey);

        State newState = new State(watchKeysFeatureFlags, Math.toIntExact(Duration.ofMinutes(10).getSeconds()),
            endpoint);

        // Config Store does return a watch key change.
        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class)))
            .thenReturn(configurationListMock);
        when(configurationListMock.iterator()).thenReturn(listedKeys.iterator());
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            assertTrue(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
        }
    }

    @Test
    public void refreshWithoutTimeFeatureFlagWasDeleted(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        // Config Store doesn't return a value, Feature Flag was deleted
        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class)))
            .thenReturn(configurationListMock);
        when(configurationListMock.iterator()).thenReturn(new ArrayList<ConfigurationSetting>().iterator());

        State newState = new State(watchKeysFeatureFlags, Math.toIntExact(Duration.ofMinutes(10).getSeconds()),
            endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            assertTrue(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
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

        // Config Store returns a new feature flag
        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class)))
            .thenReturn(configurationListMock);
        when(configurationListMock.iterator()).thenReturn(listedKeys.iterator());
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            assertTrue(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestNotEnabled(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);

        clients.add(clientOriginMock);

        Map<String, ConnectionManager> connections = new HashMap<>();
        connections.put(endpoint, connectionManagerMock);

        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(connectionManagerMock.getFeatureFlagStore()).thenReturn(featureStore);
        when(clientFactoryMock.getConnections()).thenReturn(connections);

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(false);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            // Monitor is disabled
            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 0);
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

        Map<String, ConnectionManager> connections = new HashMap<>();
        connections.put(endpoint, connectionManagerMock);

        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(connectionManagerMock.getFeatureFlagStore()).thenReturn(featureStore);
        when(clientFactoryMock.getConnections()).thenReturn(connections);

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(false);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 0);
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

        Map<String, ConnectionManager> connections = new HashMap<>();
        connections.put(endpoint, connectionManagerMock);

        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(connectionManagerMock.getFeatureFlagStore()).thenReturn(featureStore);
        when(clientFactoryMock.getConnections()).thenReturn(connections);

        // Not Refresh Time
        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 0);
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

        Map<String, ConnectionManager> connections = new HashMap<>();
        connections.put(endpoint, connectionManagerMock);

        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(connectionManagerMock.getFeatureFlagStore()).thenReturn(featureStore);
        when(clientFactoryMock.getConnections()).thenReturn(connections);

        // Refresh Time, but failed watch request
        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 0);
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

        Map<String, ConnectionManager> connections = new HashMap<>();
        connections.put(endpoint, connectionManagerMock);

        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(connectionManagerMock.getFeatureFlagStore()).thenReturn(featureStore);
        when(clientFactoryMock.getConnections()).thenReturn(connections);

        // Refresh Time, but no change
        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);
        when(clientOriginMock.getWatchKey(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(generateWatchKeys().get(0));

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 0);
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

        Map<String, ConnectionManager> connections = new HashMap<>();
        connections.put(endpoint, connectionManagerMock);

        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(connectionManagerMock.getFeatureFlagStore()).thenReturn(featureStore);
        when(clientFactoryMock.getConnections()).thenReturn(connections);

        // Refresh Time, trigger refresh
        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        ConfigurationSetting refreshKey = new ConfigurationSetting().setKey(KEY_FILTER).setLabel(EMPTY_LABEL)
            .setETag("new");

        when(clientOriginMock.getWatchKey(Mockito.anyString(), Mockito.anyString())).thenReturn(refreshKey);

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);
            stateHolderMock.when(() -> StateHolder.getCurrentState()).thenReturn(currentStateMock);

            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 0);
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

        Map<String, ConnectionManager> connections = new HashMap<>();
        connections.put(endpoint, connectionManagerMock);

        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(connectionManagerMock.getFeatureFlagStore()).thenReturn(featureStore);
        when(clientFactoryMock.getConnections()).thenReturn(connections);

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateFeatureFlagWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()),
            endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(false);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            // Monitor is disabled
            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 0);
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

        Map<String, ConnectionManager> connections = new HashMap<>();
        connections.put(endpoint, connectionManagerMock);

        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(connectionManagerMock.getFeatureFlagStore()).thenReturn(featureStore);
        when(clientFactoryMock.getConnections()).thenReturn(connections);

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);

        State newState = new State(generateFeatureFlagWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()),
            endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            // Monitor is disabled
            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 0);
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
        monitoring.setEnabled(false);

        List<ConfigurationSetting> listedKeys = new ArrayList<>();
        listedKeys.add(watchKeysFeatureFlags.get(0));

        Map<String, ConnectionManager> connections = new HashMap<>();
        connections.put(endpoint, connectionManagerMock);

        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(connectionManagerMock.getFeatureFlagStore()).thenReturn(featureStore);
        when(clientFactoryMock.getConnections()).thenReturn(connections);

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);
        when(clientOriginMock.listConfigurationSettings(Mockito.any())).thenReturn(configurationListMock);
        when(configurationListMock.iterator()).thenReturn(listedKeys.iterator());

        State newState = new State(generateFeatureFlagWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()),
            endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);
            stateHolderMock.when(() -> StateHolder.getCurrentState()).thenReturn(currentStateMock);

            // Monitor is disabled
            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 0);
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

        Map<String, ConnectionManager> connections = new HashMap<>();
        connections.put(endpoint, connectionManagerMock);

        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(connectionManagerMock.getFeatureFlagStore()).thenReturn(featureStore);
        when(clientFactoryMock.getConnections()).thenReturn(connections);

        List<ConfigurationSetting> listedKeys = new ArrayList<>();

        FeatureFlagConfigurationSetting updated = (FeatureFlagConfigurationSetting) watchKeysFeatureFlags.get(0);
        updated.setETag("new");

        listedKeys.add(updated);

        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(clients);
        when(clientOriginMock.listConfigurationSettings(Mockito.any())).thenReturn(configurationListMock);
        when(configurationListMock.iterator()).thenReturn(listedKeys.iterator());

        State newState = new State(generateFeatureFlagWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()),
            endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadStateFeatureFlag(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);
            stateHolderMock.when(() -> StateHolder.getCurrentState()).thenReturn(currentStateMock);

            // Monitor is disabled
            eventData = AppConfigurationRefreshUtil.refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 0);
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
            .setETag("current").setLabel(EMPTY_LABEL);
        watchKeys.add(currentWatchKey);
        return watchKeys;
    }
}
