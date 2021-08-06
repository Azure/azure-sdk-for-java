// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;

public class StateHolderTest {

    @Test
    public void expireState() {
        String endpoint = "testEndpoint";
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        StateHolder.setState(endpoint, watchKeys, monitoring.getRefreshInterval());
        State state = StateHolder.getState(endpoint);
        StateHolder.expireState(endpoint);
        State currentState = StateHolder.getState(endpoint);
        assertNotEquals(state, currentState);
    }


    @Test
    public void notExpireState() {
        String endpoint = "testEndpoint";
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setRefreshInterval(Duration.ofSeconds(-30));
        StateHolder.setState(endpoint, watchKeys, monitoring.getRefreshInterval());
        State state = StateHolder.getState(endpoint);
        StateHolder.expireState(endpoint);
        State currentState = StateHolder.getState(endpoint);
        assertEquals(state, currentState);
    }

}
