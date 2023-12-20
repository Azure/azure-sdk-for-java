// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class AnomalyDimensionValuesAsyncTest extends AnomalyDimensionValuesTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listAnomalyDimensionValues(HttpClient httpClient,
                                           MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAsyncClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion, false).buildAsyncClient();

        PagedFlux<String> dimensionValuesFlux = client.listAnomalyDimensionValues(
            ListAnomalyDimensionValuesInput.INSTANCE.detectionConfigurationId,
            ListAnomalyDimensionValuesInput.INSTANCE.dimensionName,
            ListAnomalyDimensionValuesInput.INSTANCE.startTime,
            ListAnomalyDimensionValuesInput.INSTANCE.endTime,
            ListAnomalyDimensionValuesInput.INSTANCE.options);

        Assertions.assertNotNull(dimensionValuesFlux);

        List<String> dimensions = new ArrayList<>();
        StepVerifier.create(dimensionValuesFlux)
            .thenConsumeWhile(dimensions::add)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        Assertions.assertEquals(ListAnomalyDimensionValuesOutput.INSTANCE.expectedValues, dimensions.size());
    }
}
