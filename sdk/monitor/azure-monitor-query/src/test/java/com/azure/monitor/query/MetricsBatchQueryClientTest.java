// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.Configuration;
import com.azure.monitor.query.models.MetricsBatchResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link MetricsBatchQueryClient}.
 */
public class MetricsBatchQueryClientTest extends MetricsBatchQueryTestBase {

    @Test
    @RecordWithoutRequestBody
    public void testMetricsBatchQuery() {
        MetricsBatchQueryClient metricsBatchQueryClient = clientBuilder
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        String resourceId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_METRICS_RESOURCE_URI", FAKE_RESOURCE_ID);
        resourceId = resourceId.substring(resourceId.indexOf("/subscriptions"));

        MetricsBatchResult metricsQueryResults = metricsBatchQueryClient.queryBatch(
            Arrays.asList(resourceId),
            Arrays.asList("Successful Requests"), " Microsoft.Eventhub/Namespaces");
        assertEquals(1, metricsQueryResults.getMetricsQueryResults().size());
    }
}
