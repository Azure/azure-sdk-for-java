// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.ai.metricsadvisor.models.ListIncidentsDetectedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.rest.PagedFlux;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list incidents detected by a detection configuration.
 */
public class ListIncidentsDetectedAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAsyncClient advisorAsyncClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildAsyncClient();

        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListIncidentsDetectedOptions options = new ListIncidentsDetectedOptions()
            .setMaxPageSize(1000);

        PagedFlux<AnomalyIncident> incidentsFlux
            = advisorAsyncClient.listIncidentsForDetectionConfig(detectionConfigurationId, startTime, endTime, options);

        incidentsFlux.doOnNext(incident -> {
            System.out.printf("Data Feed Metric Id: %s%n", incident.getMetricId());
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
