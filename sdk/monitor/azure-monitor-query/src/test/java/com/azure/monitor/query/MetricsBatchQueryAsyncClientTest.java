// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsBatchQueryOptions;
import com.azure.monitor.query.models.QueryTimeInterval;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link MetricsBatchQueryAsyncClient}.
 */
public class MetricsBatchQueryAsyncClientTest extends MetricsBatchQueryTestBase {

    @Test
    public void testMetricsBatchQuery() {
        MetricsBatchQueryAsyncClient metricsBatchQueryAsyncClient = clientBuilder
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();
        String resource = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_METRICS_RESOURCE_URI_2", FAKE_RESOURCE_ID);
        String resourceId = resource.substring(resource.indexOf("/subscriptions"));

        MetricsBatchQueryOptions options = new MetricsBatchQueryOptions()
            .setGranularity(Duration.ofMinutes(15))
            .setTop(10)
            .setTimeInterval(new QueryTimeInterval(OffsetDateTime.now().minusDays(1), OffsetDateTime.now()));

        StepVerifier.create(configAsyncClient.getConfigurationSetting("foo", "bar")
                .onErrorResume(e -> Mono.empty())
                .then(metricsBatchQueryAsyncClient.queryBatchWithResponse(
                        Arrays.asList(resourceId),
                        Arrays.asList("HttpIncomingRequestCount"), "microsoft.appconfiguration/configurationstores", options)
                    .map(response -> response.getValue())))
            .assertNext(result -> {
                assertEquals(1, result.getMetricsQueryResults().size());
                assertEquals(1, result.getMetricsQueryResults().get(0).getMetrics().size());
                MetricResult metricResult = result.getMetricsQueryResults().get(0).getMetrics().get(0);
                assertEquals("HttpIncomingRequestCount", metricResult.getMetricName());
                assertFalse(CoreUtils.isNullOrEmpty(metricResult.getTimeSeries()));
            }).verifyComplete();
    }

    @Test
    public void testMetricsBatchQueryDifferentResourceTypes() {
        MetricsBatchQueryAsyncClient metricsBatchQueryAsyncClient = clientBuilder
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();

        String resourceId1 = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_METRICS_RESOURCE_URI_1", FAKE_RESOURCE_ID);
        String resourceId2 = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_METRICS_RESOURCE_URI_2", FAKE_RESOURCE_ID);
        String updatedResource1 = resourceId1.substring(resourceId1.indexOf("/subscriptions"));
        String updatedResource2 = resourceId2.substring(resourceId2.indexOf("/subscriptions"));

        StepVerifier.create(metricsBatchQueryAsyncClient.queryBatch(Arrays.asList(updatedResource1, updatedResource2),
                Arrays.asList("Successful Requests"), " Microsoft.Eventhub/Namespaces"))
            .verifyError(HttpResponseException.class);
    }


}
