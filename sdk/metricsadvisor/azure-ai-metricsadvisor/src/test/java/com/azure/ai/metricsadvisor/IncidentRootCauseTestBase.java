// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.implementation.models.DimensionGroupIdentity;
import com.azure.ai.metricsadvisor.implementation.models.RootCause;
import com.azure.ai.metricsadvisor.implementation.util.IncidentRootCauseTransforms;
import com.azure.ai.metricsadvisor.models.IncidentRootCause;
import com.azure.core.util.Configuration;

import java.util.Arrays;
import java.util.HashMap;

import static com.azure.ai.metricsadvisor.AnomalyAlertTestBase.DETECTION_CONFIGURATION_ID;
import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class IncidentRootCauseTestBase extends MetricsAdvisorClientTestBase {

    static final String INCIDENT_ROOT_CAUSE_ID = "88ecf25a0e6bd330ef9e7b49b7c5b92b-18001521c00";
    static final String INCIDENT_ROOT_CAUSE_CONFIGURATION_ID = DETECTION_CONFIGURATION_ID;

    @Override
    protected void beforeTest() {
    }

    static IncidentRootCause getExpectedIncidentRootCause() {
        RootCause innerRootCause = new RootCause()
            .setRootCause(new DimensionGroupIdentity().setDimension(new HashMap<String, String>() {
                {
                    put("Dim1", "JPN");
                    put("Dim2", "JP");
                }
            }))
            .setPath(Arrays.asList("Dim1", "Dim2"))
            .setDescription("Increase on Dim1 = JPN | Dim2 = JP contributes the most to current incident.");
        return IncidentRootCauseTransforms.fromInner(innerRootCause);
    }

    void validateIncidentRootCauses(IncidentRootCause expectedIncidentRootCause,
        IncidentRootCause actualIncidentRootCause) {
        assertEquals(expectedIncidentRootCause.getSeriesKey().asMap(), actualIncidentRootCause.getSeriesKey().asMap());
        assertEquals(expectedIncidentRootCause.getDescription(), actualIncidentRootCause.getDescription());
        assertEquals(expectedIncidentRootCause.getPaths(), actualIncidentRootCause.getPaths());
        assertNotNull(actualIncidentRootCause.getContributionScore());
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_METRICS_ADVISOR_ENDPOINT);
    }
}
