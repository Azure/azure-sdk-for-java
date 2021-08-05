// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AlertQueryTimeMode;
import com.azure.ai.metricsadvisor.models.AnomalyAlert;
import com.azure.ai.metricsadvisor.models.ListAlertOptions;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

public abstract class AlertTestBase extends MetricsAdvisorClientTestBase {

    @Test
    public abstract void listAlerts(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);
    public static final String ALERT_CONFIG_ID = "204a211a-c5f4-45f3-a30e-512fb25d1d2c";

    // Pre-configured test resource.
    protected static class ListAlertsInput {
        static final ListAlertsInput INSTANCE = new ListAlertsInput();
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-10-10T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-10-21T00:00:00Z");
        final AlertQueryTimeMode timeMode = AlertQueryTimeMode.ANOMALY_TIME;
        final ListAlertOptions options = new ListAlertOptions()
            .setAlertQueryTimeMode(timeMode)
            .setMaxPageSize(10);
        final String alertConfigurationId = ALERT_CONFIG_ID;
    }

    protected static class ListAlertsOutput {
        static final ListAlertsOutput INSTANCE = new ListAlertsOutput();
        final int expectedAlerts = 4;
    }

    protected void assertAlertOutput(AnomalyAlert anomalyAlert) {
        Assertions.assertNotNull(anomalyAlert);
        Assertions.assertNotNull(anomalyAlert.getId());
        Assertions.assertNotNull(anomalyAlert.getCreatedTime());
        Assertions.assertNotNull(anomalyAlert.getModifiedTime());
        Assertions.assertNotNull(anomalyAlert.getTimestamp());
        boolean isInRange = (anomalyAlert.getTimestamp().isEqual(ListAlertsInput.INSTANCE.startTime)
            || anomalyAlert.getTimestamp().isAfter(ListAlertsInput.INSTANCE.startTime))
            && (anomalyAlert.getTimestamp().isEqual(ListAlertsInput.INSTANCE.endTime)
            || anomalyAlert.getTimestamp().isBefore(ListAlertsInput.INSTANCE.endTime));
        Assertions.assertTrue(isInRange);
    }
}
