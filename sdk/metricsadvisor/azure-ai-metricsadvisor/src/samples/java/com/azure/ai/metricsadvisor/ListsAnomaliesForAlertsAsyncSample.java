// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.ListAnomaliesAlertedOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;

/**
 * Sample demonstrates how to list anomalies that triggered an alert.
 */
public class ListsAnomaliesForAlertsAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAsyncClient advisorAsyncClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildAsyncClient();

        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListAnomaliesAlertedOptions options = new ListAnomaliesAlertedOptions()
            .setMaxPageSize(10);
        advisorAsyncClient.listAnomaliesForAlert(
            alertConfigurationId,
            alertId,
            options)
            .doOnNext(anomaly -> {
                System.out.printf("Data Feed Metric Id: %s%n", anomaly.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", anomaly.getDetectionConfigurationId());
                System.out.printf("DataPoint Anomaly Created Time: %s%n", anomaly.getCreatedTime());
                System.out.printf("DataPoint Anomaly Modified Time: %s%n", anomaly.getModifiedTime());
                System.out.printf("DataPoint Anomaly Severity: %s%n", anomaly.getSeverity());
                System.out.printf("DataPoint Anomaly Status: %s%n", anomaly.getStatus());
                System.out.printf("Series Key: %s%n", anomaly.getSeriesKey().asMap());
            }).blockLast();
            /*
              'blockLast()' will block until all the above CRUD on operation on detection is completed.
              This is strongly discouraged for use in production as it eliminates the benefits of
              asynchronous IO. It is used here to ensure the sample runs to completion.
             */
    }
}
