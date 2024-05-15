// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.codesnippets;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.MetricsAsyncClient;
import com.azure.monitor.query.MetricsClient;
import com.azure.monitor.query.MetricsClientBuilder;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsQueryResourcesResult;
import com.azure.monitor.query.models.MetricsQueryResult;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets
 * for {@link MetricsClient}
 */
public class MetricsBatchQueryClientJavaDocCodeSnippets {
    /**
     * Generates code sample for creating a {@link MetricsClient}.
     */
    public void createClient() {
        // BEGIN: com.azure.monitor.query.MetricsBatchQueryClient.instantiation
        MetricsClient metricsBatchQueryClient = new MetricsClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.monitor.query.MetricsBatchQueryClient.instantiation
    }

    public void createAsyncClient() {
        // BEGIN: com.azure.monitor.query.MetricsBatchQueryAsyncClient.instantiation
        MetricsAsyncClient metricsAsyncClient = new MetricsClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.monitor.query.MetricsBatchQueryAsyncClient.instantiation
    }

    /**
     * Generates a code sample for using {@link MetricsClient#queryResources(List, List, String)}.
     */
    public void queryBatch() {
        MetricsClient metricsBatchQueryClient = new MetricsClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: com.azure.monitor.query.MetricsBatchQueryClient.queryBatch#List-List-String
        MetricsQueryResourcesResult metricsQueryResourcesResult = metricsBatchQueryClient.queryResources(
            Arrays.asList("{resourceId1}", "{resourceId2}"),
            Arrays.asList("{metricId}"), "{metricNamespace}");

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
        // END: com.azure.monitor.query.MetricsBatchQueryClient.queryBatch#List-List-String
    }

    public void queryBatchAsync() {
        MetricsAsyncClient metricsAsyncClient = new MetricsClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // BEGIN: com.azure.monitor.query.MetricsBatchQueryAsyncClient.queryBatch#List-List-String
        metricsAsyncClient.queryResources(
                Arrays.asList("{resourceId1}", "{resourceId2}"),
                Arrays.asList("{metricId}"), "{metricNamespace}")
            .subscribe(metricsBatchResult -> {
                for (MetricsQueryResult metricsQueryResult : metricsBatchResult.getMetricsQueryResults()) {
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
            });
        // END: com.azure.monitor.query.MetricsBatchQueryAsyncClient.queryBatch#List-List-String
    }
}
