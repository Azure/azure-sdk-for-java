// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.Incident;
import com.azure.ai.metricsadvisor.models.ListIncidentsAlertedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;

import java.time.OffsetDateTime;

public abstract class IncidentForAlertTestBase extends MetricsAdvisorClientTestBase {
    public abstract void listIncidentsForAlert(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class ListIncidentsForAlertInput {
        static final ListIncidentsForAlertInput INSTANCE = new ListIncidentsForAlertInput();
        final ListIncidentsAlertedOptions options = new ListIncidentsAlertedOptions()
            .setTop(10);
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
    }

    protected static class ListIncidentsForAlertOutput {
        static final ListIncidentsForAlertOutput INSTANCE = new ListIncidentsForAlertOutput();
        final int expectedIncidents = 3;
    }

    protected void assertListIncidentsForAlertOutput(Incident incident) {
        Assertions.assertNotNull(incident);
        Assertions.assertNotNull(incident.getId());
        Assertions.assertNotNull(incident.getMetricId());
        Assertions.assertNotNull(incident.getSeverity());
        Assertions.assertNotNull(incident.getStatus());
        OffsetDateTime startTime = incident.getStartTime();
        Assertions.assertNotNull(incident.getLastTime());
        Assertions.assertNotNull(incident.getDetectionConfigurationId());
        Assertions.assertNotNull(incident.getRootDimensionKey());
        Assertions.assertFalse(incident.getRootDimensionKey().asMap().isEmpty());
    }
}
