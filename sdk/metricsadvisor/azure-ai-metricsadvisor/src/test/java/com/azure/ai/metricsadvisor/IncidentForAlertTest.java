// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.Incident;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class IncidentForAlertTest extends IncidentForAlertTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    public void listIncidentsForAlert(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildClient();

        PagedIterable<Incident> incidentsIterable
            = client.listIncidentsForAlert(
            ListIncidentsForAlertInput.INSTANCE.alertConfigurationId,
            ListIncidentsForAlertInput.INSTANCE.alertId,
            ListIncidentsForAlertInput.INSTANCE.options);

        int[] cnt = new int[1];
        for (Incident incident : incidentsIterable) {
            cnt[0]++;
            assertListIncidentsForAlertOutput(incident);
        }
        Assertions.assertEquals(ListIncidentsForAlertOutput.INSTANCE.expectedIncidents, cnt[0]);
    }
}
