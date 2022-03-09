// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.rest.PagedIterable;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.MetricNamespace;

import java.time.OffsetDateTime;

/**
 * A sample to demonstrate listing all metric namespaces for a resource from Azure Monitor.
 */
public class MetricNamespacesSample {

    /**
     * The main method to execute the sample.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
                .credential(tokenCredential)
                .buildClient();

        PagedIterable<MetricNamespace> metricNamespaces = metricsQueryClient.listMetricNamespaces("{resource-uri}", OffsetDateTime.now().minusMonths(12));
        metricNamespaces.forEach(metricNamespace -> {
            System.out.println(metricNamespace.getName());
            System.out.println(metricNamespace.getId());
        });
    }
}
