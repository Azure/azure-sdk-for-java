// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.*;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.RequestConditions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Disabled
public class OnlineExperimentationClientProtocolTests extends OnlineExperimentationClientTestBase {

    private ExperimentMetric createExperimentMetric(String metricId, String displayName, String description) {
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
    public void createNewExperimentMetric() {
        String metricId = testResourceNamer.randomName("metric", 16);
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
    public void createExperimentMetricWithResponse() {
        String metricId = testResourceNamer.randomName("metric", 16);
        ExperimentMetric metricDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("Response Test Metric")
            .setDescription("A metric created with response testing")
            .setCategories(Collections.singletonList("Test"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("TestEvent")));

        Response<BinaryData> response = onlineExperimentationClient.createOrUpdateMetricWithResponse(metricId,
            BinaryData.fromObject(metricDefinition), new RequestOptions());

        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
        ExperimentMetric resultMetric = response.getValue().toObject(ExperimentMetric.class);
        Assertions.assertEquals(metricId, resultMetric.getId());
        Assertions.assertEquals(metricDefinition.getDisplayName(), resultMetric.getDisplayName());
    }

    @Test
    public void createMetricOnlyIfNotExists() {
        String metricId = testResourceNamer.randomName("metric", 16);

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

        Response<BinaryData> createResponse = onlineExperimentationClient.createOrUpdateMetricWithResponse(metricId,
            BinaryData.fromObject(metricDefinition), new RequestOptions().setHeader(HttpHeaderName.IF_NONE_MATCH, "*"));

        ExperimentMetric responseValue = createResponse.getValue().toObject(ExperimentMetric.class);
        Assertions.assertNotNull(responseValue);
        Assertions.assertEquals(metricId, responseValue.getId());
        Assertions.assertEquals(metricDefinition.getDisplayName(), responseValue.getDisplayName());

        ExperimentMetric updatedDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("This should not be updated")
            .setDescription(metricDefinition.getDescription())
            .setCategories(metricDefinition.getCategories())
            .setDesiredDirection(metricDefinition.getDesiredDirection())
            .setDefinition(metricDefinition.getDefinition());

        Assertions.assertThrows(HttpResponseException.class,
            () -> onlineExperimentationClient.createOrUpdateMetricWithResponse(metricId,
                BinaryData.fromObject(updatedDefinition),
                new RequestOptions().setHeader(HttpHeaderName.IF_NONE_MATCH, "*")));
    }

    @Test
    public void updateExistingExperimentMetric() {
        String metricId = testResourceNamer.randomName("metric", 16);
        ExperimentMetric originalMetric = createExperimentMetric(metricId, "Original Metric", "Initial description");

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
    public void rejectInvalidExperimentMetrics() {
        String metricId = testResourceNamer.randomName("metric", 16);
        // This is now missing required constructor parameters
        ExperimentMetric invalidDefinition
            = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE).setDisplayName("Invalid Metric");

        HttpResponseException exception = Assertions.assertThrows(HttpResponseException.class,
            () -> onlineExperimentationClient.createOrUpdateMetric(metricId, invalidDefinition));

        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, exception.getResponse().getStatusCode());
    }

    @Test
    public void updateMetricConditionallyWithIfMatch() {
        String metricId = testResourceNamer.randomName("metric", 16);
        ExperimentMetric originalMetric = createExperimentMetric(metricId, "Original Metric", "Initial description");
        String originalETag = originalMetric.getETag();

        ExperimentMetric updatedDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("Updated With ETag")
            .setDescription("This metric has been updated with ETag condition")
            .setCategories(Arrays.asList("Test", "Conditional"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(
                new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("ConditionalUpdateEvent")));

        Response<BinaryData> response = onlineExperimentationClient.createOrUpdateMetricWithResponse(metricId,
            BinaryData.fromObject(updatedDefinition),
            new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, originalETag));

        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
        ExperimentMetric resultMetric = response.getValue().toObject(ExperimentMetric.class);
        Assertions.assertNotNull(resultMetric);
        Assertions.assertEquals(metricId, resultMetric.getId());
        Assertions.assertEquals(updatedDefinition.getDisplayName(), resultMetric.getDisplayName());
        Assertions.assertNotEquals(originalETag, resultMetric.getETag());
    }

    @Test
    public void failToUpdateWhenIfMatchHeaderDoesNotMatch() {
        String metricId = testResourceNamer.randomName("metric", 16);
        createExperimentMetric(metricId, "Original Metric", "Initial description");
        String incorrectETag = "\"incorrect-etag-value\"";

        ExperimentMetric updatedDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("This Should Not Update")
            .setDescription("This update should fail due to ETag mismatch")
            .setCategories(Collections.singletonList("Test"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("TestEvent")));

        HttpResponseException exception
            = Assertions.assertThrows(HttpResponseException.class, () -> onlineExperimentationClient
                .createOrUpdateMetric(metricId, updatedDefinition, new RequestConditions().setIfMatch(incorrectETag)));

        Assertions.assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exception.getResponse().getStatusCode());
    }

    // READ operations
    @Test
    public void listAllExperimentMetrics() {
        String metricIdPrefix = testResourceNamer.randomName("metric", 16);
        int numMetrics = 3;
        List<ExperimentMetric> createdMetrics = new ArrayList<>();

        for (int i = 0; i < numMetrics; i++) {
            createdMetrics
                .add(createExperimentMetric(metricIdPrefix + "-" + i, "Metric " + i, "Test metric for listing #" + i));
        }

        List<ExperimentMetric> metrics = new ArrayList<>();
        onlineExperimentationClient.listMetrics().forEach(metrics::add);

        Assertions.assertTrue(metrics.size() >= numMetrics);
    }

    @Test
    public void limitExperimentMetricsUsingTopParameter() {
        String metricIdPrefix = testResourceNamer.randomName("metric", 16);
        int numMetrics = 5;
        int topCount = 2;

        for (int i = 0; i < numMetrics; i++) {
            createExperimentMetric(metricIdPrefix + "-" + i, "Metric " + i,
                "Test metric for testing top parameter #" + i);
        }

        List<ExperimentMetric> metrics = new ArrayList<>();
        onlineExperimentationClient.listMetrics(topCount, null).forEach(metrics::add);

        Assertions.assertEquals(topCount, metrics.size(), "Expected top parameter to limit results to " + topCount);
    }

    @Test
    public void retrieveSpecificExperimentMetric() {
        String metricId = testResourceNamer.randomName("metric", 16);
        ExperimentMetric createdMetric
            = createExperimentMetric(metricId, "Get Test Metric", "A metric to be retrieved");

        ExperimentMetric retrievedMetric = onlineExperimentationClient.getMetric(metricId);

        Assertions.assertNotNull(retrievedMetric);
        Assertions.assertEquals(metricId, retrievedMetric.getId());
        Assertions.assertEquals(createdMetric.getDisplayName(), retrievedMetric.getDisplayName());
        Assertions.assertEquals(createdMetric.getDescription(), retrievedMetric.getDescription());
        Assertions.assertEquals(createdMetric.getETag(), retrievedMetric.getETag());
    }

    @Test
    public void returnErrorWhenRetrievingNonExistentMetric() {
        String nonExistentMetricId = "non-existent-metric-" + UUID.randomUUID();

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

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isValid());
        Assertions.assertTrue(result.getDiagnostics() == null || result.getDiagnostics().isEmpty());
    }

    @Test
    public void returnDiagnosticsForInvalidExperimentMetric() {

        EventCountMetricDefinition invalidMetricDefinition
            = new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("ValidationEvent"));
        invalidMetricDefinition.getEvent().setFilter("this is not a valid filter expression.");

        ExperimentMetric invalidDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("Invalid Metric")
            .setDescription("An invalid metric for validation testing")
            .setCategories(Collections.singletonList("Test"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(invalidMetricDefinition);

        ExperimentMetricValidationResult result = onlineExperimentationClient.validateMetric(invalidDefinition);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isValid());
        Assertions.assertNotNull(result.getDiagnostics());
        Assertions.assertFalse(result.getDiagnostics().isEmpty());
    }

    // DELETE operations
    @Test
    public void deleteExperimentMetric() {
        String metricId = testResourceNamer.randomName("metric", 16);
        createExperimentMetric(metricId, "Delete Test Metric", "A metric to be deleted");

        onlineExperimentationClient.deleteMetric(metricId);

        HttpResponseException exception = Assertions.assertThrows(HttpResponseException.class,
            () -> onlineExperimentationClient.getMetric(metricId));

        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, exception.getResponse().getStatusCode());
    }

    @Test
    public void deleteMetricConditionallyWithIfMatch() {
        String metricId = testResourceNamer.randomName("metric", 16);
        ExperimentMetric createdMetric
            = createExperimentMetric(metricId, "Conditional Delete Metric", "A metric to be conditionally deleted");
        String etag = createdMetric.getETag();

        Response<Void> response = onlineExperimentationClient.deleteMetricWithResponse(metricId,
            new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, etag));

        Assertions.assertEquals(HttpURLConnection.HTTP_NO_CONTENT, response.getStatusCode());

        HttpResponseException exception = Assertions.assertThrows(HttpResponseException.class,
            () -> onlineExperimentationClient.getMetric(metricId));

        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, exception.getResponse().getStatusCode());
    }

    @Test
    public void failToDeleteWhenIfMatchHeaderDoesNotMatch() {
        String metricId = testResourceNamer.randomName("metric", 16);
        createExperimentMetric(metricId, "Conditional Delete Fail Metric",
            "A metric with incorrect ETag for conditional delete");
        String incorrectETag = "\"incorrect-etag-value\"";

        HttpResponseException exception = Assertions.assertThrows(HttpResponseException.class,
            () -> onlineExperimentationClient.deleteMetricWithResponse(metricId,
                new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, incorrectETag)));

        Assertions.assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exception.getResponse().getStatusCode());

        ExperimentMetric stillExists = onlineExperimentationClient.getMetric(metricId);
        Assertions.assertNotNull(stillExists);
        Assertions.assertEquals(metricId, stillExists.getId());
    }

    @Test
    public void addHeadersFromContextPolicyTest() {
        String metricId = testResourceNamer.randomName("metric", 16);
        ExperimentMetric metricDefinition = new ExperimentMetric().setLifecycle(LifecycleStage.ACTIVE)
            .setDisplayName("Headers Test Metric")
            .setDescription("Testing custom headers")
            .setCategories(Collections.singletonList("Test"))
            .setDesiredDirection(DesiredDirection.INCREASE)
            .setDefinition(new EventCountMetricDefinition().setEvent(new ObservedEvent().setEventName("TestEvent")));

        Response<BinaryData> response = onlineExperimentationClient.createOrUpdateMetricWithResponse(metricId,
            BinaryData.fromObject(metricDefinition), new RequestOptions());

        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
        ExperimentMetric resultMetric = response.getValue().toObject(ExperimentMetric.class);
        Assertions.assertEquals(metricId, resultMetric.getId());
    }
}
