// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
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

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class ValuesOfDimensionWithAnomaliesAsyncTest extends ValuesOfDimensionWithAnomaliesTestBase {
    @BeforeAll
    static void beforeAll() {
        TestBase.setupClass();
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listValuesOfDimensionWithAnomalies(HttpClient httpClient,
                                                   MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAsyncClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();

        PagedFlux<String> dimensionValuesFlux = client.listDimensionValuesWithAnomalies(
            ListValuesOfDimensionWithAnomaliesInput.INSTANCE.detectionConfigurationId,
            ListValuesOfDimensionWithAnomaliesInput.INSTANCE.dimensionName,
            ListValuesOfDimensionWithAnomaliesInput.INSTANCE.startTime,
            ListValuesOfDimensionWithAnomaliesInput.INSTANCE.endTime,
            ListValuesOfDimensionWithAnomaliesInput.INSTANCE.options);

        Assertions.assertNotNull(dimensionValuesFlux);

        StepVerifier.create(dimensionValuesFlux)
            .assertNext(value -> assertListValuesOfDimensionWithAnomaliesOutput(value))
            .expectNextCount(ListValuesOfDimensionWithAnomaliesOutput.INSTANCE.expectedValues - 1)
            .verifyComplete();
    }
}
