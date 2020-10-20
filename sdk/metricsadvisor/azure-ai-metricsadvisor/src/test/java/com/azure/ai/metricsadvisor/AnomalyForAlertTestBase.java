// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.Anomaly;
import com.azure.ai.metricsadvisor.models.ListAnomaliesAlertedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;

public abstract class AnomalyForAlertTestBase extends MetricsAdvisorClientTestBase {
    public abstract void listAnomaliesForAlert(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class ListAnomaliesForAlertInput {
        static final ListAnomaliesForAlertInput INSTANCE = new ListAnomaliesForAlertInput();
        final ListAnomaliesAlertedOptions options = new ListAnomaliesAlertedOptions()
            .setTop(10);
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
    }

    protected static class ListAnomaliesForAlertOutput {
        static final ListAnomaliesForAlertOutput INSTANCE = new ListAnomaliesForAlertOutput();
        final int expectedAnomalies = 3;
    }

    protected void assertListAnomaliesForAlertOutput(Anomaly anomaly) {
        Assertions.assertNotNull(anomaly);
        Assertions.assertNotNull(anomaly.getMetricId());
        Assertions.assertNotNull(anomaly.getSeverity());
        Assertions.assertNotNull(anomaly.getStatus());
        Assertions.assertNotNull(anomaly.getCreatedTime());
        Assertions.assertNotNull(anomaly.getModifiedTime());
        Assertions.assertNotNull(anomaly.getTimestamp());
        Assertions.assertNotNull(anomaly.getDetectionConfigurationId());
        Assertions.assertNotNull(anomaly.getSeriesKey());
        Assertions.assertFalse(anomaly.getSeriesKey().asMap().isEmpty());
    }
}
