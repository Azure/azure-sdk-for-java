// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.Anomaly;
import com.azure.ai.metricsadvisor.models.ListAnomaliesAlertedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.rest.PagedIterable;

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
            .setTop(10);

        PagedIterable<Anomaly> anomaliesIterable = advisorClient.listAnomaliesForAlert(
            alertConfigurationId,
            alertId);

        for (Anomaly anomaly : anomaliesIterable) {
            System.out.printf("Metric Id: %s%n", anomaly.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", anomaly.getDetectionConfigurationId());
            System.out.printf("Anomaly Created Time: %s%n", anomaly.getCreatedTime());
            System.out.printf("Anomaly Modified Time: %s%n", anomaly.getModifiedTime());
            System.out.printf("Anomaly Severity: %s%n", anomaly.getSeverity());
            System.out.printf("Anomaly Status: %s%n", anomaly.getStatus());
            System.out.printf("Series Key: %s%n", anomaly.getSeriesKey().asMap());
        }
    }
}
