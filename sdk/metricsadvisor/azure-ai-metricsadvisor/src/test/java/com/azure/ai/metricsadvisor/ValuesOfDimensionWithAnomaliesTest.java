// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class ValuesOfDimensionWithAnomaliesTest extends ValuesOfDimensionWithAnomaliesTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void listValuesOfDimensionWithAnomalies(HttpClient httpClient,
                                                   MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();

        PagedIterable<String> dimensionValuesIterable = client.listValuesOfDimensionWithAnomalies(
            ListValuesOfDimensionWithAnomaliesInput.INSTANCE.detectionConfigurationId,
            ListValuesOfDimensionWithAnomaliesInput.INSTANCE.dimensionName,
            ListValuesOfDimensionWithAnomaliesInput.INSTANCE.options);

        int[] cnt = new int[1];
        dimensionValuesIterable.forEach(value -> {
            cnt[0]++;
            assertListValuesOfDimensionWithAnomaliesOutput(value);
        });
        Assertions.assertEquals(ListValuesOfDimensionWithAnomaliesOutput.INSTANCE.expectedValues, cnt[0]);
    }
}
