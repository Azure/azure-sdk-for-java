// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.Alert;
import com.azure.ai.metricsadvisor.models.ListAlertOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.ai.metricsadvisor.models.TimeMode;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;

import java.time.OffsetDateTime;

public abstract class AlertTestBase extends MetricsAdvisorClientTestBase {
    public abstract void listAlerts(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class ListAlertsInput {
        static final ListAlertsInput INSTANCE = new ListAlertsInput();
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final TimeMode timeMode = TimeMode.ANOMALY_TIME;
        final ListAlertOptions options = new ListAlertOptions(startTime, endTime, timeMode)
            .setTop(10);
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
    }

    protected static class ListAlertsOutput {
        static final ListAlertsOutput INSTANCE = new ListAlertsOutput();
        final int expectedAlerts = 3;
    }

    protected void assertAlertOutput(Alert alert) {
        Assertions.assertNotNull(alert);
        Assertions.assertNotNull(alert.getId());
        Assertions.assertNotNull(alert.getCreatedTime());
        Assertions.assertNotNull(alert.getModifiedTime());
        Assertions.assertNotNull(alert.getTimestamp());
        boolean isInRange = (alert.getTimestamp().isEqual(ListAlertsInput.INSTANCE.startTime)
            || alert.getTimestamp().isAfter(ListAlertsInput.INSTANCE.startTime))
            && (alert.getTimestamp().isEqual(ListAlertsInput.INSTANCE.endTime)
            || alert.getTimestamp().isBefore(ListAlertsInput.INSTANCE.endTime));
        Assertions.assertTrue(isInRange);
    }
}
