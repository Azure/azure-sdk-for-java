// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

public class StateHolderTest {

    private List<ConfigurationSetting> watchKeys;
    private StateHolder stateHolder;
    private static final String TEST_ENDPOINT = "test.azconfig.io";

    @BeforeEach
    public void setup() {
        watchKeys = new ArrayList<>();
        ConfigurationSetting watchKey = new ConfigurationSetting().setKey("sentinel").setValue("0").setETag("current");
        watchKeys.add(watchKey);
        stateHolder = new StateHolder();
    }

    @Test
    public void stateNotExpiredTest() {
        stateHolder.setNextForcedRefresh(Duration.ofMinutes(10));
        stateHolder.setState(TEST_ENDPOINT, watchKeys, Duration.ofSeconds(30));

        State originalState = stateHolder.getState(TEST_ENDPOINT);
        assertNotNull(originalState);
        
        stateHolder.expireState(TEST_ENDPOINT);
        State newState = stateHolder.getState(TEST_ENDPOINT);
        
        // State should be different because expireState adds jitter
        assertNotEquals(originalState, newState);
    }

    @Test
    public void stateExpiredTest() {
        // State with negative duration is already expired
        stateHolder.setNextForcedRefresh(Duration.ofMinutes(10));
        stateHolder.setState(TEST_ENDPOINT, watchKeys, Duration.ofHours(-30));

        State originalState = stateHolder.getState(TEST_ENDPOINT);
        Instant originalRefreshCheck = originalState.getNextRefreshCheck();
        
        stateHolder.expireState(TEST_ENDPOINT);
        State newState = stateHolder.getState(TEST_ENDPOINT);
        
        // When state is already expired, expireState won't update if jitter would make it later
        // The check is: if wait < timeLeft, update. Since timeLeft is negative, wait is always >= timeLeft
        assertEquals(originalRefreshCheck, newState.getNextRefreshCheck());
    }

    @Test
    public void updateNextRefreshTimeNoRefreshTest() {
        stateHolder.setState(TEST_ENDPOINT, watchKeys, Duration.ofMinutes(10));

        State originalState = stateHolder.getState(TEST_ENDPOINT);

        stateHolder.updateNextRefreshTime(null, 0L);
        State newState = stateHolder.getState(TEST_ENDPOINT);
        
        assertEquals(originalState.getNextRefreshCheck(), newState.getNextRefreshCheck());
    }

    @Test
    public void updateNextRefreshTimeRefreshTest() {
        // Duration 0 means refresh immediately
        stateHolder.setState(TEST_ENDPOINT, watchKeys, Duration.ofMinutes(0));

        State originalState = stateHolder.getState(TEST_ENDPOINT);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("Sleep failed");
        }
        
        stateHolder.updateNextRefreshTime(null, 0L);
        State newState = stateHolder.getState(TEST_ENDPOINT);
        
        assertNotEquals(originalState.getNextRefreshCheck(), newState.getNextRefreshCheck());
        assertTrue(originalState.getNextRefreshCheck().isBefore(newState.getNextRefreshCheck()));
    }

    @Test
    public void updateNextRefreshBackoffCalcTest() {
        stateHolder.setState(TEST_ENDPOINT, watchKeys, Duration.ofMinutes(-1));
        State originalState = stateHolder.getState(TEST_ENDPOINT);

        try (MockedStatic<BackoffTimeCalculator> backoffTimeCalculatorMock = Mockito
            .mockStatic(BackoffTimeCalculator.class)) {
            Long ns = Long.valueOf("300000000000");
            backoffTimeCalculatorMock.when(() -> BackoffTimeCalculator.calculateBackoff(Mockito.anyInt()))
                .thenReturn(ns);

            stateHolder.updateNextRefreshTime(null, -120L);
            State newState = stateHolder.getState(TEST_ENDPOINT);

            assertTrue(originalState.getNextRefreshCheck().isBefore(newState.getNextRefreshCheck()));
            backoffTimeCalculatorMock.verify(() -> BackoffTimeCalculator.calculateBackoff(Mockito.anyInt()), times(1));
        }
    }

    @Test
    public void updateNextForcedRefreshTest() {
        Duration duration = Duration.ofMinutes(-1);
        stateHolder.setNextForcedRefresh(duration);
        stateHolder.setState(TEST_ENDPOINT, watchKeys, duration);

        Instant originalForcedRefresh = stateHolder.getNextForcedRefresh();

        stateHolder.updateNextRefreshTime(Duration.ofMinutes(11), 0L);

        Instant newForcedRefresh = stateHolder.getNextForcedRefresh();

        assertNotEquals(originalForcedRefresh, newForcedRefresh);
    }

    @Test
    public void loadStateTest() {
        assertFalse(stateHolder.getLoadState(TEST_ENDPOINT));
        
        stateHolder.setLoadState(TEST_ENDPOINT, true);
        
        assertTrue(stateHolder.getLoadState(TEST_ENDPOINT));
    }

    @Test
    public void getStateReturnsNullForUnknownEndpoint() {
        assertNotNull(stateHolder);
        assertEquals(null, stateHolder.getState("unknown.azconfig.io"));
    }

    @Test
    public void getStateFeatureFlagReturnsNullForUnknownEndpoint() {
        assertEquals(null, stateHolder.getStateFeatureFlag("unknown.azconfig.io"));
    }
}
