// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.ListAnomalyDimensionValuesOptions;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static com.azure.ai.metricsadvisor.AnomalyAlertTestBase.DETECTION_CONFIGURATION_ID;
import static com.azure.ai.metricsadvisor.MetricsSeriesTestBase.DIMENSION_NAME;
import static com.azure.ai.metricsadvisor.MetricsSeriesTestBase.TIME_SERIES_END_TIME;
import static com.azure.ai.metricsadvisor.MetricsSeriesTestBase.TIME_SERIES_START_TIME;

public abstract class AnomalyDimensionValuesTestBase extends MetricsAdvisorClientTestBase {

    @Test
    public abstract void listAnomalyDimensionValues(HttpClient httpClient,
                                                    MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class ListAnomalyDimensionValuesInput {
        static final ListAnomalyDimensionValuesInput INSTANCE = new ListAnomalyDimensionValuesInput();
        final OffsetDateTime startTime = TIME_SERIES_START_TIME;
        final OffsetDateTime endTime = TIME_SERIES_END_TIME;
        final ListAnomalyDimensionValuesOptions options
            = new ListAnomalyDimensionValuesOptions()
            .setMaxPageSize(10);
        final String detectionConfigurationId = DETECTION_CONFIGURATION_ID;
        final String dimensionName = DIMENSION_NAME;
    }

    protected static class ListAnomalyDimensionValuesOutput {
        static final ListAnomalyDimensionValuesOutput INSTANCE = new ListAnomalyDimensionValuesOutput();
        final int expectedValues = 21;
    }

    protected void assertListAnomalyDimensionValuesOutput(String value) {
        Assertions.assertNotNull(value);
    }
}
