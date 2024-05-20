// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestRunAsyncTests extends LoadTestingClientTestBase {

    // Helpers

    private Map<String, Object> getTestRunBodyFromDict(String testId) {
        Map<String, Object> testRunMap = new HashMap<String, Object>();
        testRunMap.put("testId", testId);
        testRunMap.put("displayName", "Java SDK Sample Test Run Async");
        testRunMap.put("description", "Sample Test Run Async");

        return testRunMap;
    }

    // Puts and Patches

    @Test
    @Order(1)
    public void beginTestRun() {
        BinaryData body = BinaryData.fromObject(getTestRunBodyFromDict(existingTestId));
        PollerFlux<BinaryData, BinaryData> poller = getLoadTestRunAsyncClient().beginTestRun(newTestRunIdAsync, body,
                null);
        poller = setPlaybackPollerFluxPollInterval(poller);
        StepVerifier.create(poller.takeUntil(pollResponse -> pollResponse.getStatus().isComplete()).last()
                .flatMap(AsyncPollResponse::getFinalResult)).assertNext(testRunBinary -> {
                    try {
                        JsonNode testRunNode = OBJECT_MAPPER.readTree(testRunBinary.toString());
                        Assertions.assertTrue(testRunNode.get("testRunId").asText().equals(newTestRunIdAsync)
                                && testRunNode.get("status").asText().equals("DONE"));
                    } catch (Exception e) {
                        Assertions.assertTrue(false);
                    }
                }).verifyComplete();
    }

    @Test
    @Order(2)
    public void createOrUpdateAppComponents() {
        BinaryData body = BinaryData.fromObject(getAppComponentBodyFromDict());
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient().createOrUpdateAppComponentsWithResponse(
                newTestRunIdAsync,
                body,
                null);
        StepVerifier.create(monoResponse).assertNext(response -> {
            Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
        }).verifyComplete();
    }

    @Test
    @Order(3)
    public void createOrUpdateServerMetricsConfig() {
        BinaryData body = BinaryData.fromObject(getServerMetricsBodyFromDict());
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
                .createOrUpdateServerMetricsConfigWithResponse(
                        newTestRunIdAsync,
                        body,
                        null);
        StepVerifier.create(monoResponse).assertNext(response -> {
            Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
        }).verifyComplete();
    }

    // Gets

    @Test
    @Order(4)
    public void getTestRunFile() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
                .getTestRunFileWithResponse(newTestRunIdAsync, uploadJmxFileName, null);
        StepVerifier.create(monoResponse).assertNext(response -> {
            try {
                JsonNode file = OBJECT_MAPPER.readTree(response.getValue().toString());
                Assertions.assertTrue(file.get("fileName").asText().equals(uploadJmxFileName)
                        && file.get("fileType").asText().equals("JMX_FILE"));
            } catch (Exception e) {
                Assertions.assertTrue(false);
            }
            Assertions.assertEquals(200, response.getStatusCode());
        }).verifyComplete();
    }

    @Test
    @Order(5)
    public void getTestRun() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient().getTestRunWithResponse(newTestRunIdAsync,
                null);
        StepVerifier.create(monoResponse).assertNext(response -> {
            try {
                JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());
                Assertions.assertTrue(test.get("testRunId").asText().equals(newTestRunIdAsync));
            } catch (Exception e) {
                Assertions.assertTrue(false);
            }
            Assertions.assertEquals(200, response.getStatusCode());
        }).verifyComplete();
    }

    @Test
    @Order(6)
    public void getAppComponents() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
                .getAppComponentsWithResponse(newTestRunIdAsync, null);
        StepVerifier.create(monoResponse).assertNext(response -> {
            try {
                JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());
                Assertions.assertTrue(test.get("components").has(defaultAppComponentResourceId)
                        && test.get("components").get(defaultAppComponentResourceId).get("resourceId").asText()
                                .equalsIgnoreCase(defaultAppComponentResourceId));
            } catch (Exception e) {
                Assertions.assertTrue(false);
            }
            Assertions.assertEquals(200, response.getStatusCode());
        }).verifyComplete();
    }

    @Test
    @Order(7)
    public void getServerMetricsConfig() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
                .getServerMetricsConfigWithResponse(newTestRunIdAsync, null);
        StepVerifier.create(monoResponse).assertNext(response -> {
            try {
                JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());
                Assertions.assertTrue(test.get("metrics").has(defaultServerMetricId) && test.get("metrics")
                        .get(defaultServerMetricId).get("id").asText().equalsIgnoreCase(defaultServerMetricId));
            } catch (Exception e) {
                e.printStackTrace();
                Assertions.assertTrue(false);
            }
            Assertions.assertEquals(200, response.getStatusCode());
        }).verifyComplete();
    }

    @Test
    @Order(8)
    public void listMetricNamespaces() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
                .getMetricNamespacesWithResponse(newTestRunIdAsync, null);
        StepVerifier.create(monoResponse).assertNext(response -> {
            try {
                Iterator<JsonNode> metricNamespacesIterator = OBJECT_MAPPER.readTree(response.getValue().toString())
                        .get("value").iterator();
                boolean found = false;
                while (metricNamespacesIterator.hasNext()) {
                    JsonNode namespace = metricNamespacesIterator.next();
                    if (namespace.get("name").asText().equals("LoadTestRunMetrics")) {
                        found = true;
                        break;
                    }
                }
                Assertions.assertTrue(found);
            } catch (Exception e) {
                Assertions.assertTrue(false);
            }
        }).verifyComplete();
    }

    @Test
    @Order(9)
    public void listMetricDefinitions() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
                .getMetricDefinitionsWithResponse(newTestRunIdAsync, "LoadTestRunMetrics", null);
        StepVerifier.create(monoResponse).assertNext(response -> {
            try {
                Iterator<JsonNode> metricDefinitionsIterator = OBJECT_MAPPER.readTree(response.getValue().toString())
                        .get("value").iterator();
                boolean found = false;
                while (metricDefinitionsIterator.hasNext()) {
                    JsonNode definition = metricDefinitionsIterator.next();
                    if (definition.get("namespace").asText().equals("LoadTestRunMetrics")
                            && definition.get("name").asText() != null && definition.has("dimensions")) {
                        found = true;
                        break;
                    }
                }
                Assertions.assertTrue(found);
            } catch (Exception e) {
                Assertions.assertTrue(false);
            }
        }).verifyComplete();
    }

    @Test
    @Order(10)
    public void listMetrics() {
        Mono<Boolean> monoBoolean = getLoadTestRunAsyncClient()
            .getTestRunWithResponse(newTestRunIdAsync, null)
            .flatMap(response -> {
                String startDateTime = "", endDateTime = "";
                try {
                    JsonNode testRun = OBJECT_MAPPER.readTree(response.getValue().toString());

                    startDateTime = testRun.get("startDateTime").asText();
                    endDateTime = testRun.get("endDateTime").asText();
                } catch (Exception e) {
                    fail();
                }

                String timespan = startDateTime + "/" + endDateTime;
                PagedFlux<BinaryData> metricsResponse = getLoadTestRunAsyncClient().listMetrics(newTestRunIdAsync,
                    "VirtualUsers", "LoadTestRunMetrics", timespan, null);

                return metricsResponse.any((metricsBinary) -> {
                    try {
                        JsonNode metric = OBJECT_MAPPER.readTree(metricsBinary.toString());
                        if (metric.has("data") && metric.get("data").get(0).has("value")) {
                            return true;
                        }
                    } catch (Exception e) {
                        // no-op
                    }
                    return false;
                });
            });

        StepVerifier.create(monoBoolean)
            .expectNext(true)
            .thenConsumeWhile(ignored -> true)
            .verifyComplete();
    }

    // Lists

    @Test
    @Order(11)
    public void listTestRuns() {
        RequestOptions reqOpts = new RequestOptions()
                .addQueryParam("testId", existingTestId);
        PagedFlux<BinaryData> response = getLoadTestRunAsyncClient().listTestRuns(reqOpts);
        StepVerifier.create(response).expectNextMatches(testRunBinary -> {
            try {
                JsonNode testRun = OBJECT_MAPPER.readTree(testRunBinary.toString());
                if (testRun.get("testRunId").asText().equals(newTestRunIdAsync)
                        && testRun.get("testId").asText().equals(existingTestId)) {
                    return true;
                }
            } catch (Exception e) {
                // no-op
            }
            return false;
        }).thenConsumeWhile(testRunBinary -> true).verifyComplete();
    }

    // Deletes

    @Test
    @Order(12)
    public void deleteTestRun() {
        StepVerifier.create(getLoadTestRunAsyncClient().deleteTestRunWithResponse(newTestRunIdAsync, null))
                .expectNextCount(1).verifyComplete();
    }
}
