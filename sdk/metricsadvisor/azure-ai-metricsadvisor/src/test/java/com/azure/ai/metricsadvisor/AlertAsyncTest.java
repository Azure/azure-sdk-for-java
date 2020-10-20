// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.Alert;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class AlertAsyncTest extends AlertTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void listAlerts(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAsyncClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();

        PagedFlux<Alert> alertsFlux
            = client.listAlerts(ListAlertsInput.INSTANCE.alertConfigurationId, ListAlertsInput.INSTANCE.options);

        Assertions.assertNotNull(alertsFlux);

        StepVerifier.create(alertsFlux)
            .assertNext(alert -> assertAlertOutput(alert))
            .expectNextCount(ListAlertsOutput.INSTANCE.expectedAlerts - 1)
            .verifyComplete();
    }
}
