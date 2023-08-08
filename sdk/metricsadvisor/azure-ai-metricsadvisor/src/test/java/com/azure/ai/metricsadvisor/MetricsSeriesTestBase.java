// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.EnrichmentStatus;
import com.azure.ai.metricsadvisor.models.ListMetricEnrichmentStatusOptions;
import com.azure.core.util.Configuration;

import java.time.OffsetDateTime;
import java.util.HashMap;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class MetricsSeriesTestBase extends MetricsAdvisorClientTestBase {

    static final String METRIC_ID = "b6c0649c-0c51-4aa6-82b6-3c3b0aa55066";
    static final String DIMENSION_NAME = "Dim1";
    static final int LISTING_SERIES_DEFINITIONS_LIMIT = 50;

    static final int EXPECTED_DIMENSION_VALUES_COUNT = 3;
    static final OffsetDateTime TIME_SERIES_START_TIME = OffsetDateTime.parse("2022-01-01T00:00:00Z");
    static final OffsetDateTime TIME_SERIES_END_TIME = OffsetDateTime.parse("2022-03-22T00:00:00Z");
    static final HashMap<String, String> SERIES_KEY_FILTER = new HashMap<String, String>() {{
            put("Dim1", "JPN");
            put("Dim2", "JP");
        }};

    // Pre-configured test resource.
    protected static class ListEnrichmentStatusInput {
        static final ListEnrichmentStatusInput INSTANCE = new ListEnrichmentStatusInput();
        final ListMetricEnrichmentStatusOptions options =
            new ListMetricEnrichmentStatusOptions();

        final String metricId = METRIC_ID;
    }

    static void validateEnrichmentStatus(EnrichmentStatus actualEnrichmentStatus) {
        assertNotNull(actualEnrichmentStatus.getStatus());
        assertNotNull(actualEnrichmentStatus.getMessage());
        assertNotNull(actualEnrichmentStatus.getTimestamp());
    }

    @Override
    protected void beforeTest() {
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_METRICS_ADVISOR_ENDPOINT);
    }
}
