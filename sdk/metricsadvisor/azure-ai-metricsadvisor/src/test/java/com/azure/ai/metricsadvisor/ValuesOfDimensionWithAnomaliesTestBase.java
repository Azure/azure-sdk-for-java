// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.ListValuesOfDimensionWithAnomaliesOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;

import java.time.OffsetDateTime;

public abstract class ValuesOfDimensionWithAnomaliesTestBase extends MetricsAdvisorClientTestBase {
    public abstract void listValuesOfDimensionWithAnomalies(HttpClient httpClient,
                                                            MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class ListValuesOfDimensionWithAnomaliesInput {
        static final ListValuesOfDimensionWithAnomaliesInput INSTANCE = new ListValuesOfDimensionWithAnomaliesInput();
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListValuesOfDimensionWithAnomaliesOptions options
            = new ListValuesOfDimensionWithAnomaliesOptions(startTime, endTime)
            .setTop(10);
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String dimensionName = "Dim1";
    }

    protected static class ListValuesOfDimensionWithAnomaliesOutput {
        static final ListValuesOfDimensionWithAnomaliesOutput INSTANCE = new ListValuesOfDimensionWithAnomaliesOutput();
        final int expectedValues = 26;
    }

    protected void assertListValuesOfDimensionWithAnomaliesOutput(String value) {
        Assertions.assertNotNull(value);
    }
}
