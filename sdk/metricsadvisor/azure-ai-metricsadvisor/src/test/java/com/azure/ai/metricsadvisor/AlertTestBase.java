// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AlertQueryTimeMode;
import com.azure.ai.metricsadvisor.models.AnomalyAlert;
import com.azure.ai.metricsadvisor.models.ListAlertOptions;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

public abstract class AlertTestBase extends MetricsAdvisorClientTestBase {

    public static final String ALERT_CONFIG_ID = "126d1470-b500-4ef0-b5c0-47f9ca914a75";

    @Test
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/33586")
    public abstract void listAlerts(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class ListAlertsInput {
        static final ListAlertsInput INSTANCE = new ListAlertsInput();
        final OffsetDateTime startTime = OffsetDateTime.parse("2022-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2022-03-22T00:00:00Z");
        final AlertQueryTimeMode timeMode = AlertQueryTimeMode.ANOMALY_TIME;
        final ListAlertOptions options = new ListAlertOptions()
            .setAlertQueryTimeMode(timeMode)
            .setMaxPageSize(10);
        final String alertConfigurationId = ALERT_CONFIG_ID;
    }

    protected static class ListAlertsOutput {
        static final ListAlertsOutput INSTANCE = new ListAlertsOutput();
        final int expectedAlerts = 3;
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
