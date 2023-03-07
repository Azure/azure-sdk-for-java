// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class AppConfigurationStoreMonitoringTest {

    @Test
    public void validateAndInitTest() {
        // Disabled anything is fine
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.validateAndInit();

        // Enabled throw error if no triggers
        monitoring.setEnabled(true);
        assertThrows(IllegalArgumentException.class, () -> monitoring.validateAndInit());

        List<AppConfigurationStoreTrigger> triggers = new ArrayList<>();
        monitoring.setTriggers(triggers);

        assertThrows(IllegalArgumentException.class, () -> monitoring.validateAndInit());

        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey("sentinal");

        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        monitoring.validateAndInit();

        monitoring.setRefreshInterval(Duration.ofSeconds(0));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> monitoring.validateAndInit());
        assertEquals("Minimum refresh interval time is 1 Second.", e.getMessage());

        monitoring.setRefreshInterval(Duration.ofSeconds(1));
        monitoring.setFeatureFlagRefreshInterval(Duration.ofSeconds(0));

        e = assertThrows(IllegalArgumentException.class, () -> monitoring.validateAndInit());
        assertEquals("Minimum Feature Flag refresh interval time is 1 Second.", e.getMessage());

        monitoring.setFeatureFlagRefreshInterval(Duration.ofSeconds(1));

        monitoring.validateAndInit();
    }

}
