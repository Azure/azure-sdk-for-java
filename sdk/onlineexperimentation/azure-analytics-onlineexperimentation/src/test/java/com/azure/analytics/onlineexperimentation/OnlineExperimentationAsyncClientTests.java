// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.*;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.RequestConditions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class OnlineExperimentationAsyncClientTests extends OnlineExperimentationClientTestBase {
    protected OnlineExperimentationAsyncClient onlineExperimentationAsyncClient;

    @Override
    protected void beforeTest() {
        onlineExperimentationAsyncClient = getExperimentationClientBuilder().buildAsyncClient();
    }

    private Mono<ExperimentMetric> createTestMetricAsync(String metricId, String displayName, String description) {
        ExperimentMetric metricDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName(displayName != null ? displayName : "Test Metric " + metricId)
            .setDescription(
                description != null ? description : "A metric created for testing purposes (" + metricId + ")")
            .setCategories(Collections.singletonList("Test"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("TestEvent")));

        return onlineExperimentationAsyncClient.createOrUpdateMetric(metricId, metricDefinition);
    }

    // CREATE operations
    @Test
    public void createExperimentMetric() {
        String metricId = "test_metric_create_or_update_async";
        ExperimentMetric metricDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("New Test Metric Async")
            .setDescription("A metric created for testing purposes async")
            .setCategories(Collections.singletonList("Test"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("TestEvent")));

        StepVerifier.create(onlineExperimentationAsyncClient.createOrUpdateMetric(metricId, metricDefinition))
            .assertNext(response -> {
                Assertions.assertNotNull(response);
                Assertions.assertEquals(metricId, response.getId());
                Assertions.assertEquals(metricDefinition.getDisplayName(), response.getDisplayName());
                Assertions.assertEquals(metricDefinition.getDescription(), response.getDescription());
            })
            .verifyComplete();
    }

    @Test
    public void createMetricOnlyIfNotExists() {
        String metricId = "test_metric_create_if_not_exists_async";

        // Clean up any existing metric first
        onlineExperimentationAsyncClient.deleteMetric(metricId).onErrorComplete(HttpResponseException.class).block();

        ExperimentMetric metricDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("If-None-Match Test Metric Async")
            .setDescription("A metric created with If-None-Match header async")
            .setCategories(Arrays.asList("Test", "Conditional"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(
                new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("ConditionalCreateEvent")));

        StepVerifier.create(onlineExperimentationAsyncClient.createOrUpdateMetric(metricId, metricDefinition,
            new RequestConditions().setIfNoneMatch("*"))).assertNext(responseValue -> {
                Assertions.assertNotNull(responseValue);
                Assertions.assertEquals(metricId, responseValue.getId());
                Assertions.assertEquals(metricDefinition.getDisplayName(), responseValue.getDisplayName());
            }).verifyComplete();

        ExperimentMetric updatedDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("This should not be updated")
            .setDescription(metricDefinition.getDescription())
            .setCategories(metricDefinition.getCategories())
            .setDesiredDirection(metricDefinition.getDesiredDirection())
            .setDefinition(metricDefinition.getDefinition());

        StepVerifier.create(onlineExperimentationAsyncClient.createOrUpdateMetric(metricId, updatedDefinition,
            new RequestConditions().setIfNoneMatch("*"))).expectError(HttpResponseException.class).verify();
    }

    @Test
    public void updateExistingExperimentMetric() {
        String metricId = "test_metric_update_existing_async";

        StepVerifier.create(createTestMetricAsync(metricId, "Original Metric Async", "Initial description async")
            .flatMap(originalMetric -> {
                ExperimentMetric updatedDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
                    .setDisplayName("Updated Metric Async")
                    .setDescription("This metric has been updated async")
                    .setCategories(Arrays.asList("Test", "Updated"))
                    .setDesiredDirection(DesiredDirection.INCREASE)
                    .setDefinition(new EventCountMetricDefinition()
                        .setEvent(new ObservedEvent().setEventName("UpdatedTestEvent")));

                return onlineExperimentationAsyncClient.createOrUpdateMetric(metricId, updatedDefinition);
            })).assertNext(updatedMetric -> {
                Assertions.assertNotNull(updatedMetric);
                Assertions.assertEquals(metricId, updatedMetric.getId());
                Assertions.assertEquals("Updated Metric Async", updatedMetric.getDisplayName());
                Assertions.assertEquals("This metric has been updated async", updatedMetric.getDescription());
            }).verifyComplete();
    }

    @Test
    public void createOrUpdateMetricInvalidInputRejected() {
        String metricId = "test_metric_async_";
        ExperimentMetric invalidDefinition
            = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE).setDisplayName("Invalid Metric");

        StepVerifier.create(onlineExperimentationAsyncClient.createOrUpdateMetric(metricId, invalidDefinition))
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof HttpResponseException);
                HttpResponseException exception = (HttpResponseException) error;
                Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, exception.getResponse().getStatusCode());
            })
            .verify();
    }

    @Test
    public void updateMetricConditionallyWithIfMatch() {
        String metricId = "test_metric_conditional_update_async";

        StepVerifier.create(createTestMetricAsync(metricId, "Original Metric Async", "Initial description async")
            .flatMap(originalMetric -> {
                ExperimentMetric updatedDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
                    .setDisplayName("Updated With ETag Async")
                    .setDescription("This metric has been updated with ETag condition async")
                    .setCategories(Arrays.asList("Test", "Conditional"))
                    .setDesiredDirection(DesiredDirection.INCREASE)
                    .setDefinition(new EventCountMetricDefinition()
                        .setEvent(new ObservedEvent().setEventName("ConditionalUpdateEvent")));

                return onlineExperimentationAsyncClient.createOrUpdateMetric(metricId, updatedDefinition,
                    new RequestConditions().setIfMatch(originalMetric.getETag()));
            })).assertNext(resultMetric -> {
                Assertions.assertNotNull(resultMetric);
                Assertions.assertEquals(metricId, resultMetric.getId());
                Assertions.assertEquals("Updated With ETag Async", resultMetric.getDisplayName());
            }).verifyComplete();
    }

    @Test
    public void updateMetricPartially() {
        String metricId = "test_metric_partial_update_async";

        StepVerifier.create(createTestMetricAsync(metricId, "Original Metric Async", "Initial description async")
            .flatMap(originalMetric -> {
                ExperimentMetric updatedDefinition = new ExperimentMetric().setDisplayName("Updated Metric Async")
                    .setDescription("This metric has been updated with ETag condition async");

                return onlineExperimentationAsyncClient.createOrUpdateMetric(metricId, updatedDefinition);
            })).assertNext(resultMetric -> {
                Assertions.assertNotNull(resultMetric);
                Assertions.assertEquals(metricId, resultMetric.getId());
                Assertions.assertEquals("Updated Metric Async", resultMetric.getDisplayName());
            }).verifyComplete();
    }

    @Test
    public void updateMetricIfMatchPreconditionFailed() {
        String metricId = "test_metric_if_match_fail_async";

        StepVerifier.create(createTestMetricAsync(metricId, "Original Metric Async", "Initial description async")
            .flatMap(originalMetric -> {
                ExperimentMetric updatedDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
                    .setDisplayName("This Should Not Update Async")
                    .setDescription("This update should fail due to ETag mismatch async")
                    .setCategories(Collections.singletonList("Test"))
                    .setDesiredDirection(DesiredDirection.INCREASE)
                    .setDefinition(
                        new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("TestEvent")));

                return onlineExperimentationAsyncClient.createOrUpdateMetric(metricId, updatedDefinition,
                    new RequestConditions().setIfMatch("incorrect-etag-value"));
            })).expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof HttpResponseException);
                HttpResponseException exception = (HttpResponseException) error;
                Assertions.assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exception.getResponse().getStatusCode());
            }).verify();
    }

    // READ operations
    @Test
    public void listMetrics() {
        final int numMetrics = 3;
        for (int i = 0; i < numMetrics; i++) {
            createTestMetricAsync("test_metric_list_async_" + i, "Metric Async " + i,
                "Test metric for listing async #" + i).block();
        }

        StepVerifier.create(onlineExperimentationAsyncClient.listMetrics().collectList()).assertNext(list -> {
            Assertions.assertTrue(list.size() >= numMetrics);
        }).verifyComplete();
    }

    @Test
    public void listMetricsWithTopParameter() {
        final int numMetrics = 5;
        for (int i = 0; i < numMetrics; i++) {
            createTestMetricAsync("test_metric_list_top_async_" + i, "Metric Async " + i,
                "Test metric for testing top parameter async #" + i).block();
        }

        StepVerifier.create(onlineExperimentationAsyncClient.listMetrics(2, null).collectList()).assertNext(metrics -> {
            Assertions.assertTrue(metrics.size() > numMetrics);
        }).verifyComplete();
    }

    @Test
    public void getMetricById() {
        String metricId = "test_metric_retrieve_async";

        final ExperimentMetric createdMetric
            = createTestMetricAsync(metricId, "Get Test Metric Async", "A metric to be retrieved async").block();

        StepVerifier.create(onlineExperimentationAsyncClient.getMetric(metricId)).assertNext(retrievedMetric -> {
            Assertions.assertNotNull(retrievedMetric);
            Assertions.assertEquals(metricId, retrievedMetric.getId());
            Assertions.assertEquals(createdMetric.getDisplayName(), retrievedMetric.getDisplayName());
            Assertions.assertEquals(createdMetric.getDescription(), retrievedMetric.getDescription());
            Assertions.assertEquals(createdMetric.getETag(), retrievedMetric.getETag());
        }).verifyComplete();
    }

    @Test
    public void returnErrorWhenRetrievingNonExistentMetric() {
        String nonExistentMetricId = "test_metric_does_not_exist_async";

        StepVerifier.create(onlineExperimentationAsyncClient.getMetric(nonExistentMetricId))
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof HttpResponseException);
                HttpResponseException exception = (HttpResponseException) error;
                Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, exception.getResponse().getStatusCode());
            })
            .verify();
    }

    // VALIDATE operations
    @Test
    public void validateValidExperimentMetric() {
        ExperimentMetric validDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("Valid Metric Async")
            .setDescription("A valid metric for validation testing async")
            .setCategories(Arrays.asList("Test", "Validation"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(
                new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("ValidationEvent")));

        StepVerifier.create(onlineExperimentationAsyncClient.validateMetric(validDefinition)).assertNext(result -> {
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.isValid());
            Assertions.assertTrue(result.getDiagnostics() == null || result.getDiagnostics().isEmpty());
        }).verifyComplete();
    }

    @Test
    public void returnDiagnosticsForInvalidExperimentMetric() {
        EventCountMetricDefinition invalidMetricDefinition = new EventCountMetricDefinition().setEvent(
            new ObservedEvent().setEventName("ValidationEvent").setFilter("this is not a valid filter expression."));

        ExperimentMetric invalidMetric = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("Invalid Metric Async")
            .setDescription("An invalid metric for validation testing async")
            .setCategories(Collections.singletonList("Test"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(invalidMetricDefinition);

        StepVerifier.create(onlineExperimentationAsyncClient.validateMetric(invalidMetric)).assertNext(result -> {
            Assertions.assertNotNull(result);
            Assertions.assertFalse(result.isValid());
            Assertions.assertNotNull(result.getDiagnostics());
            Assertions.assertFalse(result.getDiagnostics().isEmpty());
        }).verifyComplete();
    }

    // DELETE operations
    @Test
    public void deleteMetric() {
        // Create a test metric that will be deleted
        String metricId = "test_metric_delete_async";

        StepVerifier.create(createTestMetricAsync(metricId, "Delete Test Metric Async", "A metric to be deleted async")
            .flatMap(metric -> onlineExperimentationAsyncClient.deleteMetric(metricId))
            .then(onlineExperimentationAsyncClient.getMetric(metricId))).expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof HttpResponseException);
                HttpResponseException exception = (HttpResponseException) error;
                Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, exception.getResponse().getStatusCode());
            }).verify();
    }

    @Test
    public void deleteMetricWithETag() {
        // Create a test metric and capture its ETag for conditional deletion
        String metricId = "test_metric_delete_etag_async";

        StepVerifier.create(createTestMetricAsync(metricId, "Conditional Delete Metric Async",
            "A metric to be conditionally deleted async")
                .flatMap(createdMetric -> onlineExperimentationAsyncClient.deleteMetric(metricId,
                    new RequestConditions().setIfMatch(createdMetric.getETag())))
                .then(onlineExperimentationAsyncClient.getMetric(metricId)))
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof HttpResponseException);
                HttpResponseException exception = (HttpResponseException) error;
                Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, exception.getResponse().getStatusCode());
            })
            .verify();
    }

    @Test
    public void deleteMetricPreconditionFailed() {
        // Create a test metric
        String metricId = "test_metric_delete_fail_async";

        StepVerifier.create(
            // First create the metric
            createTestMetricAsync(metricId, "Conditional Delete Fail Metric Async",
                "A metric with incorrect ETag for conditional delete async")
                    // Then attempt to delete with incorrect ETag and handle the expected error
                    .flatMap(createdMetric -> onlineExperimentationAsyncClient
                        .deleteMetric(metricId, new RequestConditions().setIfMatch("incorrect-etag-value"))
                        .onErrorResume(error -> {
                            // Verify error is of expected type and status code
                            Assertions.assertTrue(error instanceof HttpResponseException);
                            HttpResponseException exception = (HttpResponseException) error;
                            Assertions.assertEquals(HttpURLConnection.HTTP_PRECON_FAILED,
                                exception.getResponse().getStatusCode());
                            return Mono.empty();
                        }))
                    // Finally verify the metric still exists
                    .then(onlineExperimentationAsyncClient.getMetric(metricId)))
            .assertNext(stillExists -> {
                Assertions.assertNotNull(stillExists);
                Assertions.assertEquals(metricId, stillExists.getId());
            })
            .verifyComplete();
    }
}
