// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.Anomaly;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class AnomalyForAlertTest extends AnomalyForAlertTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void listAnomaliesForAlert(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();

        PagedIterable<Anomaly> anomaliesIterable
            = client.listAnomaliesForAlert(
            ListAnomaliesForAlertInput.INSTANCE.alertConfigurationId,
            ListAnomaliesForAlertInput.INSTANCE.alertId,
            ListAnomaliesForAlertInput.INSTANCE.options,
            Context.NONE
        );

        int[] cnt = new int[1];
        for (Anomaly anomaly : anomaliesIterable) {
            cnt[0]++;
            assertListAnomaliesForAlertOutput(anomaly);
        }
        Assertions.assertEquals(ListAnomaliesForAlertOutput.INSTANCE.expectedAnomalies, cnt[0]);
    }
}
