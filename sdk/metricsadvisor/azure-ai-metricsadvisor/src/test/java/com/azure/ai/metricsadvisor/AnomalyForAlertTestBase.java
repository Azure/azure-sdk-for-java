// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.DataPointAnomaly;
import com.azure.ai.metricsadvisor.models.ListAnomaliesAlertedOptions;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;

import static com.azure.ai.metricsadvisor.AlertTestBase.ALERT_CONFIG_ID;

public abstract class AnomalyForAlertTestBase extends MetricsAdvisorClientTestBase {
    public abstract void listAnomaliesForAlert(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class ListAnomaliesForAlertInput {
        static final ListAnomaliesForAlertInput INSTANCE = new ListAnomaliesForAlertInput();
        final ListAnomaliesAlertedOptions options = new ListAnomaliesAlertedOptions()
            .setMaxPageSize(10);
        final String alertConfigurationId = ALERT_CONFIG_ID;
        final String alertId = "175434e3400";
    }

    protected static class ListAnomaliesForAlertOutput {
        static final ListAnomaliesForAlertOutput INSTANCE = new ListAnomaliesForAlertOutput();
        final int expectedAnomalies = 2;
    }

    protected void assertListAnomaliesForAlertOutput(DataPointAnomaly dataPointAnomaly) {
        Assertions.assertNotNull(dataPointAnomaly);
        Assertions.assertNotNull(dataPointAnomaly.getMetricId());
        Assertions.assertNotNull(dataPointAnomaly.getSeverity());
        Assertions.assertNotNull(dataPointAnomaly.getStatus());
        Assertions.assertNotNull(dataPointAnomaly.getCreatedTime());
        Assertions.assertNotNull(dataPointAnomaly.getModifiedTime());
        Assertions.assertNotNull(dataPointAnomaly.getTimestamp());
        Assertions.assertNotNull(dataPointAnomaly.getDetectionConfigurationId());
        Assertions.assertNotNull(dataPointAnomaly.getSeriesKey());
        Assertions.assertFalse(dataPointAnomaly.getSeriesKey().asMap().isEmpty());
    }
}
