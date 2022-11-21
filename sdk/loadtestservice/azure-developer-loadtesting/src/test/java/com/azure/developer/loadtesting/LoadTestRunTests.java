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
    public void createOrUpdateTestRun() {
        BinaryData body = BinaryData.fromObject(getTestRunBodyFromDict(existingTestId));
        Response<BinaryData> response = builder.buildLoadTestRunClient().createOrUpdateTestRunWithResponse(newTestRunId, body, null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(2)
    public void createOrUpdateAppComponent() {
        BinaryData body = BinaryData.fromObject(getAppComponentBodyFromDict());
        Response<BinaryData> response = builder.buildLoadTestRunClient().createOrUpdateAppComponentWithResponse(
                                                newTestRunId,
                                                body,
                                                null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(3)
    public void createOrUpdateServerMetricsConfig() {
        BinaryData body = BinaryData.fromObject(getServerMetricsBodyFromDict());
        Response<BinaryData> response = builder.buildLoadTestRunClient().createOrUpdateServerMetricsConfigWithResponse(
                                                newTestRunId,
                                                body,
                                                null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(4)
    public void beginStartTestRun() {
        BinaryData body = BinaryData.fromObject(getTestRunBodyFromDict(existingTestId));
        SyncPoller<BinaryData, BinaryData> poller = builder.buildLoadTestRunClient().beginStartTestRun(newTestRunId2, body, null);
        poller = setPlaybackSyncPollerPollInterval(poller);
        PollResponse<BinaryData> response = poller.waitForCompletion();
        BinaryData testRunBinary = poller.getFinalResult();
        try {
            JsonNode testRunNode = OBJECT_MAPPER.readTree(testRunBinary.toString());
            Assertions.assertTrue(testRunNode.get("testRunId").asText().equals(newTestRunId2) && testRunNode.get("status").asText().equals("DONE"));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertNotNull(response.getValue().toString());
    }

    // Gets

    @Test
    @Order(5)
    public void getFile() {
        Response<BinaryData> response = builder.buildLoadTestRunClient().getTestRunFileWithResponse(newTestRunId, uploadJmxFileName, null);
        try {
            JsonNode file = OBJECT_MAPPER.readTree(response.getValue().toString());
            Assertions.assertTrue(file.get("filename").asText().equals(uploadJmxFileName) && file.get("fileType").asText().equals("JMX_FILE"));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(6)
    public void getTestRun() {
        Response<BinaryData> response = builder.buildLoadTestRunClient().getTestRunWithResponse(newTestRunId, null);
        try {
            JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());
            Assertions.assertTrue(test.get("testRunId").asText().equals(newTestRunId));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(7)
    public void getAppComponents() {
        Response<BinaryData> response = builder.buildLoadTestRunClient().getAppComponentsWithResponse(newTestRunId, null);
        try {
            JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());
            Assertions.assertTrue(test.get("components").has(defaultAppComponentResourceId) && test.get("components").get(defaultAppComponentResourceId).get("resourceId").asText().equalsIgnoreCase(defaultAppComponentResourceId));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void getServerMetricsConfig() {
        Response<BinaryData> response = builder.buildLoadTestRunClient().getServerMetricsConfigWithResponse(newTestRunId, null);
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
    @Order(9)
    public void listMetricNamespaces() {
        Response<BinaryData> response = builder.buildLoadTestRunClient().listMetricNamespacesWithResponse(newTestRunId, null);
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
    @Order(10)
    public void listMetricDefinitions() {
        Response<BinaryData> response = builder.buildLoadTestRunClient().listMetricDefinitionsWithResponse(newTestRunId, "LoadTestRunMetrics", null);
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
    @Order(11)
    public void getMetrics() {
        String startDateTime = "", endDateTime = "";
        Response<BinaryData> response = builder.buildLoadTestRunClient().getTestRunWithResponse(newTestRunId, null);
        try {
            JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());

            startDateTime = test.get("startDateTime").asText();
            endDateTime = test.get("executedDateTime").asText();
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }

        response = builder.buildLoadTestRunClient().getMetricsWithResponse(newTestRunId, "VirtualUsers", "LoadTestRunMetrics", startDateTime + "/" + endDateTime, null);
        try {
            Iterator<JsonNode> metricsIterator = OBJECT_MAPPER.readTree(response.getValue().toString()).get("timeseries").iterator();
            boolean found = false;
            while (metricsIterator.hasNext()) {
                JsonNode metric = metricsIterator.next();
                if (metric.has("data")) {
                    found = true;
                    break;
                }
            }
            Assertions.assertTrue(found);
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
    }

    // Lists

    @Test
    @Order(12)
    public void listTestRuns() {
        RequestOptions reqOpts = new RequestOptions()
                                    .addQueryParam("testId", existingTestId);
        PagedIterable<BinaryData> response = builder.buildLoadTestRunClient().listTestRuns(reqOpts);
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
    @Order(13)
    public void deleteTestRun() {
        Assertions.assertDoesNotThrow(() -> {
            builder.buildLoadTestRunClient().deleteTestRunWithResponse(newTestRunId, null);
        });
        Assertions.assertDoesNotThrow(() -> {
            builder.buildLoadTestRunClient().deleteTestRunWithResponse(newTestRunId2, null);
        });
    }
}
