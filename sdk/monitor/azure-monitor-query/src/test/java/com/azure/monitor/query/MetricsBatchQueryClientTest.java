package com.azure.monitor.query;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.MetricsBatchResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Unit tests for {@link MetricsBatchQueryClient}.
 */
public class MetricsBatchQueryClientTest {

    @Test
    public void testMetricsBatchQuery() {
        MetricsBatchQueryClient metricsQueryClient = new MetricsBatchQueryClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_METRICS_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        MetricsBatchResult metricsQueryResults = metricsQueryClient.queryBatch(
            Arrays.asList(Configuration.getGlobalConfiguration().get("AZURE_METRICS_RESOURCE_URI")),
            Arrays.asList("Transactions"), "Account");
    }
}
