// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.*;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.RequestConditions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OnlineExperimentationClientTests extends OnlineExperimentationClientTestBase {
    protected OnlineExperimentationClient onlineExperimentationClient;

    @Override
    protected void beforeTest() {
        onlineExperimentationClient = getExperimentationClientBuilder().buildClient();
    }

    private ExperimentMetric createTestMetric(String metricId, String displayName, String description) {
        ExperimentMetric metricDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName(displayName != null ? displayName : "Test Metric " + metricId)
            .setDescription(
                description != null ? description : "A metric created for testing purposes (" + metricId + ")")
            .setCategories(Collections.singletonList("Test"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("TestEvent")));

        return onlineExperimentationClient.createOrUpdateMetric(metricId, metricDefinition);
    }

    // CREATE operations
    @Test
    public void createExperimentMetric() {
        String metricId = "test_metric_create_or_update";
        ExperimentMetric metricDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("New Test Metric")
            .setDescription("A metric created for testing purposes")
            .setCategories(Collections.singletonList("Test"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("TestEvent")));

        ExperimentMetric response = onlineExperimentationClient.createOrUpdateMetric(metricId, metricDefinition);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(metricId, response.getId());
        Assertions.assertEquals(metricDefinition.getDisplayName(), response.getDisplayName());
        Assertions.assertEquals(metricDefinition.getDescription(), response.getDescription());
    }

    @Test
    public void createMetricOnlyIfNotExists() {
        String metricId = "test_metric_create_if_not_exists";

        try {
            onlineExperimentationClient.deleteMetric(metricId);
        } catch (HttpResponseException ex) {
        }

        ExperimentMetric metricDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("If-None-Match Test Metric")
            .setDescription("A metric created with If-None-Match header")
            .setCategories(Arrays.asList("Test", "Conditional"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(
                new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("ConditionalCreateEvent")));

        ExperimentMetric responseValue = onlineExperimentationClient.createOrUpdateMetric(metricId, metricDefinition,
            new RequestConditions().setIfNoneMatch("*"));

        Assertions.assertNotNull(responseValue);
        Assertions.assertEquals(metricId, responseValue.getId());
        Assertions.assertEquals(metricDefinition.getDisplayName(), responseValue.getDisplayName());

        ExperimentMetric updatedDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("This should not be updated")
            .setDescription(metricDefinition.getDescription())
            .setCategories(metricDefinition.getCategories())
            .setDesiredDirection(metricDefinition.getDesiredDirection())
            .setDefinition(metricDefinition.getDefinition());

        Assertions.assertThrows(HttpResponseException.class, () -> onlineExperimentationClient
            .createOrUpdateMetric(metricId, updatedDefinition, new RequestConditions().setIfNoneMatch("*")));
    }

    @Test
    public void updateExistingExperimentMetric() {
        String metricId = "test_metric_update_existing";
        ExperimentMetric originalMetric = createTestMetric(metricId, "Original Metric", "Initial description");

        ExperimentMetric updatedDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("Updated Metric")
            .setDescription("This metric has been updated")
            .setCategories(Arrays.asList("Test", "Updated"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(
                new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("UpdatedTestEvent")));

        ExperimentMetric updatedMetric = onlineExperimentationClient.createOrUpdateMetric(metricId, updatedDefinition);

        Assertions.assertNotNull(updatedMetric);
        Assertions.assertEquals(metricId, updatedMetric.getId());
        Assertions.assertEquals(updatedDefinition.getDisplayName(), updatedMetric.getDisplayName());
        Assertions.assertEquals(updatedDefinition.getDescription(), updatedMetric.getDescription());
        Assertions.assertNotEquals(originalMetric.getETag(), updatedMetric.getETag());
    }

    @Test
    public void createOrUpdateMetricInvalidInputRejected() {
        String metricId = "test_metric_";
        ExperimentMetric invalidDefinition
            = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE).setDisplayName("Invalid Metric");

        HttpResponseException exception = Assertions.assertThrows(HttpResponseException.class,
            () -> onlineExperimentationClient.createOrUpdateMetric(metricId, invalidDefinition));

        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, exception.getResponse().getStatusCode());
    }

    @Test
    public void updateMetricConditionallyWithIfMatch() {
        String metricId = "test_metric_conditional_update";
        ExperimentMetric originalMetric = createTestMetric(metricId, "Original Metric", "Initial description");
        String originalETag = originalMetric.getETag();

        ExperimentMetric updatedDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("Updated With ETag")
            .setDescription("This metric has been updated with ETag condition")
            .setCategories(Arrays.asList("Test", "Conditional"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(
                new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("ConditionalUpdateEvent")));

        ExperimentMetric resultMetric = onlineExperimentationClient.createOrUpdateMetric(metricId, updatedDefinition,
            new RequestConditions().setIfMatch(originalETag));

        Assertions.assertNotNull(resultMetric);
        Assertions.assertEquals(metricId, resultMetric.getId());
        Assertions.assertEquals(updatedDefinition.getDisplayName(), resultMetric.getDisplayName());
        Assertions.assertNotEquals(originalETag, resultMetric.getETag());
    }

    @Test
    public void updateMetricPartially() {
        String metricId = "test_metric_partial_update";
        createTestMetric(metricId, "Original Metric", "Initial description");

        ExperimentMetric updatedDefinition = new ExperimentMetric().setDisplayName("Updated Metric")
            .setDescription("This metric has been updated with ETag condition");

        ExperimentMetric resultMetric = onlineExperimentationClient.createOrUpdateMetric(metricId, updatedDefinition);

        Assertions.assertNotNull(resultMetric);
        Assertions.assertEquals(metricId, resultMetric.getId());
        Assertions.assertEquals(updatedDefinition.getDisplayName(), resultMetric.getDisplayName());
    }

    @Test
    public void updateMetricIfMatchPreconditionFailed() {
        String metricId = "test_metric_if_match_fail";
        createTestMetric(metricId, "Original Metric", "Initial description");

        ExperimentMetric updatedDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("This Should Not Update")
            .setDescription("This update should fail due to ETag mismatch")
            .setCategories(Collections.singletonList("Test"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("TestEvent")));

        HttpResponseException exception = Assertions.assertThrows(HttpResponseException.class,
            () -> onlineExperimentationClient.createOrUpdateMetric(metricId, updatedDefinition,
                new RequestConditions().setIfMatch("incorrect-etag-value")));

        Assertions.assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exception.getResponse().getStatusCode());
    }

    // READ operations
    @Test
    public void listMetrics() {
        final int numMetrics = 3;

        for (int i = 0; i < numMetrics; i++) {
            createTestMetric("test_metric_list_" + i, "Metric " + i, "Test metric for listing #" + i);
        }

        List<ExperimentMetric> metrics = new ArrayList<>();
        onlineExperimentationClient.listMetrics().forEach(metrics::add);

        Assertions.assertTrue(metrics.size() >= numMetrics);
    }

    @Test
    public void listMetricsWithTopParameter() {
        final int numMetrics = 5;

        for (int i = 0; i < numMetrics; i++) {
            createTestMetric("test_metric_list_" + i, "Metric " + i, "Test metric for testing top parameter #" + i);
        }

        List<ExperimentMetric> metrics = new ArrayList<>();
        onlineExperimentationClient.listMetrics(2, null).forEach(metrics::add);

        Assertions.assertTrue(metrics.size() > numMetrics);
    }

    @Test
    public void getMetricById() {
        String metricId = "test_metric_retrieve";
        ExperimentMetric createdMetric = createTestMetric(metricId, "Get Test Metric", "A metric to be retrieved");

        ExperimentMetric retrievedMetric = onlineExperimentationClient.getMetric(metricId);

        Assertions.assertNotNull(retrievedMetric);
        Assertions.assertEquals(metricId, retrievedMetric.getId());
        Assertions.assertEquals(createdMetric.getDisplayName(), retrievedMetric.getDisplayName());
        Assertions.assertEquals(createdMetric.getDescription(), retrievedMetric.getDescription());
        Assertions.assertEquals(createdMetric.getETag(), retrievedMetric.getETag());
    }

    @Test
    public void returnErrorWhenRetrievingNonExistentMetric() {
        String nonExistentMetricId = "test_metric_does_not_exist";

        HttpResponseException exception = Assertions.assertThrows(HttpResponseException.class,
            () -> onlineExperimentationClient.getMetric(nonExistentMetricId));

        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, exception.getResponse().getStatusCode());
    }

    // VALIDATE operations
    @Test
    public void validateValidExperimentMetric() {
        ExperimentMetric validDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("Valid Metric")
            .setDescription("A valid metric for validation testing")
            .setCategories(Arrays.asList("Test", "Validation"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(
                new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("ValidationEvent")));

        ExperimentMetricValidationResult result = onlineExperimentationClient.validateMetric(validDefinition);

        result.getDiagnostics()
            .forEach(diagnostic -> System.out
                .println(String.format("- %s: %s", diagnostic.getCode(), diagnostic.getMessage())));

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isValid());
        Assertions.assertTrue(result.getDiagnostics() == null || result.getDiagnostics().isEmpty());
    }

    @Test
    public void returnDiagnosticsForInvalidExperimentMetric() {
        EventCountMetricDefinition invalidMetricDefinition = new EventCountMetricDefinition().setEvent(
            new ObservedEvent().setEventName("ValidationEvent").setFilter("this is not a valid filter expression."));

        ExperimentMetric invalidMetric = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("Invalid Metric")
            .setDescription("An invalid metric for validation testing")
            .setCategories(Collections.singletonList("Test"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(invalidMetricDefinition);

        ExperimentMetricValidationResult result = onlineExperimentationClient.validateMetric(invalidMetric);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isValid());
        Assertions.assertNotNull(result.getDiagnostics());
        Assertions.assertFalse(result.getDiagnostics().isEmpty());
    }

    // DELETE operations
    @Test
    public void deleteMetric() {
        // Create a test metric that will be deleted
        String metricId = "test_metric_delete";
        createTestMetric(metricId, "Delete Test Metric", "A metric to be deleted");

        // Delete the metric
        onlineExperimentationClient.deleteMetric(metricId);

        // Verify the metric was deleted
        HttpResponseException exception = Assertions.assertThrows(HttpResponseException.class,
            () -> onlineExperimentationClient.getMetric(metricId));

        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, exception.getResponse().getStatusCode());
    }

    @Test
    public void deleteMetricWithETag() {
        // Create a test metric and capture its ETag for conditional deletion
        String metricId = "test_metric_delete_etag";
        ExperimentMetric createdMetric
            = createTestMetric(metricId, "Conditional Delete Metric", "A metric to be conditionally deleted");
        String etag = createdMetric.getETag();

        // Delete the metric using ETag condition
        onlineExperimentationClient.deleteMetric(metricId, new RequestConditions().setIfMatch(etag));

        // Verify the metric was deleted
        HttpResponseException exception = Assertions.assertThrows(HttpResponseException.class,
            () -> onlineExperimentationClient.getMetric(metricId));

        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, exception.getResponse().getStatusCode());
    }

    @Test
    public void deleteMetricPreconditionFailed() {
        // Create a test metric
        String metricId = "test_metric_delete_fail";
        createTestMetric(metricId, "Conditional Delete Fail Metric",
            "A metric with incorrect ETag for conditional delete");

        // Attempt to delete with incorrect ETag should fail
        HttpResponseException exception
            = Assertions.assertThrows(HttpResponseException.class, () -> onlineExperimentationClient
                .deleteMetric(metricId, new RequestConditions().setIfMatch("incorrect-etag-value")));

        Assertions.assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exception.getResponse().getStatusCode());

        // Verify metric still exists
        ExperimentMetric stillExists = onlineExperimentationClient.getMetric(metricId);
        Assertions.assertNotNull(stillExists);
        Assertions.assertEquals(metricId, stillExists.getId());
    }
}
