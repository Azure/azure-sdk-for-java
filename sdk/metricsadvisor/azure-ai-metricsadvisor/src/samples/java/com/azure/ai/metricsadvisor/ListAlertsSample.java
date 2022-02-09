// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AlertQueryTimeMode;
import com.azure.ai.metricsadvisor.models.AnomalyAlert;
import com.azure.ai.metricsadvisor.models.ListAlertOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list alerts.
 */
public class ListAlertsSample {
    public static void main(String[] args) {
        final MetricsAdvisorClient advisorClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        // List alerts produced by an AlertConfiguration.
        //
        //   - The id of the AlertConfiguration resource.
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        //   - Each alert has 3 time attributes - anomaly-time, alert-created-time and alert-modified-time.
        //     The ANOMALY_TIME mode selects the time attribute anomaly-time of Alerts
        //     anomaly-time represents the time in which the anomaly occurred which triggered the alert.
        final AlertQueryTimeMode timeMode = AlertQueryTimeMode.ANOMALY_TIME;
        //   - The time period for the time attribute selected.
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        //
        final ListAlertOptions options = new ListAlertOptions()
            .setAlertQueryTimeMode(timeMode)
            .setMaxPageSize(10);

        PagedIterable<AnomalyAlert> alertsIterable = advisorClient.listAlerts(alertConfigurationId, startTime, endTime,
            options, Context.NONE);
        for (AnomalyAlert anomalyAlert : alertsIterable) {
            System.out.printf("Anomaly Alert Id: %s%n", anomalyAlert.getId());
            System.out.printf("Created Time: %s%n", anomalyAlert.getCreatedTime());
            System.out.printf("Modified Time: %s%n", anomalyAlert.getModifiedTime());
        }
    }
}
