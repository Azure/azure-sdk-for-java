// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricEnrichedSeriesData;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class MetricEnrichedSeriesDataTest extends MetricEnrichedSeriesDataTestBase {

    @BeforeAll
    static void beforeAll() {
        TestBase.setupClass();
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void getEnrichedSeriesData(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();

        PagedIterable<MetricEnrichedSeriesData> enrichedDataIterable
            = client.listMetricEnrichedSeriesData(
            GetEnrichedSeriesDataInput.INSTANCE.detectionConfigurationId,
            GetEnrichedSeriesDataInput.INSTANCE.getSeriesKeys(),
            GetEnrichedSeriesDataInput.INSTANCE.startTime,
            GetEnrichedSeriesDataInput.INSTANCE.endTime);

        Assertions.assertNotNull(enrichedDataIterable);

        List<MetricEnrichedSeriesData> enrichedDataList = enrichedDataIterable
            .stream()
            .collect(Collectors.toList());

        // We asked for one series so there should be only one set of data.
        Assertions.assertEquals(1, enrichedDataList.size());
        MetricEnrichedSeriesData enrichedSeriesData = enrichedDataList.get(0);
        Assertions.assertNotNull(enrichedSeriesData);
        assertGetEnrichedSeriesDataOutput(enrichedSeriesData);
    }
}
