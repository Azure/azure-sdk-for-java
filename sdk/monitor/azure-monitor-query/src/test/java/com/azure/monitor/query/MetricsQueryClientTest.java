// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.Context;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.MetricDefinition;
import com.azure.monitor.query.models.MetricNamespace;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricValue;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.QueryTimeInterval;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.monitor.query.MonitorQueryTestUtils.getMetricResourceUri;
import static com.azure.monitor.query.TestUtil.addTestProxySanitizersAndMatchers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MetricsQueryClient}.
 */
public class MetricsQueryClientTest extends TestProxyTestBase {
    private MetricsQueryClient client;

    private String resourceUri;

    private static Stream<Arguments> getFilterPredicate() {
        return Arrays.asList(
            Arguments.of(AggregationType.AVERAGE,
                (Predicate<MetricValue>) metricValue -> metricValue.getAverage() != null
                    && metricValue.getCount() == null
                    && metricValue.getTotal() == null
                    && metricValue.getMinimum() == null
                    && metricValue.getMaximum() == null),
            Arguments.of(AggregationType.COUNT,
                (Predicate<MetricValue>) metricValue -> metricValue.getCount() != null
                    && metricValue.getAverage() == null
                    && metricValue.getTotal() == null
                    && metricValue.getMinimum() == null
                    && metricValue.getMaximum() == null),
            Arguments.of(AggregationType.TOTAL,
                (Predicate<MetricValue>) metricValue -> metricValue.getTotal() != null
                    && metricValue.getCount() == null
                    && metricValue.getAverage() == null
                    && metricValue.getMinimum() == null
                    && metricValue.getMaximum() == null),
            Arguments.of(AggregationType.MINIMUM,
                (Predicate<MetricValue>) metricValue -> metricValue.getMinimum() != null
                    && metricValue.getCount() == null
                    && metricValue.getTotal() == null
                    && metricValue.getAverage() == null
                    && metricValue.getMaximum() == null),
            Arguments.of(AggregationType.MAXIMUM,
                (Predicate<MetricValue>) metricValue -> metricValue.getMaximum() != null
                    && metricValue.getCount() == null
                    && metricValue.getTotal() == null
                    && metricValue.getMinimum() == null
                    && metricValue.getAverage() == null))
            .stream();
    }

    @BeforeEach
    public void setup() {
        resourceUri = getMetricResourceUri(interceptorManager.isPlaybackMode());
        TokenCredential credential = TestUtil.getTestTokenCredential(interceptorManager);

        MetricsQueryClientBuilder clientBuilder = new MetricsQueryClientBuilder().credential(credential);
        if (getTestMode() == TestMode.PLAYBACK) {
            addTestProxySanitizersAndMatchers(interceptorManager);
            clientBuilder.httpClient(getAssertingHttpClient(interceptorManager.getPlaybackClient()));
        } else if (getTestMode() == TestMode.RECORD) {
            addTestProxySanitizersAndMatchers(interceptorManager);
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (getTestMode() == TestMode.LIVE) {
            clientBuilder.endpoint(MonitorQueryTestUtils.getMetricEndpoint());
        }
        this.client = clientBuilder.buildClient();
    }

    private HttpClient getAssertingHttpClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient).assertSync().skipRequest((request, context) -> false).build();
    }

    @Test
    public void testMetricsQuery() {
        Response<MetricsQueryResult> metricsResponse
            = client.queryResourceWithResponse(resourceUri, Arrays.asList("SuccessfulRequests"),
                new MetricsQueryOptions().setMetricNamespace("Microsoft.EventHub/namespaces")
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
        assertEquals("SuccessfulRequests", successfulCallsMetric.getMetricName());
        assertEquals("Microsoft.Insights/metrics", successfulCallsMetric.getResourceType());
        assertEquals(1, successfulCallsMetric.getTimeSeries().size());

        Assertions.assertTrue(successfulCallsMetric.getTimeSeries()
            .stream()
            .flatMap(timeSeriesElement -> timeSeriesElement.getValues().stream())
            .anyMatch(metricsValue -> Double.compare(0.0, metricsValue.getCount()) == 0));
    }

    @ParameterizedTest
    @MethodSource("getFilterPredicate")
    public void testAggregation(AggregationType aggregationType, Predicate<MetricValue> metricValuePredicate) {
        Response<MetricsQueryResult> metricsResponse
            = client.queryResourceWithResponse(resourceUri, Arrays.asList("SuccessfulRequests"),
                new MetricsQueryOptions().setMetricNamespace("Microsoft.EventHub/namespaces")
                    .setTimeInterval(new QueryTimeInterval(Duration.ofDays(10)))
                    .setGranularity(Duration.ofHours(1))
                    .setTop(100)
                    .setAggregations(Arrays.asList(aggregationType)),
                Context.NONE);

        MetricsQueryResult metricsQueryResult = metricsResponse.getValue();
        List<MetricResult> metrics = metricsQueryResult.getMetrics();
        List<MetricValue> metricValues = metrics.stream()
            .flatMap(result -> result.getTimeSeries().stream())
            .flatMap(tsElement -> tsElement.getValues().stream())
            .filter(metricValuePredicate)
            .collect(Collectors.toList());
        assertTrue(metricValues.size() > 0);
    }

    @Test
    public void testMetricsDefinition() {
        PagedIterable<MetricDefinition> metricsDefinitions = client.listMetricDefinitions(resourceUri);

        List<String> knownMetricsDefinitions = Arrays.asList("SuccessfulRequests", "ServerErrors", "UserErrors",
            "QuotaExceededErrors", "ThrottledRequests", "IncomingRequests", "IncomingMessages", "OutgoingMessages",
            "IncomingBytes", "OutgoingBytes", "ActiveConnections", "ConnectionsOpened", "ConnectionsClosed",
            "CaptureBacklog", "CapturedMessages", "CapturedBytes", "Size", "INREQS", "SUCCREQ", "FAILREQ", "SVRBSY",
            "INTERR", "MISCERR", "INMSGS", "EHINMSGS", "OUTMSGS", "EHOUTMSGS", "EHINMBS", "EHINBYTES", "EHOUTMBS",
            "EHOUTBYTES", "EHABL", "EHAMSGS", "EHAMBS");

        List<String> metricsDefinitionNames
            = metricsDefinitions.stream().map(MetricDefinition::getName).collect(Collectors.toList());

        assertTrue(metricsDefinitionNames.containsAll(knownMetricsDefinitions));
    }

    @Test
    public void testMetricsNamespaces() {
        PagedIterable<MetricNamespace> metricsNamespaces = client.listMetricNamespaces(resourceUri, null);
        assertEquals(1, metricsNamespaces.stream().count());
    }
}
