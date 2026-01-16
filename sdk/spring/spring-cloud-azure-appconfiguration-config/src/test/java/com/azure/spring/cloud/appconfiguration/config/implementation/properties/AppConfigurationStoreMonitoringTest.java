// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class AppConfigurationStoreMonitoringTest {

    @Test
    public void validateAndInitTest() {
        // Disabled anything is fine
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.validateAndInit();

        List<AppConfigurationStoreTrigger> triggers = new ArrayList<>();
        monitoring.setTriggers(triggers);

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

    @Test
    public void refreshAllEnabledWithoutTriggersTest() {
        // When refreshAll is enabled, triggers are not required
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);

        // Should not throw an exception even with no triggers
        monitoring.validateAndInit();
        
        // Verify refresh interval validation still applies
        monitoring.setRefreshInterval(Duration.ofSeconds(0));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> monitoring.validateAndInit());
        assertEquals("Minimum refresh interval time is 1 Second.", e.getMessage());
    }

    @Test
    public void refreshAllWithTriggersTest() {
        // Even when refreshAll is enabled, having triggers should still be valid
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(true);
        
        List<AppConfigurationStoreTrigger> triggers = new ArrayList<>();
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey("sentinel");
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        
        // Should not throw an exception
        monitoring.validateAndInit();
    }

    @Test
    public void monitoringDisabledWithRefreshAllTest() {
        // When monitoring is disabled, refreshAll setting should not matter
        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(false);
        
        // Should not throw an exception even with no triggers
        monitoring.validateAndInit();
    }

}
