// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

public class StateHolderTest {

    private final List<ConfigurationSetting> watchKeys = new ArrayList<>();
    
    private MockitoSession session;

    @BeforeEach
    public void setup() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        MockitoAnnotations.openMocks(this);
        ConfigurationSetting watchKey = new ConfigurationSetting().setKey("sentinel").setValue("0").setETag("current");

        watchKeys.add(watchKey);
    }
    
    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    /**
     * Because of static code these need to run all at once.
     * @param testInfo
     */
    @Test
    public void stateHolderTest(TestInfo testInfo) {
        // Expire State Tests
        stateNotExpiredTest(testInfo);
        stateExpiredTest(testInfo);

        // Update Next Refresh Time Tests
        updateNextRefreshTimeNoRefreshTest(testInfo);
        updateNextRefreshTimeRefreshTest(testInfo);
        updateNextRefreshBackoffCalcTest(testInfo);

        // Load State Tests
        loadStateTest(testInfo);
    }

    private void stateNotExpiredTest(TestInfo testInfo) {
        // State isn't expired Test
        String endpoint = testInfo.getDisplayName() + "expire" + ".azconfig.io";

        StateHolder expireStateHolder = new StateHolder();
        expireStateHolder.setNextForcedRefresh(Duration.ofMinutes(10));
        expireStateHolder.setState(endpoint, watchKeys, Duration.ofSeconds(30));

        StateHolder.updateState(expireStateHolder);

        State originalExpireState = StateHolder.getState(endpoint);
        expireStateHolder.expireState(endpoint);
        StateHolder.updateState(expireStateHolder);
        assertNotEquals(originalExpireState, StateHolder.getState(endpoint));
    }

    private void stateExpiredTest(TestInfo testInfo) {
        // State is expired Test
        String endpoint = testInfo.getDisplayName() + "expireNegativeDuration" + ".azconfig.io";

        StateHolder expiredNegativeDurationStateHolder = new StateHolder();
        expiredNegativeDurationStateHolder.setNextForcedRefresh(Duration.ofMinutes(10));
        expiredNegativeDurationStateHolder.setState(endpoint, watchKeys, Duration.ofHours(-30));

        StateHolder.updateState(expiredNegativeDurationStateHolder);

        State originalExpireNegativeState = StateHolder.getState(endpoint);
        expiredNegativeDurationStateHolder.expireState(endpoint);
        StateHolder.updateState(expiredNegativeDurationStateHolder);
        assertEquals(originalExpireNegativeState, StateHolder.getState(endpoint));
    }

    private void updateNextRefreshTimeNoRefreshTest(TestInfo testInfo) {
        String endpoint = testInfo.getDisplayName() + "updateRefreshTime" + ".azconfig.io";

        StateHolder stateHolder = new StateHolder();

        stateHolder.setState(endpoint, watchKeys, Duration.ofMinutes((long) 10));

        StateHolder.updateState(stateHolder);

        State originalState = StateHolder.getState(endpoint);

        stateHolder.updateNextRefreshTime(null, (long) 0);
        StateHolder.updateState(stateHolder);
        State newState = StateHolder.getState(endpoint);
        assertEquals(originalState.getNextRefreshCheck(), newState.getNextRefreshCheck());
    }

    private void updateNextRefreshTimeRefreshTest(TestInfo testInfo) {
        String endpoint = testInfo.getDisplayName() + "updateRefreshTimeRefresh" + ".azconfig.io";

        StateHolder stateHolder = new StateHolder();

        stateHolder.setState(endpoint, watchKeys, Duration.ofMinutes((long) 0));

        StateHolder.updateState(stateHolder);

        State originalState = StateHolder.getState(endpoint);

        // Duration is less than the minBackOff
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("Sleep failed");
        }
        stateHolder.updateNextRefreshTime(null, (long) 0);
        State newState = StateHolder.getState(endpoint);
        assertNotEquals(originalState.getNextRefreshCheck(), newState.getNextRefreshCheck());
        assertTrue(originalState.getNextRefreshCheck().isBefore(newState.getNextRefreshCheck()));
    }

    private void updateNextRefreshBackoffCalcTest(TestInfo testInfo) {
        String endpoint = testInfo.getDisplayName() + "updateRefreshTimeBackoffCalc" + ".azconfig.io";

        StateHolder stateHolder = new StateHolder();
        stateHolder.setState(endpoint, watchKeys, Duration.ofMinutes((long) -1));
        StateHolder.updateState(stateHolder);
        State originalState = StateHolder.getState(endpoint);

        // Duration is less than the minBackOff
        try (MockedStatic<BackoffTimeCalculator> backoffTimeCalculatorMock = Mockito
            .mockStatic(BackoffTimeCalculator.class)) {
            Long ns = Long.valueOf("300000000000");
            backoffTimeCalculatorMock.when(() -> BackoffTimeCalculator.calculateBackoff(Mockito.anyInt()))
                .thenReturn(ns);

            stateHolder.updateNextRefreshTime(null, (long) -120);
            State newState = StateHolder.getState(endpoint);

            assertTrue(originalState.getNextRefreshCheck().isBefore(newState.getNextRefreshCheck()));
            backoffTimeCalculatorMock.verify(() -> BackoffTimeCalculator.calculateBackoff(Mockito.anyInt()), times(1));
        }

        stateHolder = new StateHolder();
        Duration duration = Duration.ofMinutes((long) -1);

        stateHolder.setNextForcedRefresh(duration);

        stateHolder.setState(endpoint, watchKeys, duration);

        StateHolder.updateState(stateHolder);

        Instant originalForcedRefresh = StateHolder.getNextForcedRefresh();

        stateHolder.updateNextRefreshTime(Duration.ofMinutes((long) 11), (long) 0);

        Instant newForcedRefresh = StateHolder.getNextForcedRefresh();

        assertNotEquals(originalForcedRefresh, newForcedRefresh);
    }

    private void loadStateTest(TestInfo testInfo) {
        String endpoint = testInfo.getDisplayName() + "updateRefreshTimeBackoffCalc" + ".azconfig.io";
        StateHolder testStateHolder = new StateHolder();
        testStateHolder.setLoadState(endpoint, true, false);
        StateHolder.updateState(testStateHolder);
        assertEquals(testStateHolder.getLoadState().get(endpoint), StateHolder.getLoadState(endpoint));
        assertEquals(testStateHolder, StateHolder.getCurrentState());
    }

}
