// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.metrics.models.MetricResult;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesOptions;
import com.azure.monitor.query.metrics.models.MetricsQueryTimeInterval;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MetricsAsyncClientTest extends MetricsClientTestBase {
    @Test
    public void testMetricsAsyncBatchQuery() {
        MetricsAsyncClient metricsClient
            = clientBuilder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildAsyncClient();
        String resourceId
            = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_METRICS_RESOURCE_URI_2", FAKE_RESOURCE_ID);
        resourceId = resourceId.substring(resourceId.indexOf("/subscriptions"));

        try {
            configClient.getConfigurationSetting("foo", "bar");
        } catch (HttpResponseException exception) {
            // ignore as this is only to generate some metrics
        }

        MetricsQueryResourcesOptions options = new MetricsQueryResourcesOptions().setGranularity(Duration.ofMinutes(15))
            .setTop(10)
            .setTimeInterval(new MetricsQueryTimeInterval(OffsetDateTime.now().minusDays(1), OffsetDateTime.now()));

        String finalResourceId = resourceId;
        StepVerifier.create(metricsClient
            .queryResourcesWithResponse(Arrays.asList(resourceId), Arrays.asList("HttpIncomingRequestCount"),
                "microsoft.appconfiguration/configurationstores", options)
            .flatMapMany(metricsQueryResourcesResultResponse -> {
                assertEquals(1, metricsQueryResourcesResultResponse.getValue().getMetricsQueryResults().size());
                assertEquals(1, metricsQueryResourcesResultResponse.getValue().getMetricsQueryResults().size());
                assertEquals(1,
                    metricsQueryResourcesResultResponse.getValue().getMetricsQueryResults().get(0).getMetrics().size());
                MetricResult metricResult = metricsQueryResourcesResultResponse.getValue()
                    .getMetricsQueryResults()
                    .get(0)
                    .getMetrics()
                    .get(0);
                assertEquals("HttpIncomingRequestCount", metricResult.getMetricName());
                assertFalse(CoreUtils.isNullOrEmpty(metricResult.getTimeSeries()));
                return metricsClient.queryResourcesWithResponse(Arrays.asList(finalResourceId),
                    Arrays.asList("HttpIncomingRequestCount"), "microsoft.appconfiguration/configurationstores",
                    options);
            })).expectNextCount(1).verifyComplete();
    }
}
