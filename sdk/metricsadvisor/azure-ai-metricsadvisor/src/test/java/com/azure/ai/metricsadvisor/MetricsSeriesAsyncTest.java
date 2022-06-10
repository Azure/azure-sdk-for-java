// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.EnrichmentStatus;
import com.azure.ai.metricsadvisor.models.ListMetricSeriesDefinitionOptions;
import com.azure.ai.metricsadvisor.models.MetricSeriesDefinition;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MetricsSeriesAsyncTest extends MetricsSeriesTestBase {

    private MetricsAdvisorAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    /**
     * Verifies all the dimension values returned for a metric.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listMetricDimensionValues(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        List<String> actualDimensionValues = new ArrayList<String>();
        StepVerifier.create(client.listMetricDimensionValues(METRIC_ID, DIMENSION_NAME))
            .thenConsumeWhile(actualDimensionValues::add)
            .verifyComplete();
        assertEquals(EXPECTED_DIMENSION_VALUES_COUNT, actualDimensionValues.size());
    }

    /**
     * Verifies the metric series data values returned for a metric.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listMetricSeriesData(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        StepVerifier.create(client.listMetricSeriesData(METRIC_ID,
            Collections.singletonList(new DimensionKey(SERIES_KEY_FILTER)),
                TIME_SERIES_START_TIME, TIME_SERIES_END_TIME))
            .assertNext(metricSeriesData -> {
                assertEquals(METRIC_ID, metricSeriesData.getMetricId());
                assertNotNull(metricSeriesData.getSeriesKey());
                assertEquals(SERIES_KEY_FILTER, metricSeriesData.getSeriesKey().asMap());
                assertNotNull(metricSeriesData.getTimestamps());
                assertNotNull(metricSeriesData.getMetricValues());
            })
            .verifyComplete();
    }

    /**
     * Verifies list of metric definitions returned for a metric.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listMetricSeriesDefinitions(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        StepVerifier.create(client.listMetricSeriesDefinitions(METRIC_ID, TIME_SERIES_START_TIME, null)
            .take(LISTING_SERIES_DEFINITIONS_LIMIT))
            .thenConsumeWhile(metricSeriesDefinition -> metricSeriesDefinition.getMetricId() != null
                && metricSeriesDefinition.getSeriesKey() != null)
            .verifyComplete();
    }

    /**
     * Verifies list of metric definitions returned for a metric using dimension filter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listMetricSeriesDefinitionsDimensionFilter(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        List<MetricSeriesDefinition> actualMetricSeriesDefinitions = new ArrayList<>();
        StepVerifier.create(client.listMetricSeriesDefinitions(METRIC_ID, TIME_SERIES_START_TIME,
            new ListMetricSeriesDefinitionOptions()
                .setDimensionCombinationToFilter(new HashMap<String, List<String>>() {{
                        put("Dim1", Collections.singletonList("JPN"));
                    }})))
            .thenConsumeWhile(actualMetricSeriesDefinitions::add)
            .verifyComplete();

        actualMetricSeriesDefinitions.forEach(metricSeriesDefinition -> {
            final String dimensionFilterValue = metricSeriesDefinition.getSeriesKey().asMap().get("Dim1");
            assertNotNull(dimensionFilterValue);
            assertEquals("JPN", dimensionFilterValue);
        });
    }

    /**
     * Verifies list of enrichment status returned for a metric.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listMetricEnrichmentStatus(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        List<EnrichmentStatus> enrichmentStatuses = new ArrayList<>();
        StepVerifier.create(
            client.listMetricEnrichmentStatus(ListEnrichmentStatusInput.INSTANCE.metricId,
                OffsetDateTime.parse("2021-10-01T00:00:00Z"), OffsetDateTime.parse("2021-10-30T00:00:00Z"),
                ListEnrichmentStatusInput.INSTANCE.options))
            .thenConsumeWhile(enrichmentStatuses::add)
            .verifyComplete();

        enrichmentStatuses.forEach(MetricsSeriesTestBase::validateEnrichmentStatus);
    }
}
