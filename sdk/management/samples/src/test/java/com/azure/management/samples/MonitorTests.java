// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.samples;


import com.azure.management.monitor.samples.AutoscaleSettingsBasedOnPerformanceOrSchedule;
import com.azure.management.monitor.samples.QueryMetricsAndActivityLogs;
import com.azure.management.monitor.samples.SecurityBreachOrRiskActivityLogAlerts;
import com.azure.management.monitor.samples.WebAppPerformanceMonitoringAlerts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MonitorTests extends SamplesTestBase {

    @Test
    @Disabled("Live only sample due to the need to call non-management endpoints.")
    public void testQueryMetricsAndActivityLogs() {
        Assertions.assertTrue(QueryMetricsAndActivityLogs.runSample(azure));
    }

    @Test
    public void testSecurityBreachOrRiskActivityLogAlerts() {
        Assertions.assertTrue(SecurityBreachOrRiskActivityLogAlerts.runSample(azure));
    }

    @Test
    public void testWebAppPerformanceMonitoringAlerts() {
        Assertions.assertTrue(WebAppPerformanceMonitoringAlerts.runSample(azure));
    }

    @Test
    @Disabled("Live only sample due to the need to call non-management endpoints.")
    public void testAutoscaleSettingsBasedOnPerformanceOrSchedule() {
        Assertions.assertTrue(AutoscaleSettingsBasedOnPerformanceOrSchedule.runSample(azure));
    }
}
