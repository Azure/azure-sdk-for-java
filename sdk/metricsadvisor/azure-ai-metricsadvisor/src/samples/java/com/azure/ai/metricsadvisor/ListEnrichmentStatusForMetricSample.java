// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.ListMetricEnrichmentStatusOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.util.Context;

import java.time.OffsetDateTime;

/**
 * Sample for listing enrichment statuses for a metric.
 */
public class ListEnrichmentStatusForMetricSample {
    public static void main(String[] args) {
        final MetricsAdvisorClient advisorClient = new MetricsAdvisorClientBuilder()
            .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
            .endpoint("{endpoint}")
            .buildClient();

        final String metricId = "24gdgfbbbb-41ec-a637-677e77b81455";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListMetricEnrichmentStatusOptions options = new ListMetricEnrichmentStatusOptions()
            .setMaxPageSize(10);

        advisorClient.listMetricEnrichmentStatus(metricId, startTime, endTime, options, Context.NONE)
            .forEach(enrichmentStatus -> {
                System.out.printf("Data Feed Metric enrichment status : %s%n", enrichmentStatus.getStatus());
                System.out.printf("Data Feed Metric enrichment status message: %s%n", enrichmentStatus.getMessage());
                System.out.printf("Data Feed Metric enrichment status data slice timestamp : %s%n",
                    enrichmentStatus.getTimestamp());
            });
    }
}
