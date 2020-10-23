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
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-10-10T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-10-21T00:00:00Z");
        final TimeMode timeMode = TimeMode.ANOMALY_TIME;
        final ListAlertOptions options = new ListAlertOptions(startTime, endTime, timeMode)
            .setTop(10);
        final String alertConfigurationId = "204a211a-c5f4-45f3-a30e-512fb25d1d2c";
    }

    protected static class ListAlertsOutput {
        static final ListAlertsOutput INSTANCE = new ListAlertsOutput();
        final int expectedAlerts = 4;
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
