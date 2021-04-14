// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class StateHolderTest {

    @Test
    public void expireState() {
        String endpoint = "testEndpoint";
        List<ConfigurationSetting> watchKeys = new ArrayList<ConfigurationSetting>();

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        StateHolder.setState(endpoint, watchKeys, monitoring.getCacheExpiration());
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
        monitoring.setCacheExpiration(Duration.ofSeconds(-30));
        StateHolder.setState(endpoint, watchKeys, monitoring.getCacheExpiration());
        State state = StateHolder.getState(endpoint);
        StateHolder.expireState(endpoint);
        State currentState = StateHolder.getState(endpoint);
        assertEquals(state, currentState);
    }

}
