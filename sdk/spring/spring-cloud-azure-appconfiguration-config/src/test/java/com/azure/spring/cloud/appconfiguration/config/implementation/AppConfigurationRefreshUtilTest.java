// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.EMPTY_LABEL;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationRefreshUtil.RefreshEventData;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlagState;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;

public class AppConfigurationRefreshUtilTest {

    private static final String KEY_FILTER = "/application/*";

    @Mock
    private AppConfigurationReplicaClient clientMock;

    @Mock
    private AppConfigurationReplicaClientFactory clientFactoryMock;

    @Mock
    private AppConfigurationReplicaClient clientOriginMock;

    @Mock
    private StateHolder currentStateMock;

    @Mock
    private ConnectionManager connectionManagerMock;

    @Mock
    private ReplicaLookUp replicaLookUpMock;

    private ConfigStore configStore;

    private String endpoint;

    private final List<ConfigurationSetting> watchKeysFeatureFlags = generateFeatureFlagWatchKeys();

    private final AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();

    private final FeatureFlagStore featureStore = new FeatureFlagStore();

    private MockitoSession session;

    @BeforeEach
    public void setup() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        MockitoAnnotations.openMocks(this);
        configStore = new ConfigStore();

        featureStore.setEnabled(true);

        FeatureFlagKeyValueSelector ffSelect = new FeatureFlagKeyValueSelector().setKeyFilter(FEATURE_FLAG_PREFIX)
            .setLabelFilter(EMPTY_LABEL);
        featureStore.setSelects(List.of(ffSelect));
        configStore.setFeatureFlags(featureStore);

        monitoring.setEnabled(true);
        featureStore.setEnabled(true);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void refreshWithoutTimeWatchKeyConfigStoreNotLoaded(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);
        when(clientFactoryMock.findOriginForEndpoint(Mockito.eq(endpoint))).thenReturn(endpoint);

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
        when(clientFactoryMock.findOriginForEndpoint(Mockito.eq(endpoint))).thenReturn(endpoint);

        List<ConfigurationSetting> watchKeys = generateWatchKeys();
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
        when(clientFactoryMock.findOriginForEndpoint(Mockito.eq(endpoint))).thenReturn(endpoint);

        FeatureFlagState newState = new FeatureFlagState(
            List.of(new FeatureFlags(new SettingSelector().setKeyFilter(KEY_FILTER).setLabelFilter(EMPTY_LABEL), null)),
            Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        // Config Store does return a watch key change.
        when(clientMock.checkWatchKeys(Mockito.any(SettingSelector.class))).thenReturn(false);
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
        }
    }

    @SuppressWarnings("try")
    @Test
    public void refreshWithoutTimeFeatureFlagDisabled(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);
        when(clientFactoryMock.findOriginForEndpoint(Mockito.eq(endpoint))).thenReturn(endpoint);

        configStore.getFeatureFlags().setEnabled(false);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
        }
    }

    @SuppressWarnings("try")
    @Test
    public void refreshWithoutTimeFeatureFlagNotLoaded(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);
        when(clientFactoryMock.findOriginForEndpoint(Mockito.eq(endpoint))).thenReturn(endpoint);

        configStore.getFeatureFlags().setEnabled(true);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
        }
    }

    @Test
    public void refreshWithoutTimeFeatureFlagNoChange(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);
        when(clientFactoryMock.findOriginForEndpoint(Mockito.eq(endpoint))).thenReturn(endpoint);

        FeatureFlagState newState = new FeatureFlagState(
            List.of(new FeatureFlags(new SettingSelector().setKeyFilter(KEY_FILTER).setLabelFilter(EMPTY_LABEL), null)),
            Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        // Config Store doesn't return a watch key change.
        when(clientMock.checkWatchKeys(Mockito.any(SettingSelector.class))).thenReturn(false);
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            assertFalse(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
        }

    }

    @Test
    public void refreshWithoutTimeFeatureFlagEtagChanged(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(clientMock.getEndpoint()).thenReturn(endpoint);
        when(clientFactoryMock.findOriginForEndpoint(Mockito.eq(endpoint))).thenReturn(endpoint);

        FeatureFlags featureFlags = new FeatureFlags(new SettingSelector(), watchKeysFeatureFlags);
        FeatureFlagState newState = new FeatureFlagState(List.of(featureFlags),
            Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        // Config Store does return a watch key change.
        when(clientMock.checkWatchKeys(Mockito.any(SettingSelector.class))).thenReturn(true);
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            assertTrue(
                AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(clientMock, clientFactoryMock, featureStore));
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestNotEnabled(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        setupFeatureFlagLoad();

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(false);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            // Monitor is disabled
            RefreshEventData eventData = new AppConfigurationRefreshUtil().refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10),
                (long) 60, replicaLookUpMock);
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestNotLoaded(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        setupFeatureFlagLoad();

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(false);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            RefreshEventData eventData = new AppConfigurationRefreshUtil().refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 60, replicaLookUpMock);
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestNotRefreshTime(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        setupFeatureFlagLoad();

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);

            RefreshEventData eventData = new AppConfigurationRefreshUtil().refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10),
                (long) 60, replicaLookUpMock);
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestFailedRequest(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        setupFeatureFlagLoad();

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);
            StateHolder updatedStateHolder = new StateHolder();
            stateHolderMock.when(() -> StateHolder.getCurrentState()).thenReturn(updatedStateHolder);

            RefreshEventData eventData = new AppConfigurationRefreshUtil().refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10),
                (long) 60, replicaLookUpMock);
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(1)).getWatchKey(Mockito.anyString(), Mockito.anyString());
            assertEquals(newState, StateHolder.getState(endpoint));
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestRefreshTimeNoChange(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        setupFeatureFlagLoad();

        when(clientOriginMock.getWatchKey(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(generateWatchKeys().get(0));

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(Mockito.any())).thenReturn(newState);
            StateHolder updatedStateHolder = new StateHolder();
            stateHolderMock.when(() -> StateHolder.getCurrentState()).thenReturn(updatedStateHolder);

            RefreshEventData eventData = new AppConfigurationRefreshUtil().refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 60, replicaLookUpMock);
            assertEquals(newState, StateHolder.getState(endpoint));
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(1)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckSettingsTestTriggerRefresh(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(clientFactoryMock.getConnections()).thenReturn(Map.of(endpoint, connectionManagerMock));

        // Refresh Time, trigger refresh
        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(List.of(clientOriginMock));

        ConfigurationSetting refreshKey = new ConfigurationSetting().setKey(KEY_FILTER).setLabel(EMPTY_LABEL)
            .setETag("new");

        when(clientOriginMock.getWatchKey(Mockito.anyString(), Mockito.anyString())).thenReturn(refreshKey);

        State newState = new State(generateWatchKeys(), Math.toIntExact(Duration.ofMinutes(-1).getSeconds()), endpoint);

        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getLoadState(endpoint)).thenReturn(true);
            stateHolderMock.when(() -> StateHolder.getState(endpoint)).thenReturn(newState);
            stateHolderMock.when(StateHolder::getCurrentState).thenReturn(currentStateMock);

            RefreshEventData eventData = new AppConfigurationRefreshUtil().refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10),
                (long) 60, replicaLookUpMock);
            assertTrue(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(1)).getWatchKey(Mockito.anyString(), Mockito.anyString());
            verify(currentStateMock, times(1)).updateStateRefresh(Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void refreshStoresCheckFeatureFlagTestNotLoaded(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        setupFeatureFlagLoad();

        FeatureFlagState newState = new FeatureFlagState(List.of(),
            Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            // Monitor is disabled
            RefreshEventData eventData = new AppConfigurationRefreshUtil().refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10),
                (long) 60, replicaLookUpMock);
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckFeatureFlagTestNotRefreshTime(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        setupFeatureFlagLoad();

        FeatureFlagState newState = new FeatureFlagState(List.of(),
            Math.toIntExact(Duration.ofMinutes(10).getSeconds()), endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);

            // Monitor is disabled
            RefreshEventData eventData = new AppConfigurationRefreshUtil().refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10),
                (long) 60, replicaLookUpMock);
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void refreshStoresCheckFeatureFlagTestNoChange(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        configStore.setEndpoint(endpoint);
        configStore.setFeatureFlags(featureStore);
        configStore.setMonitoring(monitoring);

        setupFeatureFlagLoad();
        when(clientOriginMock.checkWatchKeys(Mockito.any())).thenReturn(false);

        FeatureFlagState newState = new FeatureFlagState(
            List.of(new FeatureFlags(new SettingSelector().setKeyFilter(KEY_FILTER).setLabelFilter(EMPTY_LABEL), null)),
            Math.toIntExact(Duration.ofMinutes(-1).getSeconds()), endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);
            stateHolderMock.when(StateHolder::getCurrentState).thenReturn(currentStateMock);

            // Monitor is disabled
            RefreshEventData eventData = new AppConfigurationRefreshUtil().refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 60, replicaLookUpMock);
            assertFalse(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
            verify(currentStateMock, times(1)).updateFeatureFlagStateRefresh(Mockito.any(), Mockito.any());

        }
    }

    @Test
    public void refreshStoresCheckFeatureFlagTestTriggerRefresh(TestInfo testInfo) {
        endpoint = testInfo.getDisplayName() + ".azconfig.io";
        setupFeatureFlagLoad();
        when(clientOriginMock.checkWatchKeys(Mockito.any())).thenReturn(true);

        FeatureFlags featureFlags = new FeatureFlags(new SettingSelector(), watchKeysFeatureFlags);

        FeatureFlagState newState = new FeatureFlagState(List.of(featureFlags),
            Math.toIntExact(Duration.ofMinutes(-1).getSeconds()), endpoint);

        // Config Store doesn't return a watch key change.
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getStateFeatureFlag(endpoint)).thenReturn(newState);
            stateHolderMock.when(StateHolder::getCurrentState).thenReturn(currentStateMock);

            // Monitor is disabled
            RefreshEventData eventData = new AppConfigurationRefreshUtil().refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(10), (long) 60, replicaLookUpMock);
            assertTrue(eventData.getDoRefresh());
            verify(clientFactoryMock, times(1)).setCurrentConfigStoreClient(Mockito.eq(endpoint), Mockito.eq(endpoint));
            verify(clientOriginMock, times(0)).getWatchKey(Mockito.anyString(), Mockito.anyString());
        }
    }

    @Test
    public void minRefreshPeriodTest() {
        try (MockedStatic<StateHolder> stateHolderMock = Mockito.mockStatic(StateHolder.class)) {
            stateHolderMock.when(() -> StateHolder.getNextForcedRefresh()).thenReturn(Instant.now().minusSeconds(600));
            RefreshEventData eventData = new AppConfigurationRefreshUtil().refreshStoresCheck(clientFactoryMock,
                Duration.ofMinutes(1), (long) 0, replicaLookUpMock);
            assertTrue(eventData.getDoRefresh());
            assertEquals("Minimum refresh period reached. Refreshing configurations.", eventData.getMessage());
        }
    }

    private void setupFeatureFlagLoad() {
        when(connectionManagerMock.getMonitoring()).thenReturn(monitoring);
        when(connectionManagerMock.getFeatureFlagStore()).thenReturn(featureStore);
        when(clientFactoryMock.getConnections()).thenReturn(Map.of(endpoint, connectionManagerMock));
        when(clientFactoryMock.getAvailableClients(Mockito.eq(endpoint))).thenReturn(List.of(clientOriginMock));
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
