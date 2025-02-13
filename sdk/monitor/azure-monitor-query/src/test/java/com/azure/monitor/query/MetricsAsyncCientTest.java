// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.query;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsQueryResourcesOptions;
import com.azure.monitor.query.models.MetricsQueryResourcesResult;
import com.azure.monitor.query.models.QueryTimeInterval;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MetricsAsyncCientTest extends MetricsClientTestBase {

    private static final Logger log = LoggerFactory.getLogger(MetricsAsyncCientTest.class);

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
            .setTimeInterval(new QueryTimeInterval(OffsetDateTime.now().minusDays(1), OffsetDateTime.now()));

        Response<MetricsQueryResourcesResult> metricsQueryResults
            = metricsClient
                .queryResourcesWithResponse(Arrays.asList(resourceId), Arrays.asList("HttpIncomingRequestCount"),
                    "microsoft.appconfiguration/configurationstores", options)
                .block();

        assertEquals(1, metricsQueryResults.getValue().getMetricsQueryResults().size());
        assertEquals(1, metricsQueryResults.getValue().getMetricsQueryResults().get(0).getMetrics().size());
        MetricResult metricResult = metricsQueryResults.getValue().getMetricsQueryResults().get(0).getMetrics().get(0);
        assertEquals("HttpIncomingRequestCount", metricResult.getMetricName());
        assertFalse(CoreUtils.isNullOrEmpty(metricResult.getTimeSeries()));
    }

    @Test
    public void testQueryResourcesReturnsNonNullResourceId() {
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
            .setTimeInterval(new QueryTimeInterval(OffsetDateTime.now().minusDays(1), OffsetDateTime.now()));

        MetricsQueryResourcesResult metricsQueryResults
            = metricsClient
                .queryResourcesWithResponse(Arrays.asList(resourceId), Arrays.asList("HttpIncomingRequestCount"),
                    "microsoft.appconfiguration/configurationstores", options)
                .block()
                .getValue();

        assertEquals(1, metricsQueryResults.getMetricsQueryResults().size());
        assertEquals(resourceId, metricsQueryResults.getMetricsQueryResults().get(0).getResourceId());
    }

}
