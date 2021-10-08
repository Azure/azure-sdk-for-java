// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.MetricDefinition;
import com.azure.monitor.query.models.MetricNamespace;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.QueryTimeInterval;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link MetricsQueryClient}.
 */
public class MetricsQueryClientTest extends TestBase {
    public static final String RESOURCE_URI = Configuration.getGlobalConfiguration()
            .get("AZURE_MONITOR_METRICS_RESOURCE_URI",
                    "/subscriptions/faa080af-c1d8-40ad-9cce-e1a450ca5b57/resourceGroups/srnagar-azuresdkgroup/providers/Microsoft.CognitiveServices/accounts/srnagara-textanalytics");
    private MetricsQueryClient client;

    @BeforeEach
    public void setup() {
        MetricsQueryClientBuilder clientBuilder = new MetricsQueryClientBuilder();
        if (getTestMode() == TestMode.PLAYBACK) {
            clientBuilder
                .credential(request -> Mono.just(new AccessToken("fakeToken", OffsetDateTime.now().plusDays(1))))
                .httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            clientBuilder
                .addPolicy(interceptorManager.getRecordPolicy())
                .credential(getCredential());
        } else if (getTestMode() == TestMode.LIVE) {
            clientBuilder.credential(new DefaultAzureCredentialBuilder().build());
        }
        this.client = clientBuilder
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();
    }

    private TokenCredential getCredential() {
        return new ClientSecretCredentialBuilder()
            .clientId(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_ID"))
            .clientSecret(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_SECRET"))
            .tenantId(Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID"))
            .build();
    }

    @Test
    public void testMetricsQuery() {
        Response<MetricsQueryResult> metricsResponse = client
            .queryResourceWithResponse(RESOURCE_URI, Arrays.asList("SuccessfulCalls"),
                new MetricsQueryOptions()
                    .setMetricNamespace("Microsoft.CognitiveServices/accounts")
                    .setTimeInterval(new QueryTimeInterval(Duration.ofDays(10)))
                    .setGranularity(Duration.ofHours(1))
                    .setTop(100)
                    .setAggregations(Arrays.asList(AggregationType.COUNT, AggregationType.TOTAL,
                            AggregationType.MAXIMUM, AggregationType.MINIMUM, AggregationType.AVERAGE)),
                Context.NONE);

        MetricsQueryResult metricsQueryResult = metricsResponse.getValue();
        List<MetricResult> metrics = metricsQueryResult.getMetrics();

        assertEquals(1, metrics.size());
        MetricResult successfulCallsMetric = metrics.get(0);
        assertEquals("SuccessfulCalls", successfulCallsMetric.getMetricName());
        assertEquals("Microsoft.Insights/metrics", successfulCallsMetric.getResourceType());
        assertEquals(1, successfulCallsMetric.getTimeSeries().size());

        Assertions.assertTrue(successfulCallsMetric.getTimeSeries()
            .stream()
            .flatMap(timeSeriesElement -> timeSeriesElement.getValues().stream())
            .anyMatch(metricsValue -> Double.compare(0.0, metricsValue.getCount()) == 0));
    }

    @Test
    public void testMetricsDefinition() {
        PagedIterable<MetricDefinition> metricsDefinitions = client
                .listMetricDefinitions(RESOURCE_URI);
        assertEquals(12, metricsDefinitions.stream().count());
    }

    @Test
    public void testMetricsNamespaces() {
        PagedIterable<MetricNamespace> metricsNamespaces = client.listMetricNamespaces(RESOURCE_URI, null);
        assertEquals(1, metricsNamespaces.stream().count());
    }
}
