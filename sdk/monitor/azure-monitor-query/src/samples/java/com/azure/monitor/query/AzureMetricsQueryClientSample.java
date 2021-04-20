// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.metric.implementation.models.Metric;
import com.azure.monitor.query.metric.implementation.models.MetricsResponse;
import com.azure.monitor.query.metric.implementation.models.ResultType;

import java.time.Duration;
import java.util.List;

public class AzureMetricsQueryClientSample {
    public static void main(String[] args) {
        ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
            .clientId(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_ID"))
            .clientSecret(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_SECRET"))
            .tenantId(Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID"))
            .build();

        AzureMonitorQueryClient azureMetricQueryClient = new AzureMonitorQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        MetricsResponse metricsResponse = azureMetricQueryClient.queryMetrics("/subscriptions/faa080af-c1d8-40ad-9cce" +
                "-e1a450ca5b57/resourceGroups/srnagar-azuresdkgroup/providers/Microsoft.CognitiveServices/accounts/srnagara-textanalytics", Duration.ofDays(30).toString(),
            Duration.ofHours(1), "SuccessfulCalls", "1,2,3,4,5", 100, null, null, ResultType.DATA, "Microsoft" +
                ".CognitiveServices/accounts");

        List<Metric> value = metricsResponse.getValue();
        value.stream()
            .forEach(metric -> {
                System.out.println(metric.getName().getLocalizedValue());
                System.out.println(metric.getId());
                System.out.println(metric.getType());
                System.out.println(metric.getUnit());
                System.out.println(metric.getTimeseries().size());
                System.out.println(metric.getTimeseries().get(0).getData().size());
                metric.getTimeseries()
                    .stream()
                    .flatMap(ts -> ts.getData().stream())
                    .forEach(mv -> System.out.println(mv.getTimeStamp().toString() + "; Count = " + mv.getCount() +
                        "; Average = " + mv.getAverage() + "; Maximum  " + mv.getMaximum() + "; Minimum = " + mv.getMinimum()));
            });


    }
}
