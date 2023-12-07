// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class AnomalyIncidentDetectedTest extends IncidentDetectedTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listIncidentsDetected(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion, true).buildClient();

        PagedIterable<AnomalyIncident> incidentsIterable
            = client.listIncidentsForDetectionConfig(
                ListIncidentsDetectedInput.INSTANCE.detectionConfigurationId,
            ListIncidentsDetectedInput.INSTANCE.startTime, ListIncidentsDetectedInput.INSTANCE.endTime,
            ListIncidentsDetectedInput.INSTANCE.options, Context.NONE);

        for (AnomalyIncident anomalyIncident : incidentsIterable) {
            assertListIncidentsDetectedOutput(anomalyIncident);
        }
    }
}
