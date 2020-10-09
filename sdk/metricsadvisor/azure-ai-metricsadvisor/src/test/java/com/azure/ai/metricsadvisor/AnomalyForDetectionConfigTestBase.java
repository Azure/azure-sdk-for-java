// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.Anomaly;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedFilter;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.ai.metricsadvisor.models.Severity;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;

import java.time.OffsetDateTime;

public abstract class AnomalyForDetectionConfigTestBase extends MetricsAdvisorClientTestBase {
    public abstract void listAnomaliesForDetectionConfig(HttpClient httpClient,
                                                         MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class ListAnomaliesForDetectionConfigInput {
        static final ListAnomaliesForDetectionConfigInput INSTANCE = new ListAnomaliesForDetectionConfigInput();
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter()
            .setSeverity(Severity.LOW, Severity.MEDIUM);
        final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions(startTime, endTime)
            .setTop(10)
            .setFilter(filter);
    }

    protected void assertListAnomaliesDetectionConfigOutput(Anomaly anomaly) {
        Assertions.assertNotNull(anomaly);
        Assertions.assertNotNull(anomaly.getSeverity());
        Assertions.assertNotNull(anomaly.getTimestamp());
        Assertions.assertNotNull(anomaly.getSeriesKey());
        Assertions.assertFalse(anomaly.getSeriesKey().asMap().isEmpty());
    }
}
