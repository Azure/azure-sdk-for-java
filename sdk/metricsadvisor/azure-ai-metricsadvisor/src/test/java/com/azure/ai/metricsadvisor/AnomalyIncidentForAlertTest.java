// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class AnomalyIncidentForAlertTest extends IncidentForAlertTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Disabled
    public void listIncidentsForAlert(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion, true).buildClient();

        PagedIterable<AnomalyIncident> incidentsIterable
            = client.listIncidentsForAlert(
            ListIncidentsForAlertInput.INSTANCE.alertConfigurationId,
            ListIncidentsForAlertInput.INSTANCE.alertId,
            ListIncidentsForAlertInput.INSTANCE.options,
            Context.NONE);

        int[] cnt = new int[1];
        for (AnomalyIncident anomalyIncident : incidentsIterable) {
            cnt[0]++;
            assertListIncidentsForAlertOutput(anomalyIncident);
        }
        Assertions.assertEquals(ListIncidentsForAlertOutput.INSTANCE.expectedIncidents, cnt[0]);
    }
}
