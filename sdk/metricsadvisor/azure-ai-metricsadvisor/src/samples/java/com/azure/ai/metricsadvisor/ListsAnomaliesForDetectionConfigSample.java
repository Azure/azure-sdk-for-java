// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.DataPointAnomaly;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedFilter;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.core.http.rest.PagedIterable;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list anomalies identified by a detection configuration.
 */
public class ListsAnomaliesForDetectionConfigSample {
    public static void main(String[] args) {
        final MetricsAdvisorClient advisorClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter()
            .setSeverityRange(AnomalySeverity.LOW, AnomalySeverity.MEDIUM);
        final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions()
            .setMaxPageSize(10)
            .setFilter(filter);
        PagedIterable<DataPointAnomaly> anomaliesIterable
            = advisorClient.listAnomaliesForDetectionConfig(detectionConfigurationId,
                startTime, endTime);

        for (DataPointAnomaly dataPointAnomaly : anomaliesIterable) {
            System.out.printf("DataPoint Anomaly Severity: %s%n", dataPointAnomaly.getSeverity());
            System.out.printf("Series Key: %s%n", dataPointAnomaly.getSeriesKey().asMap().entrySet());
        }
    }
}
