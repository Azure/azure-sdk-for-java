// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.DataPointAnomaly;
import com.azure.ai.metricsadvisor.models.ListAnomaliesAlertedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;

/**
 * Sample demonstrates how to list anomalies that triggered an alert.
 */
public class ListsAnomaliesForAlertsSample {
    public static void main(String[] args) {
        final MetricsAdvisorClient advisorClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListAnomaliesAlertedOptions options = new ListAnomaliesAlertedOptions()
            .setMaxPageSize(10);

        PagedIterable<DataPointAnomaly> anomaliesIterable = advisorClient.listAnomaliesForAlert(
            alertConfigurationId,
            alertId,
            options,
            Context.NONE);

        for (DataPointAnomaly dataPointAnomaly : anomaliesIterable) {
            System.out.printf("Data Feed Metric Id: %s%n", dataPointAnomaly.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", dataPointAnomaly.getDetectionConfigurationId());
            System.out.printf("DataPoint Anomaly Created Time: %s%n", dataPointAnomaly.getCreatedTime());
            System.out.printf("DataPoint Anomaly Modified Time: %s%n", dataPointAnomaly.getModifiedTime());
            System.out.printf("DataPoint Anomaly Severity: %s%n", dataPointAnomaly.getSeverity());
            System.out.printf("DataPoint Anomaly Status: %s%n", dataPointAnomaly.getStatus());
            System.out.printf("Series Key: %s%n", dataPointAnomaly.getSeriesKey().asMap());
        }
    }
}
