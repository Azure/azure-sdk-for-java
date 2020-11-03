// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.Incident;
import com.azure.ai.metricsadvisor.models.ListIncidentsDetectedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorServiceVersion;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static com.azure.ai.metricsadvisor.AnomalyAlertTestBase.DETECTION_CONFIGURATION_ID;

public abstract class IncidentDetectedTestBase extends MetricsAdvisorClientTestBase {
    @Test
    public abstract void listIncidentsDetected(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    // Pre-configured test resource.
    protected static class ListIncidentsDetectedInput {
        static final ListIncidentsDetectedInput INSTANCE = new ListIncidentsDetectedInput();
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-10-20T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-10-21T00:00:00Z");
        final ListIncidentsDetectedOptions options = new ListIncidentsDetectedOptions(startTime, endTime)
            .setTop(1000);
        final String detectionConfigurationId = DETECTION_CONFIGURATION_ID;
    }

    protected static class ListIncidentsDetectedOutput {
        static final ListIncidentsDetectedOutput INSTANCE = new ListIncidentsDetectedOutput();
        final int expectedIncidents = 27;
    }

    protected void assertListIncidentsDetectedOutput(Incident incident) {
        Assertions.assertNotNull(incident);
        Assertions.assertNotNull(incident.getId());
        // Note: Service will not return metricId & detectionId when listing incidents by detection
        // config but only for alert.
        Assertions.assertNotNull(incident.getSeverity());
        Assertions.assertNotNull(incident.getStatus());
        Assertions.assertNotNull(incident.getLastTime());
        Assertions.assertNotNull(incident.getRootDimensionKey());
        Assertions.assertFalse(incident.getRootDimensionKey().asMap().isEmpty());
        OffsetDateTime startTime = incident.getStartTime();
        Assertions.assertNotNull(startTime);
        boolean isInRange = (startTime.isEqual(ListIncidentsDetectedInput.INSTANCE.startTime)
            || startTime.isAfter(ListIncidentsDetectedInput.INSTANCE.startTime))
            && (startTime.isEqual(ListIncidentsDetectedInput.INSTANCE.endTime)
            || startTime.isBefore(ListIncidentsDetectedInput.INSTANCE.endTime));
        Assertions.assertTrue(isInRange);
    }
}
