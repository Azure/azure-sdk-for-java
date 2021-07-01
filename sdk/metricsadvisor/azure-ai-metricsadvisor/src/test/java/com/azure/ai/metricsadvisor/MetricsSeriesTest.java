// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.EnrichmentStatus;
import com.azure.ai.metricsadvisor.models.ListMetricDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.ListMetricSeriesDefinitionOptions;
import com.azure.ai.metricsadvisor.models.MetricSeriesDefinition;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MetricsSeriesTest extends MetricsSeriesTestBase {
    private MetricsAdvisorClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    /**
     * Verifies the dimension values returned for a metric with skip and top parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listMetricDimensionValuesWithSkipTop(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion) {
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();
        List<String> actualDimensionValues = client.listMetricDimensionValues(METRIC_ID, DIMENSION_NAME,
            new ListMetricDimensionValuesOptions().setMaxPageSize(20).setSkip(20), Context.NONE)
            .stream().sorted().collect(Collectors.toList());

        Assertions.assertIterableEquals(EXPECTED_DIMENSION_VALUES, actualDimensionValues);
    }

    /**
     * Verifies all the dimension values returned for a metric.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listMetricDimensionValues(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();
        List<String> actualDimensionValues = client.listMetricDimensionValues(METRIC_ID, DIMENSION_NAME)
            .stream().collect(Collectors.toList());
        assertEquals(EXPECTED_DIMENSION_VALUES_COUNT, actualDimensionValues.size());
    }

    /**
     * Verifies the metric series data values returned for a metric.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listMetricSeriesData(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();
        client.listMetricSeriesData(METRIC_ID, Collections.singletonList(new DimensionKey(SERIES_KEY_FILTER)),
            TIME_SERIES_START_TIME, TIME_SERIES_END_TIME)
            .forEach(metricSeriesData -> {
                assertEquals(METRIC_ID, metricSeriesData.getMetricId());
                assertEquals(SERIES_KEY_FILTER, metricSeriesData.getSeriesKey().asMap());
                assertNotNull(metricSeriesData.getTimestamps());
                assertNotNull(metricSeriesData.getMetricValues());
            });
    }

    /**
     * Verifies list of metric definitions returned for a metric.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listMetricSeriesDefinitions(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();
        client.listMetricSeriesDefinitions(METRIC_ID,
                TIME_SERIES_START_TIME)
            .stream()
            .limit(LISTING_SERIES_DEFINITIONS_LIMIT)
            .forEach(metricSeriesDefinition -> {
                assertNotNull(metricSeriesDefinition.getMetricId());
                assertNotNull(metricSeriesDefinition.getSeriesKey());
            });
    }

    /**
     * Verifies list of metric definitions returned for a metric using dimension filter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listMetricSeriesDefinitionsDimensionFilter(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion) {
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();
        List<MetricSeriesDefinition> actualMetricSeriesDefinitions
            = client.listMetricSeriesDefinitions(METRIC_ID, TIME_SERIES_START_TIME,
                new ListMetricSeriesDefinitionOptions()
                .setDimensionCombinationToFilter(new HashMap<String, List<String>>() {{
                        put("city", Collections.singletonList("Miami"));
                    }}), Context.NONE)
            .stream().collect(Collectors.toList());

        actualMetricSeriesDefinitions.forEach(metricSeriesDefinition -> {
            final String dimensionFilterValue = metricSeriesDefinition.getSeriesKey().asMap().get("city");
            assertNotNull(dimensionFilterValue);
            assertEquals("Miami", dimensionFilterValue);
        });
    }

    /**
     * Verifies list of enrichment status returned for a metric.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listMetricEnrichmentStatus(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();
        List<EnrichmentStatus> enrichmentStatuses =
            client.listMetricEnrichmentStatus(ListEnrichmentStatusInput.INSTANCE.metricId,
                OffsetDateTime.parse("2020-10-01T00:00:00Z"), OffsetDateTime.parse("2020-10-30T00:00:00Z"))
                .stream()
                .collect(Collectors.toList());

        assertEquals(ListEnrichmentStatusOutput.INSTANCE.expectedStatuses, enrichmentStatuses.size());
        enrichmentStatuses.forEach(MetricsSeriesTestBase::validateEnrichmentStatus);
    }
}
