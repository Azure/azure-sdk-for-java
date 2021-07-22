// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.ChangePointValue;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.MetricAnomalyFeedback;
import com.azure.ai.metricsadvisor.models.MetricChangePointFeedback;
import com.azure.ai.metricsadvisor.models.MetricCommentFeedback;
import com.azure.ai.metricsadvisor.models.MetricFeedback;
import com.azure.ai.metricsadvisor.models.MetricPeriodFeedback;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static com.azure.ai.metricsadvisor.FeedbackTestBase.DIMENSION_FILTER;
import static com.azure.ai.metricsadvisor.models.FeedbackType.ANOMALY;
import static com.azure.ai.metricsadvisor.models.FeedbackType.CHANGE_POINT;
import static com.azure.ai.metricsadvisor.models.FeedbackType.COMMENT;
import static com.azure.ai.metricsadvisor.models.FeedbackType.PERIOD;

/**
 * Async sample demonstrates how to create, get and list metric feedbacks.
 */
public class MetricFeedbackAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAsyncClient advisorAsyncClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildAsyncClient();

        // Create Metric Feedback
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final MetricChangePointFeedback metricChangePointFeedback
            = new MetricChangePointFeedback(startTime, endTime, ChangePointValue.AUTO_DETECT)
            .setDimensionFilter(new DimensionKey(DIMENSION_FILTER));

        System.out.printf("Creating Metric Feedback%n");
        final Mono<MetricFeedback> createdFeedbackMono
            = advisorAsyncClient.addFeedback(metricId, metricChangePointFeedback);

        createdFeedbackMono
            .doOnSubscribe(__ ->
                System.out.printf("Creating Metric Feedback%n"))
            .doOnSuccess(feedback ->
                System.out.printf("Created Metric Feedback: %s%n", feedback.getId()));

        // Retrieve the metric feedback that just created.
        Mono<MetricFeedback> fetchFeedbackMono =
            createdFeedbackMono.flatMap(createdFeedback -> {
                return advisorAsyncClient.getFeedback(createdFeedback.getId())
                    .doOnSubscribe(__ ->
                        System.out.printf("Fetching Metric Feedback: %s%n", createdFeedback.getId()))
                    .doOnSuccess(config ->
                        System.out.printf("Fetched Metric Feedback%n"))
                    .doOnNext(feedback -> {
                        System.out.printf("Metric Feedback Id : %s%n", feedback.getId());
                        System.out.printf("Metric Feedback created time : %s%n", feedback.getCreatedTime());
                        System.out.printf("Metric Feedback user principal : %s%n", feedback.getUserPrincipal());
                        System.out.printf("Metric feedback associated dimension filter: %s%n",
                            feedback.getDimensionFilter().asMap());

                        if (CHANGE_POINT.equals(createdFeedback.getFeedbackType())) {
                            MetricChangePointFeedback createdMetricChangePointFeedback
                                = (MetricChangePointFeedback) createdFeedback;
                            System.out.printf("Metric feedback Id: %s%n", createdMetricChangePointFeedback.getId());
                            System.out.printf("Metric feedback change point value: %s%n",
                                createdMetricChangePointFeedback.getChangePointValue().toString());
                            System.out.printf("Metric feedback start time: %s%n",
                                createdMetricChangePointFeedback.getStartTime());
                            System.out.printf("Metric feedback end time: %s%n",
                                createdMetricChangePointFeedback.getEndTime());
                        }
                    });
            });

        /*
          This will block until all the above CRUD on operation on email hook is completed.
          This is strongly discouraged for use in production as it eliminates the benefits
          of asynchronous IO. It is used here to ensure the sample runs to completion.
         */
        fetchFeedbackMono.block();

        // List metric feedbacks.
        System.out.printf("Listing metric feedbacks%n");
        advisorAsyncClient.listFeedback(metricId)
            .doOnNext(feedbackItem -> {
                System.out.printf("Metric Feedback Id : %s%n", feedbackItem.getId());
                System.out.printf("Metric Feedback created time : %s%n", feedbackItem.getCreatedTime());
                System.out.printf("Metric Feedback user principal : %s%n", feedbackItem.getUserPrincipal());
                System.out.printf("Metric feedback associated dimension filter: %s%n",
                    feedbackItem.getDimensionFilter().asMap());

                if (CHANGE_POINT.equals(feedbackItem.getFeedbackType())) {
                    MetricChangePointFeedback changePointFeedback
                        = (MetricChangePointFeedback) feedbackItem;
                    System.out.printf("Metric feedback change point value: %s%n",
                        changePointFeedback.getChangePointValue().toString());
                    System.out.printf("Metric feedback start time: %s%n",
                        changePointFeedback.getStartTime());
                    System.out.printf("Metric feedback end time: %s%n",
                        changePointFeedback.getEndTime());
                } else if (PERIOD.equals(feedbackItem.getFeedbackType())) {
                    MetricPeriodFeedback periodFeedback
                        = (MetricPeriodFeedback) feedbackItem;
                    System.out.printf("Metric feedback type: %s%n",
                        periodFeedback.getPeriodType().toString());
                    System.out.printf("Metric feedback period value: %d%n",
                        periodFeedback.getPeriodValue());
                } else if (ANOMALY.equals(feedbackItem.getFeedbackType())) {
                    MetricAnomalyFeedback metricAnomalyFeedback
                        = (MetricAnomalyFeedback) feedbackItem;
                    System.out.printf("Metric feedback anomaly value: %s%n",
                        metricAnomalyFeedback.getAnomalyValue().toString());
                    System.out.printf("Metric feedback associated detection configuration Id: %s%n",
                        metricAnomalyFeedback.getDetectionConfigurationId());
                } else if (COMMENT.equals(feedbackItem.getFeedbackType())) {
                    MetricCommentFeedback metricCommentFeedback
                        = (MetricCommentFeedback) feedbackItem;
                    System.out.printf("Metric feedback comment value: %s%n",
                        metricCommentFeedback.getComment());
                }
            });
    }
}
