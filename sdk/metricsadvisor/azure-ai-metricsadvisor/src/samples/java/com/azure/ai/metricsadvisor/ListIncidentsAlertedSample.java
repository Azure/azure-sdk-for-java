// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.ai.metricsadvisor.models.ListIncidentsAlertedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;

/**
 * Sample demonstrates how to list incidents in an alert.
 */
public class ListIncidentsAlertedSample {
    public static void main(String[] args) {
        final MetricsAdvisorClient advisorClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListIncidentsAlertedOptions options = new ListIncidentsAlertedOptions()
            .setMaxPageSize(10);

        PagedIterable<AnomalyIncident> incidentsIterable = advisorClient.listIncidentsForAlert(
            alertConfigurationId,
            alertId,
            options,
            Context.NONE);

        for (AnomalyIncident anomalyIncident : incidentsIterable) {
            System.out.printf("DataFeedMetric Id: %s%n", anomalyIncident.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", anomalyIncident.getDetectionConfigurationId());
            System.out.printf("Anomaly Incident Id: %s%n", anomalyIncident.getId());
            System.out.printf("Anomaly Incident Start Time: %s%n", anomalyIncident.getStartTime());
            System.out.printf("Anomaly Incident Severity: %s%n", anomalyIncident.getSeverity());
            System.out.printf("Anomaly Incident Status: %s%n", anomalyIncident.getStatus());
            System.out.printf("Root Dimension Key: %s%n", anomalyIncident.getRootDimensionKey().asMap());
        }
    }
}
