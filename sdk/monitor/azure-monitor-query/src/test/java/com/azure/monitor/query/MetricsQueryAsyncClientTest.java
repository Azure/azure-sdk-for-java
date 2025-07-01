// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.Context;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.MetricDefinition;
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
import reactor.test.StepVerifier;

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
 * Unit tests for {@link MetricsQueryAsyncClient}.
 */
public class MetricsQueryAsyncClientTest extends TestProxyTestBase {

    private MetricsQueryAsyncClient client;

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
        this.client = clientBuilder.buildAsyncClient();
    }

    private HttpClient getAssertingHttpClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient).assertAsync()
            .skipRequest((request, context) -> false)
            .build();
    }

    @Test
    public void testMetricsQuery() {
        StepVerifier
            .create(client.queryResourceWithResponse(resourceUri, Arrays.asList("SuccessfulRequests"),
                new MetricsQueryOptions().setMetricNamespace("Microsoft.EventHub/namespaces")
                    .setTimeInterval(new QueryTimeInterval(Duration.ofDays(10)))
                    .setGranularity(Duration.ofHours(1))
                    .setTop(100)
                    .setAggregations(Arrays.asList(AggregationType.COUNT, AggregationType.TOTAL,
                        AggregationType.MAXIMUM, AggregationType.MINIMUM, AggregationType.AVERAGE)),
                Context.NONE))
            .assertNext(response -> {
                MetricsQueryResult metricsQueryResult = response.getValue();
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
            })
            .verifyComplete();

    }

    @ParameterizedTest
    @MethodSource("getFilterPredicate")
    public void testAggregation(AggregationType aggregationType, Predicate<MetricValue> metricValuePredicate) {
        StepVerifier.create(client.queryResourceWithResponse(resourceUri, Arrays.asList("SuccessfulRequests"),
            new MetricsQueryOptions().setMetricNamespace("Microsoft.EventHub/namespaces")
                .setTimeInterval(new QueryTimeInterval(Duration.ofDays(10)))
                .setGranularity(Duration.ofHours(1))
                .setTop(100)
                .setAggregations(Arrays.asList(aggregationType)),
            Context.NONE)).assertNext(metricsResponse -> {
                MetricsQueryResult metricsQueryResult = metricsResponse.getValue();
                List<MetricResult> metrics = metricsQueryResult.getMetrics();
                List<MetricValue> metricValues = metrics.stream()
                    .flatMap(result -> result.getTimeSeries().stream())
                    .flatMap(tsElement -> tsElement.getValues().stream())
                    .filter(metricValuePredicate)
                    .collect(Collectors.toList());
                assertTrue(metricValues.size() > 0);
            }).verifyComplete();
    }

    @Test
    public void testMetricsDefinition() {
        List<String> knownMetricsDefinitions = Arrays.asList("SuccessfulRequests", "ServerErrors", "UserErrors",
            "QuotaExceededErrors", "ThrottledRequests", "IncomingRequests", "IncomingMessages", "OutgoingMessages",
            "IncomingBytes", "OutgoingBytes", "ActiveConnections", "ConnectionsOpened", "ConnectionsClosed",
            "CaptureBacklog", "CapturedMessages", "CapturedBytes", "Size", "INREQS", "SUCCREQ", "FAILREQ", "SVRBSY",
            "INTERR", "MISCERR", "INMSGS", "EHINMSGS", "OUTMSGS", "EHOUTMSGS", "EHINMBS", "EHINBYTES", "EHOUTMBS",
            "EHOUTBYTES", "EHABL", "EHAMSGS", "EHAMBS");

        StepVerifier.create(client.listMetricDefinitions(resourceUri).collectList()).assertNext(metricDefinitions -> {
            List<String> metricsDefinitionNames
                = metricDefinitions.stream().map(MetricDefinition::getName).collect(Collectors.toList());

            assertTrue(metricsDefinitionNames.containsAll(knownMetricsDefinitions));
        }).verifyComplete();
    }

    @Test
    public void testMetricsNamespaces() {
        StepVerifier.create(client.listMetricNamespaces(resourceUri, null).collectList())
            .assertNext(namespaces -> assertEquals(1, namespaces.size()))
            .verifyComplete();
    }

    @Test
    public void testDurationBasedQueryTimeIntervalLast30Minutes() {
        // Test the specific case mentioned in issue #45283
        StepVerifier.create(client.queryResourceWithResponse(resourceUri, Arrays.asList("SuccessfulRequests"),
            new MetricsQueryOptions().setMetricNamespace("Microsoft.EventHub/namespaces")
                .setTimeInterval(QueryTimeInterval.LAST_30_MINUTES)
                .setGranularity(Duration.ofMinutes(5))
                .setAggregations(Arrays.asList(AggregationType.COUNT))))
            .assertNext(response -> {
                MetricsQueryResult result = response.getValue();

                // Verify that the service accepted the timespan and returned a valid result
                Assertions.assertNotNull(result, "Metrics result should not be null");
                Assertions.assertNotNull(result.getTimeInterval(), "Time interval should be present in response");
                Assertions.assertFalse(result.getMetrics().isEmpty(), "Metrics should not be empty");

                // Verify the timespan is in the correct absolute format, not just "PT30M"
                Assertions.assertNotNull(result.getTimeInterval().getStartTime(), "Start time should be present");
                Assertions.assertNotNull(result.getTimeInterval().getEndTime(), "End time should be present");
                Assertions.assertNull(result.getTimeInterval().getDuration(), "Duration should not be present");

                // Verify that the start time and end time are 30 minutes apart
                Assertions.assertTrue(Duration.between(result.getTimeInterval().getStartTime(), result.getTimeInterval().getEndTime()).toMinutes() == 30, "Start time and end time should be 30 minutes apart");
            })
            .verifyComplete();
    }

    @Test
    public void testDurationBasedQueryTimeIntervalLast1Hour() {
        StepVerifier.create(client.queryResourceWithResponse(resourceUri, Arrays.asList("SuccessfulRequests"),
            new MetricsQueryOptions().setMetricNamespace("Microsoft.EventHub/namespaces")
                .setTimeInterval(QueryTimeInterval.LAST_1_HOUR)
                .setGranularity(Duration.ofMinutes(15))
                .setAggregations(Arrays.asList(AggregationType.COUNT))))
            .assertNext(response -> {
                MetricsQueryResult result = response.getValue();

                // Verify that the service accepted the timespan and returned a valid result
                Assertions.assertNotNull(result, "Metrics result should not be null");
                Assertions.assertNotNull(result.getTimeInterval(), "Time interval should be present in response");
                Assertions.assertFalse(result.getMetrics().isEmpty(), "Metrics should not be empty");

                // Verify the timespan is in absolute format
                Assertions.assertNotNull(result.getTimeInterval().getStartTime(), "Start time should be present");
                Assertions.assertNotNull(result.getTimeInterval().getEndTime(), "End time should be present");
                Assertions.assertNull(result.getTimeInterval().getDuration(), "Duration should not be present");

                // Verify that the start time and end time are 1 hour apart
                Assertions.assertTrue(Duration.between(result.getTimeInterval().getStartTime(), result.getTimeInterval().getEndTime()).toHours() == 1, "Start time and end time should be 1 hour apart");
            })
            .verifyComplete();
    }

    @Test
    public void testDurationBasedQueryTimeIntervalLastDay() {
        StepVerifier.create(client.queryResourceWithResponse(resourceUri, Arrays.asList("SuccessfulRequests"),
            new MetricsQueryOptions().setMetricNamespace("Microsoft.EventHub/namespaces")
                .setTimeInterval(QueryTimeInterval.LAST_DAY)
                .setGranularity(Duration.ofHours(1))
                .setAggregations(Arrays.asList(AggregationType.COUNT))))
            .assertNext(response -> {
                MetricsQueryResult result = response.getValue();

                // Verify that the service accepted the timespan and returned a valid result
                Assertions.assertNotNull(result, "Metrics result should not be null");
                Assertions.assertNotNull(result.getTimeInterval(), "Time interval should be present in response");
                Assertions.assertFalse(result.getMetrics().isEmpty(), "Metrics should not be empty");

                // Verify the timespan is in absolute format
                Assertions.assertNotNull(result.getTimeInterval().getStartTime(), "Start time should be present");
                Assertions.assertNotNull(result.getTimeInterval().getEndTime(), "End time should be present");
                Assertions.assertNull(result.getTimeInterval().getDuration(), "Duration should not be present");

                // Verify that the start time and end time are 1 day apart
                Assertions.assertTrue(Duration.between(result.getTimeInterval().getStartTime(), result.getTimeInterval().getEndTime()).toDays() == 1, "Start time and end time should be 1 day apart");
            })
            .verifyComplete();
    }
}
