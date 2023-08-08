// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.MetricEnrichedSeriesData;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.http.rest.PagedIterable;

import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Sample demonstrates how to list enriched time series.
 */
public class ListEnrichedSeriesSample {
    public static void main(String[] args) {
        final MetricsAdvisorClient advisorClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        final String detectionConfigurationId = "e87d899d-a5a0-4259-b752-11aea34d5e34";
        final DimensionKey seriesKey = new DimensionKey()
            .put("Dim1", "Common Lime")
            .put("Dim2", "Antelope");
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-08-12T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-12T00:00:00Z");

        PagedIterable<MetricEnrichedSeriesData> enrichedDataIterable
            = advisorClient.listMetricEnrichedSeriesData(detectionConfigurationId,
            Arrays.asList(seriesKey),
            startTime,
            endTime);

        for (MetricEnrichedSeriesData enrichedData : enrichedDataIterable) {
            System.out.printf("Series Key %s%n:", enrichedData.getSeriesKey().asMap());
            System.out.println("List of data points for this series");
            System.out.println(enrichedData.getMetricValues());
            System.out.println("Timestamps of the data related to this time series:");
            System.out.println(enrichedData.getTimestamps());
            System.out.println("The expected values of the data points calculated by the smart detector:");
            System.out.println(enrichedData.getExpectedMetricValues());
            System.out.println("The lower boundary values of the data points calculated by smart detector:");
            System.out.println(enrichedData.getLowerBoundaryValues());
            System.out.println("the periods calculated for the data points in the time series:");
            System.out.println(enrichedData.getPeriods());
        }
    }
}
