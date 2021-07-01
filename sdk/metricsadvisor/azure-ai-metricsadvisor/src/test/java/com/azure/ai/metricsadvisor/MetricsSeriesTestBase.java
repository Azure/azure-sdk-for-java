// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.EnrichmentStatus;
import com.azure.ai.metricsadvisor.models.ListMetricEnrichmentStatusOptions;
import com.azure.core.util.Configuration;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class MetricsSeriesTestBase extends MetricsAdvisorClientTestBase {

    static final String METRIC_ID = "27e3015f-04fd-44ba-a20b-bc529a0aebae";
    static final String DIMENSION_NAME = "category";
    static final Iterable<?> EXPECTED_DIMENSION_VALUES = Arrays.asList("Music",
        "Musical Instruments",
        "Office Products",
        "Outdoors",
        "Personal Computers",
        "Shoes Handbags & Sunglasses",
        "Software & Computer Games",
        "Sports",
        "Sports Collectibles",
        "__SUM__",
        "Tools & Home Improvement").stream().sorted().collect(Collectors.toList());
    static final int LISTING_SERIES_DEFINITIONS_LIMIT = 50;

    static final int EXPECTED_DIMENSION_VALUES_COUNT = 31;
    static final OffsetDateTime TIME_SERIES_START_TIME = OffsetDateTime.parse("2020-01-01T00:00:00Z");
    static final OffsetDateTime TIME_SERIES_END_TIME = OffsetDateTime.parse("2020-10-22T00:00:00Z");
    static final HashMap<String, String> SERIES_KEY_FILTER = new HashMap<String, String>() {{
            put("city", "Miami");
            put("category", "Health & Personal Care");
        }};

    // Pre-configured test resource.
    protected static class ListEnrichmentStatusInput {
        static final ListEnrichmentStatusInput INSTANCE = new ListEnrichmentStatusInput();
        final ListMetricEnrichmentStatusOptions options =
            new ListMetricEnrichmentStatusOptions();

        final String metricId = METRIC_ID;
    }

    protected static class ListEnrichmentStatusOutput {
        static final ListEnrichmentStatusOutput INSTANCE = new ListEnrichmentStatusOutput();
        final int expectedStatuses = 29;
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
