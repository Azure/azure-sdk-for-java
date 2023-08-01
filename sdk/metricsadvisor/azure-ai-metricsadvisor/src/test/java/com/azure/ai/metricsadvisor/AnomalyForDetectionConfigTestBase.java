// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.ai.metricsadvisor.models.DataPointAnomaly;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedFilter;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedOptions;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;

import java.time.OffsetDateTime;

import static com.azure.ai.metricsadvisor.AnomalyAlertTestBase.DETECTION_CONFIGURATION_ID;
import static com.azure.ai.metricsadvisor.MetricsSeriesTestBase.TIME_SERIES_END_TIME;
import static com.azure.ai.metricsadvisor.MetricsSeriesTestBase.TIME_SERIES_START_TIME;

public abstract class AnomalyForDetectionConfigTestBase extends MetricsAdvisorClientTestBase {
    public abstract void listAnomaliesForDetectionConfig(HttpClient httpClient,
                                                         MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class ListAnomaliesForDetectionConfigInput {
        static final ListAnomaliesForDetectionConfigInput INSTANCE = new ListAnomaliesForDetectionConfigInput();
        final String detectionConfigurationId = DETECTION_CONFIGURATION_ID;
        final OffsetDateTime startTime = TIME_SERIES_START_TIME;
        final OffsetDateTime endTime = TIME_SERIES_END_TIME;
        final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter()
            .setSeverityRange(AnomalySeverity.LOW, AnomalySeverity.MEDIUM);
        final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions()
            .setMaxPageSize(10)
            .setFilter(filter);
    }

    protected void assertListAnomaliesDetectionConfigOutput(DataPointAnomaly dataPointAnomaly) {
        Assertions.assertNotNull(dataPointAnomaly);
        Assertions.assertNotNull(dataPointAnomaly.getSeverity());
        Assertions.assertNotNull(dataPointAnomaly.getTimestamp());
        Assertions.assertNotNull(dataPointAnomaly.getSeriesKey());
        Assertions.assertFalse(dataPointAnomaly.getSeriesKey().asMap().isEmpty());
    }
}
