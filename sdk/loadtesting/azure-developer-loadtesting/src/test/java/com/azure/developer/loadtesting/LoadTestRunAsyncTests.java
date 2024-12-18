// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestRunAsyncTests extends LoadTestingClientTestBase {

    // Helpers

    private Map<String, Object> getTestRunBodyFromDict(String testId) {
        Map<String, Object> testRunMap = new HashMap<>();
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
        StepVerifier.create(poller.takeUntil(pollResponse -> pollResponse.getStatus().isComplete())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .assertNext(testRunBinary -> {
                try (JsonReader jsonReader = JsonProviders.createReader(testRunBinary.toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                    assertEquals(newTestRunIdAsync, jsonTree.get("testRunId"));
                    assertEquals("DONE", jsonTree.get("status"));
                } catch (IOException e) {
                    fail("Encountered exception while reading test run data", e);
                }
            })
            .verifyComplete();
    }

    @Test
    @Order(2)
    public void createOrUpdateAppComponents() {
        BinaryData body = BinaryData.fromObject(getAppComponentBodyFromDict());
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
            .createOrUpdateAppComponentsWithResponse(newTestRunIdAsync, body, null);

        StepVerifier.create(monoResponse)
            .assertNext(response -> assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode())))
            .verifyComplete();
    }

    @Test
    @Order(3)
    public void createOrUpdateServerMetricsConfig() {
        BinaryData body = BinaryData.fromObject(getServerMetricsBodyFromDict());
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
            .createOrUpdateServerMetricsConfigWithResponse(newTestRunIdAsync, body, null);

        StepVerifier.create(monoResponse)
            .assertNext(response -> assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode())))
            .verifyComplete();
    }

    // Gets

    @Test
    @Order(4)
    public void getTestRunFile() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
            .getTestRunFileWithResponse(newTestRunIdAsync, uploadJmxFileName, null);

        StepVerifier.create(monoResponse)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());

                try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                    assertEquals(uploadJmxFileName, jsonTree.get("fileName"));
                    assertEquals("JMX_FILE", jsonTree.get("fileType"));
                } catch (IOException e) {
                    fail("Encountered exception while reading test run file data", e);
                }
            })
            .verifyComplete();
    }

    @Test
    @Order(5)
    public void getTestRun() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
            .getTestRunWithResponse(newTestRunIdAsync, null);

        StepVerifier.create(monoResponse)
            .assertNext(response -> {
                try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                    assertEquals(newTestRunIdAsync, jsonTree.get("testRunId"));
                } catch (IOException e) {
                    fail("Encountered exception while reading test run data", e);
                }

                assertEquals(200, response.getStatusCode());
            })
            .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(6)
    public void getAppComponents() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
            .getAppComponentsWithResponse(newTestRunIdAsync, null);

        StepVerifier.create(monoResponse).assertNext(response -> {
            assertEquals(200, response.getStatusCode());

            try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
                Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);
                Map<String, Object> components = (Map<String, Object>) jsonTree.get("components");

                assertTrue(components.containsKey(defaultAppComponentResourceId));

                Map<String, Object> appComponentResource =
                    (Map<String, Object>) components.get(defaultAppComponentResourceId);

                assertTrue(defaultAppComponentResourceId
                    .equalsIgnoreCase(appComponentResource.get("resourceId").toString()));
            } catch (IOException e) {
                fail("Encountered exception while reading app components", e);
            }
        }).verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(7)
    public void getServerMetricsConfig() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
            .getServerMetricsConfigWithResponse(newTestRunIdAsync, null);

        StepVerifier.create(monoResponse)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());

                try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);
                    Map<String, Object> components = (Map<String, Object>) jsonTree.get("metrics");

                    assertTrue(components.containsKey(defaultServerMetricId));

                    Map<String, Object> appComponentResource =
                        (Map<String, Object>) components.get(defaultServerMetricId);

                    assertTrue(defaultServerMetricId.equalsIgnoreCase(appComponentResource.get("id").toString()));
                } catch (IOException e) {
                    fail("Encountered exception while reading server metrics", e);
                }
            })
            .verifyComplete();
    }

    @SuppressWarnings({ "WriteOnlyObject", "unchecked" })
    @Test
    @Order(8)
    public void listMetricNamespaces() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
            .getMetricNamespacesWithResponse(newTestRunIdAsync, null);

        StepVerifier.create(monoResponse)
            .assertNext(response -> {
                try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);
                    List<Object> metricNamespaces = (List<Object>) jsonTree.get("value");
                    AtomicBoolean found = new AtomicBoolean(false);

                    metricNamespaces.forEach(namespace -> {
                        Map<String, Object> namespaceMap = (Map<String, Object>) namespace;

                        if (namespaceMap.get("name").equals("LoadTestRunMetrics")) {
                            found.set(true);
                        }
                    });

                    assertTrue(found.get());
                } catch (IOException e) {
                    fail("Encountered exception while reading metric namespaces", e);
                }
            })
            .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(9)
    public void listMetricDefinitions() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestRunAsyncClient()
            .getMetricDefinitionsWithResponse(newTestRunIdAsync, "LoadTestRunMetrics", null);

        StepVerifier.create(monoResponse).assertNext(response -> {
            try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
                Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);
                List<Object> metricDefinitions = (List<Object>) jsonTree.get("value");
                AtomicBoolean found = new AtomicBoolean(false);

                metricDefinitions.forEach(definition -> {
                    Map<String, Object> definitionMap = (Map<String, Object>) definition;

                    if (definitionMap.get("namespace").equals("LoadTestRunMetrics")
                        && definitionMap.get("name") != null
                        && definitionMap.containsKey("dimensions")) {

                        found.set(true);
                    }
                });

                assertTrue(found.get());
            } catch (IOException e) {
                fail("Encountered exception while reading metric definitions", e);
            }
        }).verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(10)
    public void listMetrics() {
        Mono<Boolean> monoBoolean = getLoadTestRunAsyncClient()
            .getTestRunWithResponse(newTestRunIdAsync, null)
            .flatMap(response -> {
                String startDateTime = "", endDateTime = "";

                try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                    startDateTime = jsonTree.get("startDateTime").toString();
                    endDateTime = jsonTree.get("endDateTime").toString();
                } catch (IOException e) {
                    fail("Encountered exception while reading metrics data", e);
                }

                String timespan = startDateTime + "/" + endDateTime;
                PagedFlux<BinaryData> metricsResponse = getLoadTestRunAsyncClient()
                    .listMetrics(newTestRunIdAsync, "VirtualUsers", "LoadTestRunMetrics", timespan, null);

                return metricsResponse.any(metricsBinary -> {
                    AtomicBoolean found = new AtomicBoolean(false);

                    try (JsonReader jsonReader = JsonProviders.createReader(metricsBinary.toBytes())) {
                        Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                        if (jsonTree.containsKey("data")) {
                            List<Object> data = (List<Object>) jsonTree.get("data");

                            data.forEach(metric -> {
                                Map<String, Object> metricMap = (Map<String, Object>) metric;

                                if (metricMap.containsKey("value")) {
                                    found.set(true);
                                }
                            });
                        }
                    } catch (IOException e) {
                        // no-op
                    }

                    return found.get();
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
        RequestOptions reqOpts = new RequestOptions().addQueryParam("testId", existingTestId);
        PagedFlux<BinaryData> response = getLoadTestRunAsyncClient().listTestRuns(reqOpts);

        StepVerifier.create(response)
            .expectNextMatches(testRunBinary -> {
                try (JsonReader jsonReader = JsonProviders.createReader(testRunBinary.toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                    assertEquals(newTestRunIdAsync, jsonTree.get("testRunId"));
                    assertEquals(existingTestId, jsonTree.get("testId"));

                    return true;
                } catch (IOException e) {
                    // no-op
                }

                return false;
            })
            .thenConsumeWhile(testRunBinary -> true)
            .verifyComplete();
    }

    // Deletes

    @Test
    @Order(12)
    public void deleteTestRun() {
        StepVerifier.create(getLoadTestRunAsyncClient().deleteTestRunWithResponse(newTestRunIdAsync, null))
            .expectNextCount(1)
            .verifyComplete();
    }
}
