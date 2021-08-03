// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.FeedbackQueryTimeMode;
import com.azure.ai.metricsadvisor.models.FeedbackType;
import com.azure.ai.metricsadvisor.models.ListMetricFeedbackFilter;
import com.azure.ai.metricsadvisor.models.ListMetricFeedbackOptions;
import com.azure.ai.metricsadvisor.models.MetricFeedback;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.util.Context;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.MetricsSeriesTestBase.METRIC_ID;
import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID;
import static com.azure.ai.metricsadvisor.TestUtils.INCORRECT_UUID_ERROR;
import static com.azure.ai.metricsadvisor.models.FeedbackType.ANOMALY;
import static com.azure.ai.metricsadvisor.models.FeedbackType.CHANGE_POINT;
import static com.azure.ai.metricsadvisor.models.FeedbackType.COMMENT;
import static com.azure.ai.metricsadvisor.models.FeedbackType.PERIOD;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FeedbackAsyncTest extends FeedbackTestBase {
    private MetricsAdvisorAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        TestBase.setupClass();
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    /**
     * Verifies the result of the list metric feedback  method when no options specified.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListMetricFeedback(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();

        listMetricFeedbackRunner(inputMetricFeedbackList -> {
            List<MetricFeedback> actualMetricFeedbackList = new ArrayList<>();
            List<MetricFeedback> expectedMetricFeedbackList =
                inputMetricFeedbackList.stream().map(metricFeedback  ->
                    client.addFeedback(METRIC_ID, metricFeedback)
                    .block())
                    .collect(Collectors.toList());

            final MetricFeedback firstFeedback = expectedMetricFeedbackList.get(0);
            final OffsetDateTime firstFeedbackCreatedTime = firstFeedback.getCreatedTime();

            // Act
            StepVerifier.create(client.listFeedback(METRIC_ID,
                new ListMetricFeedbackOptions()
                    .setFilter(new ListMetricFeedbackFilter()
                        .setTimeMode(FeedbackQueryTimeMode.FEEDBACK_CREATED_TIME)
                        .setStartTime(firstFeedbackCreatedTime.minusDays(1))
                        .setEndTime(firstFeedbackCreatedTime.plusDays(1))),
                Context.NONE))
                .thenConsumeWhile(actualMetricFeedbackList::add)
                .verifyComplete();

            final List<String> expectedMetricFeedbackIdList = expectedMetricFeedbackList.stream()
                .map(MetricFeedback::getId)
                .collect(Collectors.toList());

            final List<MetricFeedback> actualList =
                actualMetricFeedbackList.stream().filter(metricFeedback  ->
                    expectedMetricFeedbackIdList.contains(metricFeedback .getId()))
                    .collect(Collectors.toList());

            // Assert
            assertEquals(inputMetricFeedbackList.size(), actualList.size());
            expectedMetricFeedbackList.sort(Comparator.comparing(metricFeedback -> metricFeedback.getFeedbackType().toString()));
            actualList.sort(Comparator.comparing(metricFeedback -> metricFeedback.getFeedbackType().toString()));
            final AtomicInteger i = new AtomicInteger(-1);
            final List<FeedbackType> metricFeedbackTypes = Arrays.asList(COMMENT, COMMENT);
            expectedMetricFeedbackList.forEach(expectedMetricFeedback ->
                validateMetricFeedbackResult(expectedMetricFeedback,
                actualList.get(i.incrementAndGet()), metricFeedbackTypes.get(i.get())));
        });
    }


    /**
     * Verifies the result of the list metric feedback  method to filter results using
     * {@link ListMetricFeedbackFilter#setDimensionFilter(DimensionKey)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListMetricFeedbackFilterByDimensionFilter(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        creatMetricFeedbackRunner(inputMetricFeedback -> {
            final MetricFeedback feedbackAdded = client.addFeedback(METRIC_ID, inputMetricFeedback
                .setDimensionFilter(new DimensionKey(DIMENSION_FILTER)))
                .block();
            final OffsetDateTime feedbackCreatedTime = feedbackAdded.getCreatedTime();

            // Act & Assert
            StepVerifier.create(client.listFeedback(METRIC_ID,
                new ListMetricFeedbackOptions().setFilter(new ListMetricFeedbackFilter()
                    .setTimeMode(FeedbackQueryTimeMode.FEEDBACK_CREATED_TIME)
                    .setStartTime(feedbackCreatedTime.minusDays(1))
                    .setEndTime(feedbackCreatedTime.plusDays(1))
                    .setDimensionFilter(new DimensionKey(DIMENSION_FILTER)))
                    .setMaxPageSize(10)))
                .thenConsumeWhile(metricFeedback ->
                    metricFeedback.getDimensionFilter().asMap().keySet().stream().anyMatch(DIMENSION_FILTER::containsKey))
                .verifyComplete();
        }, ANOMALY);

    }

    /**
     * Verifies the result of the list metric feedback  method to filter results using
     * {@link ListMetricFeedbackFilter#setFeedbackType(FeedbackType)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListMetricFeedbackFilterByFeedbackType(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        int[] count = new int[1];

        // Act & Assert
        StepVerifier.create(client.listFeedback(METRIC_ID,
            new ListMetricFeedbackOptions().setFilter(new ListMetricFeedbackFilter()
                .setFeedbackType(ANOMALY)))
            .take(LISTING_FILTER_BY_FEEDBACK_TYPE_LIMIT))
            .thenConsumeWhile(metricFeedback  -> {
                boolean matched = ANOMALY.equals(metricFeedback.getFeedbackType());
                if (matched) {
                    count[0]++;
                }
                return matched;
            })
            .verifyComplete();

        Assertions.assertTrue(count[0] > 0);
    }

    /**
     * Verifies the result of the list metric feedback  method to filter results using
     * {@link ListMetricFeedbackFilter#setStartTime(OffsetDateTime)}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListMetricFeedbackFilterStartTime(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        creatMetricFeedbackRunner(inputMetricFeedback -> {
            final MetricFeedback createdMetricFeedback = client.addFeedback(METRIC_ID, inputMetricFeedback).block();

            // Act & Assert
            StepVerifier.create(client.listFeedback(METRIC_ID,
                new ListMetricFeedbackOptions().setFilter(new ListMetricFeedbackFilter()
                    .setStartTime(createdMetricFeedback.getCreatedTime())
                    .setTimeMode(FeedbackQueryTimeMode.FEEDBACK_CREATED_TIME))))
                .thenConsumeWhile(metricFeedback ->
                    metricFeedback.getCreatedTime().isAfter(createdMetricFeedback.getCreatedTime())
                        || metricFeedback.getCreatedTime().isEqual(createdMetricFeedback.getCreatedTime()))
                .verifyComplete();

        }, ANOMALY);
    }

    // Get Feedback

    /**
     * Verifies that an exception is thrown for null metric feedback  Id parameter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getMetricFeedbackNullId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.getFeedback(null))
            .expectErrorMatches(throwable -> throwable instanceof NullPointerException
                && throwable.getMessage().equals("'feedbackId' is required."))
            .verify();
    }

    /**
     * Verifies that an exception is thrown for invalid metric feedback  Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getMetricFeedbackInvalidId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.getFeedback(INCORRECT_UUID))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INCORRECT_UUID_ERROR))
            .verify();
    }

    /**
     * Verifies metric feedback  info returned with response for a valid metric feedback  Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void getMetricFeedbackValidId(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        creatMetricFeedbackRunner(expectedMetricFeedback -> {
            // Act & Assert
            MetricFeedback createdMetricFeedback
                = client.addFeedback(METRIC_ID, expectedMetricFeedback).block();
            // Act & Assert
            StepVerifier.create(client.getFeedbackWithResponse(createdMetricFeedback.getId()))
                .assertNext(metricFeedbackResponse -> {
                    assertEquals(metricFeedbackResponse.getStatusCode(), HttpResponseStatus.OK.code());
                    validateMetricFeedbackResult(getCommentFeedback(), metricFeedbackResponse.getValue(), COMMENT);
                })
                .verifyComplete();
        }, COMMENT);
    }

    // Create metric feedback

    /**
     * Verifies valid comment metric feedback created for required metric feedback details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createCommentMetricFeedback(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        creatMetricFeedbackRunner(expectedMetricFeedback ->

            // Act & Assert
            StepVerifier.create(client.addFeedback(METRIC_ID, expectedMetricFeedback))
                .assertNext(createdMetricFeedback ->
                    validateMetricFeedbackResult(expectedMetricFeedback, createdMetricFeedback, COMMENT))
                .verifyComplete(), COMMENT);
    }

    /**
     * Verifies valid anomaly metric feedback created for required metric feedback details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createAnomalyFeedback(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        creatMetricFeedbackRunner(expectedMetricFeedback ->

            // Act & Assert
            StepVerifier.create(client.addFeedback(METRIC_ID, expectedMetricFeedback))
                .assertNext(createdMetricFeedback ->
                    validateMetricFeedbackResult(expectedMetricFeedback, createdMetricFeedback, ANOMALY))
                .verifyComplete(), ANOMALY);
    }

    /**
     * Verifies valid period metric feedback created for required metric feedback details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createPeriodMetricFeedback(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        creatMetricFeedbackRunner(expectedMetricFeedback ->

            // Act & Assert
            StepVerifier.create(client.addFeedback(METRIC_ID, expectedMetricFeedback))
                .assertNext(createdMetricFeedback ->
                    validateMetricFeedbackResult(expectedMetricFeedback, createdMetricFeedback, PERIOD))
                .verifyComplete(), PERIOD);
    }

    /**
     * Verifies valid change point metric feedback created for required metric feedback details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    public void createChangePointMetricFeedback(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        // Arrange
        client = getMetricsAdvisorBuilder(httpClient, serviceVersion).buildAsyncClient();
        creatMetricFeedbackRunner(expectedMetricFeedback ->
            // Act & Assert
            StepVerifier.create(client.addFeedback(METRIC_ID, expectedMetricFeedback))
                .assertNext(createdMetricFeedback -> validateMetricFeedbackResult(expectedMetricFeedback, createdMetricFeedback, CHANGE_POINT))
                .verifyComplete(), CHANGE_POINT);
    }
}
