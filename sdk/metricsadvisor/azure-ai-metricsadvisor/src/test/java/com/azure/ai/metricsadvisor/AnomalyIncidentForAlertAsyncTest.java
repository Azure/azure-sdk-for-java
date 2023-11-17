// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class AnomalyIncidentForAlertAsyncTest extends IncidentForAlertTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Disabled
    public void listIncidentsForAlert(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAsyncClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion, false).buildAsyncClient();

        PagedFlux<AnomalyIncident> incidentsFlux
            = client.listIncidentsForAlert(
            ListIncidentsForAlertInput.INSTANCE.alertConfigurationId,
            ListIncidentsForAlertInput.INSTANCE.alertId,
            ListIncidentsForAlertInput.INSTANCE.options);

        Assertions.assertNotNull(incidentsFlux);

        StepVerifier.create(incidentsFlux)
            .assertNext(this::assertListIncidentsForAlertOutput)
            .expectNextCount(ListIncidentsForAlertOutput.INSTANCE.expectedIncidents - 1)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }
}
