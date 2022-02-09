// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class AnomalyDimensionValuesTest extends AnomalyDimensionValuesTestBase {
    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void listAnomalyDimensionValues(HttpClient httpClient,
                                           MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();

        PagedIterable<String> dimensionValuesIterable = client.listAnomalyDimensionValues(
            ListAnomalyDimensionValuesInput.INSTANCE.detectionConfigurationId,
            ListAnomalyDimensionValuesInput.INSTANCE.dimensionName,
            ListAnomalyDimensionValuesInput.INSTANCE.startTime,
            ListAnomalyDimensionValuesInput.INSTANCE.endTime,
            ListAnomalyDimensionValuesInput.INSTANCE.options, Context.NONE);

        int[] cnt = new int[1];
        dimensionValuesIterable.forEach(value -> {
            cnt[0]++;
            assertListAnomalyDimensionValuesOutput(value);
        });
        Assertions.assertEquals(ListAnomalyDimensionValuesOutput.INSTANCE.expectedValues, cnt[0]);
    }
}
