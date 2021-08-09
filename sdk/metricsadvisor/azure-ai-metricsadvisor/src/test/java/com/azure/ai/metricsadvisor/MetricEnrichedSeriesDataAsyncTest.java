// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricEnrichedSeriesData;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.test.TestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class MetricEnrichedSeriesDataAsyncTest extends MetricEnrichedSeriesDataTestBase {

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
        MetricsAdvisorAsyncClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();

        PagedFlux<MetricEnrichedSeriesData> enrichedDataFlux
            = client.listMetricEnrichedSeriesData(
            GetEnrichedSeriesDataInput.INSTANCE.detectionConfigurationId,
            GetEnrichedSeriesDataInput.INSTANCE.getSeriesKeys(),
            GetEnrichedSeriesDataInput.INSTANCE.startTime,
            GetEnrichedSeriesDataInput.INSTANCE.endTime);

        Assertions.assertNotNull(enrichedDataFlux);

        StepVerifier.create(enrichedDataFlux)
            .assertNext(enrichedSeriesData -> {
                assertGetEnrichedSeriesDataOutput(enrichedSeriesData);
            })
            .verifyComplete();
    }
}
