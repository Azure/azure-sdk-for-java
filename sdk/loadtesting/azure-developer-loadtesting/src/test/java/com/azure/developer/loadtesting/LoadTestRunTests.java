// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestRunTests extends LoadTestingClientTestBase {

    // Helpers

    private Map<String, Object> getTestRunBodyFromDict(String testId) {
        Map<String, Object> testRunMap = new HashMap<String, Object>();
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
        SyncPoller<BinaryData, BinaryData> poller = testRunBuilder.buildClient().beginTestRun(newTestRunId, body, null);
        poller = setPlaybackSyncPollerPollInterval(poller);
        PollResponse<BinaryData> response = poller.waitForCompletion();
        BinaryData testRunBinary = poller.getFinalResult();
        try {
            JsonNode testRunNode = OBJECT_MAPPER.readTree(testRunBinary.toString());
            Assertions.assertTrue(testRunNode.get("testRunId").asText().equals(newTestRunId) && testRunNode.get("status").asText().equals("DONE"));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertNotNull(response.getValue().toString());
    }

    @Test
    @Order(2)
    public void createOrUpdateAppComponents() {
        BinaryData body = BinaryData.fromObject(getAppComponentBodyFromDict());
        Response<BinaryData> response = testRunBuilder.buildClient().createOrUpdateAppComponentsWithResponse(
                                                newTestRunId,
                                                body,
                                                null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(3)
    public void createOrUpdateServerMetricsConfig() {
        BinaryData body = BinaryData.fromObject(getServerMetricsBodyFromDict());
        Response<BinaryData> response = testRunBuilder.buildClient().createOrUpdateServerMetricsConfigWithResponse(
                                                newTestRunId,
                                                body,
                                                null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    // Gets

    @Test
    @Order(4)
    public void getTestRunFile() {
        Response<BinaryData> response = testRunBuilder.buildClient().getTestRunFileWithResponse(newTestRunId, uploadJmxFileName, null);
        try {
            JsonNode file = OBJECT_MAPPER.readTree(response.getValue().toString());
            Assertions.assertTrue(file.get("fileName").asText().equals(uploadJmxFileName) && file.get("fileType").asText().equals("JMX_FILE"));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(5)
    public void getTestRun() {
        Response<BinaryData> response = testRunBuilder.buildClient().getTestRunWithResponse(newTestRunId, null);
        try {
            JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());
            Assertions.assertTrue(test.get("testRunId").asText().equals(newTestRunId));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(6)
    public void getAppComponents() {
        Response<BinaryData> response = testRunBuilder.buildClient().getAppComponentsWithResponse(newTestRunId, null);
        try {
            JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());
            Assertions.assertTrue(test.get("components").has(defaultAppComponentResourceId) && test.get("components").get(defaultAppComponentResourceId).get("resourceId").asText().equalsIgnoreCase(defaultAppComponentResourceId));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(7)
    public void getServerMetricsConfig() {
        Response<BinaryData> response = testRunBuilder.buildClient().getServerMetricsConfigWithResponse(newTestRunId, null);
        try {
            JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());
            Assertions.assertTrue(test.get("metrics").has(defaultServerMetricId) && test.get("metrics").get(defaultServerMetricId).get("id").asText().equalsIgnoreCase(defaultServerMetricId));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void listMetricNamespaces() {
        Response<BinaryData> response = testRunBuilder.buildClient().getMetricNamespacesWithResponse(newTestRunId, null);
        try {
            Iterator<JsonNode> metricNamespacesIterator = OBJECT_MAPPER.readTree(response.getValue().toString()).get("value").iterator();
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
    }

    @Test
    @Order(9)
    public void listMetricDefinitions() {
        Response<BinaryData> response = testRunBuilder.buildClient().getMetricDefinitionsWithResponse(newTestRunId, "LoadTestRunMetrics", null);
        try {
            Iterator<JsonNode> metricDefinitionsIterator = OBJECT_MAPPER.readTree(response.getValue().toString()).get("value").iterator();
            boolean found = false;
            while (metricDefinitionsIterator.hasNext()) {
                JsonNode definition = metricDefinitionsIterator.next();
                if (definition.get("namespace").asText().equals("LoadTestRunMetrics") && definition.get("name").asText() != null && definition.has("dimensions")) {
                    found = true;
                    break;
                }
            }
            Assertions.assertTrue(found);
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    @Order(10)
    public void listMetrics() {
        String startDateTime = "", endDateTime = "";
        Response<BinaryData> response = testRunBuilder.buildClient().getTestRunWithResponse(newTestRunId, null);
        try {
            JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());

            startDateTime = test.get("startDateTime").asText();
            endDateTime = test.get("endDateTime").asText();
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }

        PagedIterable<BinaryData> metricsResponse = testRunBuilder.buildClient().listMetrics(newTestRunId, "VirtualUsers", "LoadTestRunMetrics", startDateTime + "/" + endDateTime, null);
        boolean valid = metricsResponse.stream().anyMatch((metricsBinary) -> {
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
        Assertions.assertTrue(valid);
    }

    // Lists

    @Test
    @Order(11)
    public void listTestRuns() {
        RequestOptions reqOpts = new RequestOptions()
                                    .addQueryParam("testId", existingTestId);
        PagedIterable<BinaryData> response = testRunBuilder.buildClient().listTestRuns(reqOpts);
        boolean found = response.stream().anyMatch((testRunBinary) -> {
            try {
                JsonNode testRun = OBJECT_MAPPER.readTree(testRunBinary.toString());
                if (testRun.get("testRunId").asText().equals(newTestRunId) && testRun.get("testId").asText().equals(existingTestId)) {
                    return true;
                }
            } catch (Exception e) {
                // no-op
            }
            return false;
        });
        Assertions.assertTrue(found);
    }

    // Deletes

    @Test
    @Order(12)
    public void deleteTestRun() {
        Assertions.assertDoesNotThrow(() -> {
            testRunBuilder.buildClient().deleteTestRunWithResponse(newTestRunId, null);
        });
    }
}
