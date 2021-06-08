// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.ai.metricsadvisor.models.ListIncidentsDetectedOptions;
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
        final ListIncidentsDetectedOptions options = new ListIncidentsDetectedOptions()
            .setMaxPageSize(1000);
        final String detectionConfigurationId = DETECTION_CONFIGURATION_ID;
    }

    protected static class ListIncidentsDetectedOutput {
        static final ListIncidentsDetectedOutput INSTANCE = new ListIncidentsDetectedOutput();
        final int expectedIncidents = 27;
    }

    protected void assertListIncidentsDetectedOutput(AnomalyIncident anomalyIncident) {
        Assertions.assertNotNull(anomalyIncident);
        Assertions.assertNotNull(anomalyIncident.getId());
        // Note: Service will not return metricId & detectionId when listing incidents by detection
        // config but only for alert.
        Assertions.assertNotNull(anomalyIncident.getSeverity());
        Assertions.assertNotNull(anomalyIncident.getStatus());
        Assertions.assertNotNull(anomalyIncident.getLastTime());
        Assertions.assertNotNull(anomalyIncident.getRootDimensionKey());
        Assertions.assertFalse(anomalyIncident.getRootDimensionKey().asMap().isEmpty());
        OffsetDateTime startTime = anomalyIncident.getStartTime();
        Assertions.assertNotNull(startTime);
        boolean isInRange = (startTime.isEqual(ListIncidentsDetectedInput.INSTANCE.startTime)
            || startTime.isAfter(ListIncidentsDetectedInput.INSTANCE.startTime))
            && (startTime.isEqual(ListIncidentsDetectedInput.INSTANCE.endTime)
            || startTime.isBefore(ListIncidentsDetectedInput.INSTANCE.endTime));
        Assertions.assertTrue(isInRange);
    }
}
