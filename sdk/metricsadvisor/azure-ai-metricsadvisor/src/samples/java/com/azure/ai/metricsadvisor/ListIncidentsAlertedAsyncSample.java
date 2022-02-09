// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.ai.metricsadvisor.models.ListIncidentsAlertedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.rest.PagedFlux;

/**
 * Sample demonstrates how to list incidents in an alert.
 */
public class ListIncidentsAlertedAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAsyncClient advisorAsyncClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildAsyncClient();

        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListIncidentsAlertedOptions options = new ListIncidentsAlertedOptions()
            .setMaxPageSize(10);

        PagedFlux<AnomalyIncident> incidentsPagedFlux = advisorAsyncClient.listIncidentsForAlert(
            alertConfigurationId,
            alertId,
            options);

        incidentsPagedFlux.doOnNext(incident -> {
            System.out.printf("DataFeedMetric Id: %s%n", incident.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", incident.getDetectionConfigurationId());
            System.out.printf("Anomaly Incident Id: %s%n", incident.getId());
            System.out.printf("Anomaly Incident Start Time: %s%n", incident.getStartTime());
            System.out.printf("Anomaly Incident Severity: %s%n", incident.getSeverity());
            System.out.printf("Anomaly Incident Status: %s%n", incident.getStatus());
            System.out.printf("Root Dimension Key: %s%n", incident.getRootDimensionKey().asMap());
        }).blockLast();
        /*
          'blockLast()' will block until the above operation s completed.
          This is strongly discouraged for use in production as it eliminates the benefits of
          asynchronous IO. It is used here to ensure the sample runs to completion.
         */
    }
}
