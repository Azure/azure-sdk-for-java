// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("unchecked")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestRunTests extends LoadTestingClientTestBase {

    // Helpers

    private Map<String, Object> getTestRunBodyFromDict(String testId) {
        Map<String, Object> testRunMap = new HashMap<>();
        testRunMap.put("testId", testId);
        testRunMap.put("displayName", "Java SDK Sample Test Run");
        testRunMap.put("description", "Sample Test Run");

        return testRunMap;
    }

    // Puts and Patches

    @Test
    @Order(1)
    public void beginTestRun() {
        BinaryData body = BinaryData.fromObject(getTestRunBodyFromDict(existingTestId));
        SyncPoller<BinaryData, BinaryData> poller = getLoadTestRunClient().beginTestRun(newTestRunId, body, null);
        poller = setPlaybackSyncPollerPollInterval(poller);
        PollResponse<BinaryData> response = poller.waitForCompletion();
        BinaryData testRunBinary = poller.getFinalResult();

        try (JsonReader jsonReader = JsonProviders.createReader(testRunBinary.toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            assertEquals(newTestRunId, jsonTree.get("testRunId"));
            assertEquals("DONE", jsonTree.get("status"));
        } catch (IOException e) {
            fail("Encountered exception while reading test run data", e);
        }

        assertNotNull(response.getValue());
    }

    @Test
    @Order(2)
    public void createOrUpdateAppComponents() {
        BinaryData body = BinaryData.fromObject(getAppComponentBodyFromDict());
        Response<BinaryData> response = getLoadTestRunClient()
            .createOrUpdateAppComponentsWithResponse(newTestRunId, body, null);

        assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(3)
    public void createOrUpdateServerMetricsConfig() {
        BinaryData body = BinaryData.fromObject(getServerMetricsBodyFromDict());
        Response<BinaryData> response = getLoadTestRunClient()
            .createOrUpdateServerMetricsConfigWithResponse(newTestRunId, body, null);

        assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    // Gets

    @Test
    @Order(4)
    public void getTestRunFile() {
        Response<BinaryData> response = getLoadTestRunClient()
            .getTestRunFileWithResponse(newTestRunId, uploadJmxFileName, null);

        try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            assertEquals(uploadJmxFileName, jsonTree.get("fileName"));
            assertEquals("JMX_FILE", jsonTree.get("fileType"));
        } catch (IOException e) {
            fail("Encountered exception while reading test run file data", e);
        }

        assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(5)
    public void getTestRun() {
        Response<BinaryData> response = getLoadTestRunClient().getTestRunWithResponse(newTestRunId, null);

        try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            assertEquals(newTestRunId, jsonTree.get("testRunId"));
        } catch (IOException e) {
            fail("Encountered exception while reading test run data", e);
        }

        assertEquals(200, response.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(6)
    public void getAppComponents() {
        Response<BinaryData> response = getLoadTestRunClient().getAppComponentsWithResponse(newTestRunId, null);

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

        assertEquals(200, response.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(7)
    public void getServerMetricsConfig() {
        Response<BinaryData> response = getLoadTestRunClient().getServerMetricsConfigWithResponse(newTestRunId, null);

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

        assertEquals(200, response.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(8)
    public void listMetricNamespaces() {
        Response<BinaryData> response = getLoadTestRunClient().getMetricNamespacesWithResponse(newTestRunId, null);

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
    }

    @Test
    @Order(9)
    public void listMetricDefinitions() {
        Response<BinaryData> response = getLoadTestRunClient()
            .getMetricDefinitionsWithResponse(newTestRunId, "LoadTestRunMetrics", null);

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
    }

    @Test
    @Order(10)
    public void listMetrics() {
        String startDateTime = "", endDateTime = "";
        Response<BinaryData> response = getLoadTestRunClient().getTestRunWithResponse(newTestRunId, null);

        try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            startDateTime = jsonTree.get("startDateTime").toString();
            endDateTime = jsonTree.get("endDateTime").toString();
        } catch (IOException e) {
            fail("Encountered exception while reading metrics data", e);
        }

        String timespan = startDateTime + "/" + endDateTime;
        PagedIterable<BinaryData> metricsResponse = getLoadTestRunClient()
            .listMetrics(newTestRunId, "VirtualUsers", "LoadTestRunMetrics", timespan, null);
        boolean valid = metricsResponse.stream().anyMatch(metricsBinary -> {
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

        assertTrue(valid);
    }

    // Lists

    @Test
    @Order(11)
    public void listTestRuns() {
        RequestOptions reqOpts = new RequestOptions().addQueryParam("testId", existingTestId);
        PagedIterable<BinaryData> response = getLoadTestRunClient().listTestRuns(reqOpts);
        boolean found = response.stream().anyMatch(testRunBinary -> {
            try (JsonReader jsonReader = JsonProviders.createReader(testRunBinary.toBytes())) {
                Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                assertEquals(newTestRunId, jsonTree.get("testRunId"));
                assertEquals(existingTestId, jsonTree.get("testId"));

                return true;
            } catch (IOException e) {
                // no-op
            }

            return false;
        });

        assertTrue(found);
    }

    // Deletes

    @Test
    @Order(12)
    public void deleteTestRun() {
        assertDoesNotThrow(() -> {
            getLoadTestRunClient().deleteTestRunWithResponse(newTestRunId, null);
        });
    }
}
