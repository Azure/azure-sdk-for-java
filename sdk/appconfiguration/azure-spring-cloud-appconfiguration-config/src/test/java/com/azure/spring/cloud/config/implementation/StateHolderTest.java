// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;

public class StateHolderTest {

    @Mock
    private StateHolder stateHolderMock;

    private AppConfigurationProviderProperties providerProperties;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        providerProperties = new AppConfigurationProviderProperties();
        providerProperties.setDefaultMaxBackoff((long) 10);
        providerProperties.setDefaultMinBackoff((long) 0);
    }

    @Test
    public void expireState() {
        String endpoint = "testEndpoint";
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();

        StateHolder testState = new StateHolder();
        testState.setNextForcedRefresh(Duration.ofMinutes(10));
        testState.setState(endpoint, watchKeys, monitoring.getRefreshInterval());

        StateHolder.updateState(testState);

        State state = StateHolder.getState(endpoint);
        testState.expireState(endpoint);
        StateHolder.updateState(testState);
        State currentState = StateHolder.getState(endpoint);
        assertNotEquals(state, currentState);
    }

    @Test
    public void notExpireState() {
        String endpoint = "notTestEndpoint";
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setRefreshInterval(Duration.ofSeconds(-30));

        StateHolder testState = new StateHolder();
        testState.setNextForcedRefresh(Duration.ofMinutes(10));
        testState.setState(endpoint, watchKeys, monitoring.getRefreshInterval());
        StateHolder.updateState(testState);

        State state = StateHolder.getState(endpoint);
        testState.expireState(endpoint);
        StateHolder.updateState(testState);
        State currentState = StateHolder.getState(endpoint);
        assertEquals(state, currentState);
    }

    @Test
    public void updateNextRefreshTimeNotExpired(TestInfo testInfo) {
        String endpoint = testInfo.getDisplayName() + ".azconfig.io";
        StateHolder state = new StateHolder();
        List<ConfigurationSetting> watchKeys = new ArrayList<>();
        Duration duration = Duration.ofMinutes((long) 10);

        ConfigurationSetting watchKey = new ConfigurationSetting().setKey("sentinal").setValue("0").setETag("current");

        watchKeys.add(watchKey);

        State originalState = new State(watchKeys, Math.toIntExact(duration.getSeconds()), endpoint);

        state.setState(originalState, duration);

        state.updateNextRefreshTime(null, providerProperties);
        StateHolder.updateState(state);
        State newState = StateHolder.getState(endpoint);
        assertEquals(originalState.getNextRefreshCheck(), newState.getNextRefreshCheck());

    }

}
