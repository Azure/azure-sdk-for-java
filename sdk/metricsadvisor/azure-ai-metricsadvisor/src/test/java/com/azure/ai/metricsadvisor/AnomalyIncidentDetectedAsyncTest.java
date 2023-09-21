// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class AnomalyIncidentDetectedAsyncTest extends IncidentDetectedTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void listIncidentsDetected(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAsyncClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion, false).buildAsyncClient();

        PagedFlux<AnomalyIncident> incidentsFlux
            = client.listIncidentsForDetectionConfig(
            ListIncidentsDetectedInput.INSTANCE.detectionConfigurationId,
            ListIncidentsDetectedInput.INSTANCE.startTime, ListIncidentsDetectedInput.INSTANCE.endTime,
            ListIncidentsDetectedInput.INSTANCE.options);

        Assertions.assertNotNull(incidentsFlux);

        incidentsFlux.toIterable().forEach(this::assertListIncidentsDetectedOutput);
    }
}
