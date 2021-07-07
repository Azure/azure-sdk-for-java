// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedFilter;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list anomalies identified by a detection configuration.
 */
public class ListsAnomaliesForDetectionConfigAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAsyncClient advisorAsyncClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildAsyncClient();

        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter()
            .setSeverityRange(AnomalySeverity.LOW, AnomalySeverity.MEDIUM);
        final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions()
            .setMaxPageSize(10)
            .setFilter(filter);
        advisorAsyncClient.listAnomaliesForDetectionConfig(detectionConfigurationId,
                startTime, endTime, options)
            .doOnNext(anomaly -> {
                System.out.printf("DataPoint Anomaly Severity: %s%n", anomaly.getSeverity());
                System.out.printf("Series Key: %s%n", anomaly.getSeriesKey().asMap().entrySet());
            }).blockLast();
             /*
              'blockLast()' will block until all the above CRUD on operation on detection is completed.
              This is strongly discouraged for use in production as it eliminates the benefits of
              asynchronous IO. It is used here to ensure the sample runs to completion.
             */
    }
}
