// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.Anomaly;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class AnomalyForDetectionConfigTest extends AnomalyForDetectionConfigTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void listAnomaliesForDetectionConfig(HttpClient httpClient,
                                                MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();

        PagedIterable<Anomaly> anomaliesIterable
            = client.listAnomaliesForDetectionConfiguration(
            ListAnomaliesForDetectionConfigInput.INSTANCE.detectionConfigurationId,
            ListAnomaliesForDetectionConfigInput.INSTANCE.options);

        for (Anomaly anomaly : anomaliesIterable) {
            assertListAnomaliesDetectionConfigOutput(anomaly);
        }
    }
}
