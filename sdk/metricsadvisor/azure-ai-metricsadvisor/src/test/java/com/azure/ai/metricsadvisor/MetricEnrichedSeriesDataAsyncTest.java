// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricEnrichedSeriesData;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class MetricEnrichedSeriesDataAsyncTest extends MetricEnrichedSeriesDataTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Disabled
    public void getEnrichedSeriesData(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAsyncClient client
            = getMetricsAdvisorBuilder(httpClient, serviceVersion, false).buildAsyncClient();

        PagedFlux<MetricEnrichedSeriesData> enrichedDataFlux
            = client.listMetricEnrichedSeriesData(GetEnrichedSeriesDataInput.INSTANCE.detectionConfigurationId,
                GetEnrichedSeriesDataInput.INSTANCE.getSeriesKeys(), GetEnrichedSeriesDataInput.INSTANCE.startTime,
                GetEnrichedSeriesDataInput.INSTANCE.endTime);

        Assertions.assertNotNull(enrichedDataFlux);

        StepVerifier.create(enrichedDataFlux)
            .assertNext(this::assertGetEnrichedSeriesDataOutput)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }
}
