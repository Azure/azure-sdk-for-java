// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.codesnippets;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.MetricsBatchQueryAsyncClient;
import com.azure.monitor.query.MetricsBatchQueryClient;
import com.azure.monitor.query.MetricsBatchQueryClientBuilder;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsBatchResult;
import com.azure.monitor.query.models.MetricsQueryResult;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets
 * for {@link com.azure.monitor.query.MetricsBatchQueryClient}
 */
public class MetricsBatchQueryClientJavaDocCodeSnippets {
    /**
     * Generates code sample for creating a {@link MetricsBatchQueryClient}.
     */
    public void createClient() {
        // BEGIN: com.azure.monitor.query.MetricsBatchQueryClient.instantiation
        MetricsBatchQueryClient metricsBatchQueryClient = new MetricsBatchQueryClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.monitor.query.MetricsBatchQueryClient.instantiation
    }

    public void createAsyncClient() {
        // BEGIN: com.azure.monitor.query.MetricsBatchQueryAsyncClient.instantiation
        MetricsBatchQueryAsyncClient metricsBatchQueryAsyncClient = new MetricsBatchQueryClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.monitor.query.MetricsBatchQueryAsyncClient.instantiation
    }

    /**
     * Generates a code sample for using {@link MetricsBatchQueryClient#queryBatch(List, List, String)}.
     */
    public void queryBatch() {
        MetricsBatchQueryClient metricsBatchQueryClient = new MetricsBatchQueryClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: com.azure.monitor.query.MetricsBatchQueryClient.queryBatch#List-List-String
        MetricsBatchResult metricsBatchResult = metricsBatchQueryClient.queryBatch(
            Arrays.asList("{resourceId1}", "{resourceId2}"),
            Arrays.asList("{metricId}"), "{metricNamespace}");

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
        // END: com.azure.monitor.query.MetricsBatchQueryClient.queryBatch#List-List-String
    }

    public void queryBatchAsync() {
        MetricsBatchQueryAsyncClient metricsBatchQueryAsyncClient = new MetricsBatchQueryClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // BEGIN: com.azure.monitor.query.MetricsBatchQueryAsyncClient.queryBatch#List-List-String
        metricsBatchQueryAsyncClient.queryBatch(
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
