// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.ai.metricsadvisor.models.ListIncidentsDetectedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list incidents detected by a detection configuration.
 */
public class ListIncidentsDetectedSample {
    public static void main(String[] args) {
        final MetricsAdvisorClient advisorClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListIncidentsDetectedOptions options = new ListIncidentsDetectedOptions()
            .setMaxPageSize(1000);

        PagedIterable<AnomalyIncident> incidentsIterable
            = advisorClient.listIncidentsForDetectionConfig(detectionConfigurationId, startTime, endTime, options,
            Context.NONE);

        for (AnomalyIncident anomalyIncident : incidentsIterable) {
            System.out.printf("Data Feed Metric Id: %s%n", anomalyIncident.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", anomalyIncident.getDetectionConfigurationId());
            System.out.printf("Anomaly Incident Id: %s%n", anomalyIncident.getId());
            System.out.printf("Anomaly Incident Start Time: %s%n", anomalyIncident.getStartTime());
            System.out.printf("Anomaly Incident Severity: %s%n", anomalyIncident.getSeverity());
            System.out.printf("Anomaly Incident Status: %s%n", anomalyIncident.getStatus());
            System.out.printf("Root Dimension Key: %s%n", anomalyIncident.getRootDimensionKey().asMap());
        }
    }
}
