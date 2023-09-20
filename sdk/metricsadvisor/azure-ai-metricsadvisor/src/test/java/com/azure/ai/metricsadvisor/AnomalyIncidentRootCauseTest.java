// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.IncidentRootCause;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AnomalyIncidentRootCauseTest extends IncidentRootCauseTestBase {

    /**
     * Verifies the root causes for an incident.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void listIncidentRootCauses(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorClient client = getMetricsAdvisorBuilder(httpClient, serviceVersion, true).buildClient();
        List<IncidentRootCause> actualIncidentRootCauses = client.listIncidentRootCauses(
            INCIDENT_ROOT_CAUSE_CONFIGURATION_ID, INCIDENT_ROOT_CAUSE_ID)
            .stream()
            .collect(Collectors.toList());

        assertNotNull(actualIncidentRootCauses);
        assertEquals(1, actualIncidentRootCauses.size());
        validateIncidentRootCauses(getExpectedIncidentRootCause(), actualIncidentRootCauses.get(0));
    }
}
