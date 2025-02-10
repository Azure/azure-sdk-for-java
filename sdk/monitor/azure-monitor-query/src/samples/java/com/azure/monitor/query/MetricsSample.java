// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsQueryResourcesResult;
import com.azure.monitor.query.models.MetricsQueryResult;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to synchronously query metrics for multiple resources.
 */
public class MetricsSample {
    /**
     * The main method to execute the sample.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        MetricsClient metricsClient = new MetricsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("https://westus2.monitoring.azure.com")
            .buildClient();

        MetricsQueryResourcesResult metricsQueryResourcesResult = metricsClient.queryResources(
            Arrays.asList("{resourceId1}", "{resourceId2}"),
            Arrays.asList("{metric1}", "{metric2}"),
            "{metricNamespace}");

        for (MetricsQueryResult metricsQueryResult : metricsQueryResourcesResult.getMetricsQueryResults()) {
            // Each MetricsQueryResult corresponds to one of the resourceIds in the batch request.
            List<MetricResult> metrics = metricsQueryResult.getMetrics();
            metrics.forEach(metric -> {
                System.out.println(metric.getMetricName());
                System.out.println(metric.getId());
                System.out.println(metric.getResourceType());
                System.out.println(metric.getUnit());
                System.out.println(metric.getTimeSeries().size());
                System.out.println(metric.getTimeSeries().get(0).getValues().size());
                metric.getTimeSeries()
                    .stream()
                    .flatMap(ts -> ts.getValues().stream())
                    .forEach(mv -> System.out.println(mv.getTimeStamp().toString()
                        + "; Count = " + mv.getCount()
                        + "; Average = " + mv.getAverage()));
            });
        }
    }
}
