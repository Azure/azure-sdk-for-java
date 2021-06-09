// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.Metric;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.QueryTimeSpan;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * A sample to demonstrate querying for metrics from Azure Monitor.
 */
public class MetricsQuerySample {

    /**
     * The main method to execute the sample.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
                .clientId(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_ID"))
                .clientSecret(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_SECRET"))
                .tenantId(Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID"))
                .build();

        MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
                .credential(tokenCredential)
                .buildClient();

        Response<MetricsQueryResult> metricsResponse = metricsQueryClient
                .queryMetricsWithResponse(
                        "/subscriptions/faa080af-c1d8-40ad-9cce-e1a450ca5b57/resourceGroups/srnagar-azuresdkgroup/providers/"
                                + "Microsoft.CognitiveServices/accounts/srnagara-textanalytics",
                        Arrays.asList("SuccessfulCalls"),
                        new MetricsQueryOptions()
                                .setMetricsNamespace("Microsoft.CognitiveServices/accounts")
                                .setTimeSpan(new QueryTimeSpan(Duration.ofDays(30)))
                                .setInterval(Duration.ofHours(1))
                                .setTop(100)
                                .setAggregation(Arrays.asList(AggregationType.AVERAGE, AggregationType.COUNT)),
                        Context.NONE);

        MetricsQueryResult metricsQueryResult = metricsResponse.getValue();
        List<Metric> metrics = metricsQueryResult.getMetrics();
        metrics.stream()
                .forEach(metric -> {
                    System.out.println(metric.getMetricsName());
                    System.out.println(metric.getId());
                    System.out.println(metric.getType());
                    System.out.println(metric.getUnit());
                    System.out.println(metric.getTimeSeries().size());
                    System.out.println(metric.getTimeSeries().get(0).getData().size());
                    metric.getTimeSeries()
                            .stream()
                            .flatMap(ts -> ts.getData().stream())
                            .forEach(mv -> System.out.println(mv.getTimeStamp().toString()
                                    + "; Count = " + mv.getCount()
                                    + "; Average = " + mv.getAverage()));
                });
    }
}
