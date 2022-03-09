// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.rest.PagedIterable;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.MetricDefinition;

/**
 * A sample to demonstrate list all metric definitions for a resource from Azure Monitor.
 */
public class MetricDefinitionsSample {

    /**
     * The main method to execute the sample.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
                .credential(tokenCredential)
                .buildClient();

        PagedIterable<MetricDefinition> metricDefinitions = metricsQueryClient.listMetricDefinitions("{resource-uri}");
        metricDefinitions.forEach(metricDefinition -> {
            System.out.println(metricDefinition.getName());
            System.out.println(metricDefinition.getCategory());
            System.out.println(metricDefinition.getPrimaryAggregationType());
            System.out.println(metricDefinition.getCategory());
        });
    }
}
