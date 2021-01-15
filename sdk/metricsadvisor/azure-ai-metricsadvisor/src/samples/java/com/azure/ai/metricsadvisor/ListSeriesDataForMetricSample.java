// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;

import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Sample for listing time series data for a metric filtered using specific set of dimensions.
 */
public class ListSeriesDataForMetricSample {
    public static void main(String[] args) {
        final MetricsAdvisorClient advisorClient = new MetricsAdvisorClientBuilder()
            .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
            .endpoint("{endpoint}")
            .buildClient();

        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final String metricId = "2dgfbbbb-41ec-a637-677e77b81455";

        advisorClient.listMetricSeriesData(metricId,
            Arrays.asList(new DimensionKey().put("cost", "redmond")),
            startTime, endTime)
            .forEach(metricSeriesData -> {
                System.out.println("List of data points for this series:");
                System.out.println(metricSeriesData.getMetricValues());
                System.out.println("Timestamps of the data related to this time series:");
                System.out.println(metricSeriesData.getTimestamps());
                System.out.printf("Series Key: %s%n", metricSeriesData.getSeriesKey().asMap());
            });
    }
}
