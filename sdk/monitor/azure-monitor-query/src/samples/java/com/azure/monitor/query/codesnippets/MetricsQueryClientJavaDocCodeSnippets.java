// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.codesnippets;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.MetricsQueryAsyncClient;
import com.azure.monitor.query.MetricsQueryClient;
import com.azure.monitor.query.MetricsQueryClientBuilder;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsQueryResult;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Class containing JavaDoc codesnippets for metrics query client.
 */
public class MetricsQueryClientJavaDocCodeSnippets {
    /**
     * Code snippet for creating a metrics query client.
     */
    public void instantiation() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // BEGIN: com.azure.monitor.query.MetricsQueryClient.instantiation
        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
                .credential(tokenCredential)
                .buildClient();
        // END: com.azure.monitor.query.MetricsQueryClient.instantiation

        // BEGIN: com.azure.monitor.query.MetricsQueryAsyncClient.instantiation
        MetricsQueryAsyncClient metricsQueryAsyncClient = new MetricsQueryClientBuilder()
                .credential(tokenCredential)
                .buildAsyncClient();
        // END: com.azure.monitor.query.MetricsQueryAsyncClient.instantiation
    }

    public void queryMetricsAsync() {
        MetricsQueryAsyncClient metricsQueryAsyncClient = new MetricsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();
        // BEGIN: com.azure.monitor.query.MetricsQueryAsyncClient.query#String-List
        Mono<MetricsQueryResult> response = metricsQueryAsyncClient
                .queryResource("{resource-id}", Arrays.asList("{metric-1}", "{metric-2}"));

        response.subscribe(result -> {
            for (MetricResult metricResult : result.getMetrics()) {
                System.out.println("Metric name " + metricResult.getMetricName());
                metricResult.getTimeSeries().stream()
                        .flatMap(timeSeriesElement -> timeSeriesElement.getValues().stream())
                        .forEach(metricValue ->
                                System.out.println("Time stamp: " + metricValue.getTimeStamp() + "; Total:  "
                                        + metricValue.getTotal()));
            }
        });
        // END: com.azure.monitor.query.MetricsQueryAsyncClient.query#String-List
    }

    public void queryMetrics() {
        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        // BEGIN: com.azure.monitor.query.MetricsQueryClient.query#String-List
        MetricsQueryResult response = metricsQueryClient.queryResource("{resource-id}",
                Arrays.asList("{metric-1}", "{metric-2}"));
        for (MetricResult metricResult : response.getMetrics()) {
            System.out.println("Metric name " + metricResult.getMetricName());

            metricResult.getTimeSeries().stream()
                    .flatMap(timeSeriesElement -> timeSeriesElement.getValues().stream())
                    .forEach(metricValue ->
                            System.out.println("Time stamp: " + metricValue.getTimeStamp() + "; Total:  "
                                    + metricValue.getTotal()));
        }

        // END: com.azure.monitor.query.MetricsQueryClient.query#String-List
    }
}
