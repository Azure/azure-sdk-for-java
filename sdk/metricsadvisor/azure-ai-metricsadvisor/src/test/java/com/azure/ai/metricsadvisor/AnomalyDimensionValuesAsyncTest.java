// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

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
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class AnomalyDimensionValuesAsyncTest extends AnomalyDimensionValuesTestBase {
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
    public void listAnomalyDimensionValues(HttpClient httpClient,
                                           MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAsyncClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();

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
            .verifyComplete();

        Assertions.assertEquals(ListAnomalyDimensionValuesOutput.INSTANCE.expectedValues, dimensions.size());
    }
}
