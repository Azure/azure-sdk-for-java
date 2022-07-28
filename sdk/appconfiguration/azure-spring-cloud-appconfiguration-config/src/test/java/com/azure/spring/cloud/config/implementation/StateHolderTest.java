// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;

public class StateHolderTest {

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
    public void updateNextRefreshTimeTest(TestInfo testInfo) {
        String endpoint = testInfo.getDisplayName() + ".azconfig.io";
        StateHolder stateHolder = new StateHolder();
        List<ConfigurationSetting> watchKeys = new ArrayList<>();
        Duration duration = Duration.ofMinutes((long) 10);

        ConfigurationSetting watchKey = new ConfigurationSetting().setKey("sentinal").setValue("0").setETag("current");

        watchKeys.add(watchKey);

        stateHolder.setState(endpoint, watchKeys, duration);

        StateHolder.updateState(stateHolder);

        State originalState = StateHolder.getState(endpoint);

        stateHolder.updateNextRefreshTime(null, providerProperties);
        StateHolder.updateState(stateHolder);
        State newState = StateHolder.getState(endpoint);
        assertEquals(originalState.getNextRefreshCheck(), newState.getNextRefreshCheck());

        // Test 2
        stateHolder.setState(endpoint, watchKeys, Duration.ofMinutes((long) -1));
        StateHolder.updateState(stateHolder);
        originalState = StateHolder.getState(endpoint);

        // Duration is less than the minBackOff
        stateHolder.updateNextRefreshTime(null, providerProperties);
        newState = StateHolder.getState(endpoint);
        assertTrue(originalState.getNextRefreshCheck().isBefore(newState.getNextRefreshCheck()));

        // Test 3
        stateHolder.setState(endpoint, watchKeys, Duration.ofMinutes((long) -1));
        StateHolder.updateState(stateHolder);
        originalState = StateHolder.getState(endpoint);
        providerProperties.setDefaultMinBackoff((long) -120);

        // Duration is less than the minBackOff
        try (MockedStatic<BackoffTimeCalculator> backoffTimeCalculatorMock = Mockito
            .mockStatic(BackoffTimeCalculator.class)) {
            Long ns = Long.valueOf("300000000000");
            backoffTimeCalculatorMock
                .when(() -> BackoffTimeCalculator.calculateBackoff(Mockito.anyInt(), Mockito.any(), Mockito.any()))
                .thenReturn(ns);

            stateHolder.updateNextRefreshTime(null, providerProperties);
            newState = StateHolder.getState(endpoint);

            assertTrue(originalState.getNextRefreshCheck().isBefore(newState.getNextRefreshCheck()));
            backoffTimeCalculatorMock.verify(() -> BackoffTimeCalculator.calculateBackoff(Mockito.anyInt(),
                Mockito.any(), Mockito.any()), times(1));
        }
    }

    @Test
    public void updateNextRefreshForcedRefresh(TestInfo testInfo) {
        String endpoint = testInfo.getDisplayName() + ".azconfig.io";
        StateHolder stateHolder = new StateHolder();
        Duration duration = Duration.ofMinutes((long) -1);
        List<ConfigurationSetting> watchKeys = new ArrayList<>();
        stateHolder.setNextForcedRefresh(duration);

        ConfigurationSetting watchKey = new ConfigurationSetting().setKey("sentinal").setValue("0").setETag("current");

        watchKeys.add(watchKey);

        stateHolder.setState(endpoint, watchKeys, duration);

        StateHolder.updateState(stateHolder);

        Instant originalForcedRefresh = StateHolder.getNextForcedRefresh();

        stateHolder.updateNextRefreshTime(Duration.ofMinutes((long) 11), providerProperties);

        Instant newForcedRefresh = StateHolder.getNextForcedRefresh();

        assertNotEquals(originalForcedRefresh, newForcedRefresh);
    }

}
