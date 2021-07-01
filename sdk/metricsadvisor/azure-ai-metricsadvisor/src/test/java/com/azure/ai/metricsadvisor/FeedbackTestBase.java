// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyValue;
import com.azure.ai.metricsadvisor.models.ChangePointValue;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.FeedbackType;
import com.azure.ai.metricsadvisor.models.MetricAnomalyFeedback;
import com.azure.ai.metricsadvisor.models.MetricChangePointFeedback;
import com.azure.ai.metricsadvisor.models.MetricCommentFeedback;
import com.azure.ai.metricsadvisor.models.MetricFeedback;
import com.azure.ai.metricsadvisor.models.MetricPeriodFeedback;
import com.azure.ai.metricsadvisor.models.PeriodType;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static com.azure.ai.metricsadvisor.MetricsSeriesTestBase.SERIES_KEY_FILTER;
import static com.azure.ai.metricsadvisor.MetricsSeriesTestBase.TIME_SERIES_END_TIME;
import static com.azure.ai.metricsadvisor.MetricsSeriesTestBase.TIME_SERIES_START_TIME;
import static com.azure.ai.metricsadvisor.TestUtils.AZURE_METRICS_ADVISOR_ENDPOINT;
import static com.azure.ai.metricsadvisor.models.FeedbackType.ANOMALY;
import static com.azure.ai.metricsadvisor.models.FeedbackType.CHANGE_POINT;
import static com.azure.ai.metricsadvisor.models.FeedbackType.COMMENT;
import static com.azure.ai.metricsadvisor.models.FeedbackType.PERIOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class FeedbackTestBase extends MetricsAdvisorClientTestBase {

    static final OffsetDateTime FEEDBACK_START_TIME = TIME_SERIES_START_TIME;
    static final OffsetDateTime FEEDBACK_END_TIME = TIME_SERIES_END_TIME;
    static final HashMap<String, String> DIMENSION_FILTER = SERIES_KEY_FILTER;
    static final int LISTING_FILTER_BY_FEEDBACK_TYPE_LIMIT = 50;

    @Override
    protected void beforeTest() {
    }

    @Test
    abstract void createAnomalyFeedback(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void createChangePointMetricFeedback(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void createPeriodMetricFeedback(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void createCommentMetricFeedback(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testListMetricFeedback(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testListMetricFeedbackFilterStartTime(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testListMetricFeedbackFilterByFeedbackType(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion);

    @Test
    abstract void testListMetricFeedbackFilterByDimensionFilter(HttpClient httpClient,
        MetricsAdvisorServiceVersion serviceVersion);

    void listMetricFeedbackRunner(Consumer<List<MetricFeedback>> testRunner) {
        // create data feeds
        testRunner.accept(Arrays.asList(getCommentFeedback(), getCommentFeedback()));
    }

    MetricFeedback getCommentFeedback() {
        return new MetricCommentFeedback(FEEDBACK_START_TIME, FEEDBACK_END_TIME, "Not an anomaly.")
            .setDimensionFilter(new DimensionKey(DIMENSION_FILTER));
    }

    void creatMetricFeedbackRunner(Consumer<MetricFeedback> testRunner, FeedbackType metricFeedbackType) {
        // create data feeds
        MetricFeedback metricFeedback;
        if (ANOMALY.equals(metricFeedbackType)) {
            metricFeedback = new MetricAnomalyFeedback(FEEDBACK_START_TIME, FEEDBACK_END_TIME,
                AnomalyValue.NOT_ANOMALY);
        } else if (CHANGE_POINT.equals(metricFeedbackType)) {
            metricFeedback = new MetricChangePointFeedback(FEEDBACK_START_TIME, FEEDBACK_END_TIME,
                ChangePointValue.AUTO_DETECT);
        } else if (PERIOD.equals(metricFeedbackType)) {
            metricFeedback = new MetricPeriodFeedback(PeriodType.AUTO_DETECT, 3);
        } else if (COMMENT.equals(metricFeedbackType)) {
            metricFeedback = new MetricCommentFeedback(FEEDBACK_START_TIME, FEEDBACK_END_TIME, "Not an anomaly.");
        } else {
            throw new IllegalStateException("Unexpected value: " + metricFeedbackType.toString());
        }
        testRunner.accept(metricFeedback.setDimensionFilter(new DimensionKey(DIMENSION_FILTER)));
    }


    void validateMetricFeedbackResult(MetricFeedback expectedMetricFeedback, MetricFeedback actualMetricFeedback,
        FeedbackType feedbackType) {
        assertNotNull(actualMetricFeedback.getId());
        assertNotNull(actualMetricFeedback.getCreatedTime());
        assertNotNull(actualMetricFeedback.getMetricId());
        assertNotNull(actualMetricFeedback.getUserPrincipal());
        assertEquals(expectedMetricFeedback.getDimensionFilter(), actualMetricFeedback.getDimensionFilter());

        if (ANOMALY.equals(feedbackType)) {
            MetricAnomalyFeedback expectedAnomalyFeedback = (MetricAnomalyFeedback) expectedMetricFeedback;
            MetricAnomalyFeedback actualAnomalyFeedback = (MetricAnomalyFeedback) actualMetricFeedback;
            assertEquals(expectedAnomalyFeedback.getDetectionConfiguration(),
                actualAnomalyFeedback.getDetectionConfiguration());
            assertEquals(expectedAnomalyFeedback.getStartTime(), actualAnomalyFeedback.getStartTime());
            assertEquals(expectedAnomalyFeedback.getEndTime(), actualAnomalyFeedback.getEndTime());
        } else if (PERIOD.equals(feedbackType)) {
            MetricPeriodFeedback expectedPeriodFeedback = (MetricPeriodFeedback) expectedMetricFeedback;
            MetricPeriodFeedback actualPeriodFeedback = (MetricPeriodFeedback) actualMetricFeedback;
            assertEquals(expectedPeriodFeedback.getPeriodType(), actualPeriodFeedback.getPeriodType());
            assertEquals(expectedPeriodFeedback.getPeriodValue(), actualPeriodFeedback.getPeriodValue());
        } else if (CHANGE_POINT.equals(feedbackType)) {
            MetricChangePointFeedback expectedChangePointFeedback = (MetricChangePointFeedback) expectedMetricFeedback;
            MetricChangePointFeedback actualChangePointFeedback = (MetricChangePointFeedback) actualMetricFeedback;
            assertEquals(expectedChangePointFeedback.getChangePointValue().toString(),
                actualChangePointFeedback.getChangePointValue().toString());
            assertEquals(expectedChangePointFeedback.getStartTime(), actualChangePointFeedback.getStartTime());
            assertEquals(expectedChangePointFeedback.getEndTime(), actualChangePointFeedback.getEndTime());
        } else if (COMMENT.equals(feedbackType)) {
            MetricCommentFeedback expectedCommentFeedback = (MetricCommentFeedback) expectedMetricFeedback;
            MetricCommentFeedback actualChangePointFeedback = (MetricCommentFeedback) actualMetricFeedback;
            assertEquals(expectedCommentFeedback.getComment(),
                actualChangePointFeedback.getComment());
            assertEquals(expectedCommentFeedback.getStartTime(), actualChangePointFeedback.getStartTime());
            assertEquals(expectedCommentFeedback.getEndTime(), actualChangePointFeedback.getEndTime());
        } else {
            throw new IllegalStateException("Unexpected value: " + feedbackType);
        }
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_METRICS_ADVISOR_ENDPOINT);
    }
}
