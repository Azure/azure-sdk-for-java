// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.EnrichmentStatus;
import com.azure.ai.metricsadvisor.models.ListMetricEnrichmentStatusOptions;
import com.azure.core.util.Configuration;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class MetricsSeriesTestBase extends MetricsAdvisorClientTestBase {

    static final String METRIC_ID = "3d48ed3e-6e6e-4391-b78f-b00dfee1e6f5";
    static final String DIMENSION_NAME = "Dim1";
    static final Iterable<?> EXPECTED_DIMENSION_VALUES = Arrays.asList("Common Alder",
        "Common Ash",
        "Common Beech",
        "Common Hazel",
        "Common Juniper",
        "Common Lime",
        "Common Walnut",
        "Common Yew",
        "Copper Beech");

    static final int EXPECTED_DIMENSION_VALUES_COUNT = 29;
    static final OffsetDateTime TIME_SERIES_START_TIME = OffsetDateTime.parse("2020-01-01T00:00:00Z");
    static final OffsetDateTime TIME_SERIES_END_TIME = OffsetDateTime.parse("2020-09-09T00:00:00Z");
    static final HashMap<String, String> SERIES_KEY_FILTER = new HashMap<String, String>() {{
            put("Dim1", "Common Lime");
            put("Dim2", "Amphibian");
        }};

    // Pre-configured test resource.
    protected static class ListEnrichmentStatusInput {
        static final ListEnrichmentStatusInput INSTANCE = new ListEnrichmentStatusInput();
        final ListMetricEnrichmentStatusOptions options =
            new ListMetricEnrichmentStatusOptions(TIME_SERIES_START_TIME, TIME_SERIES_END_TIME);

        final String metricId = METRIC_ID;
    }

    protected static class ListEnrichmentStatusOutput {
        static final ListEnrichmentStatusOutput INSTANCE = new ListEnrichmentStatusOutput();
        final int expectedStatuses = 3;
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
