// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestAdministrationTests extends LoadTestingClientTestBase {

    // Helpers

    private Map<String, Object> getTestBodyFromDict() {
        Map<String, Object> testMap = new HashMap<String, Object>();
        testMap.put("displayName", "Java SDK Sample Test");
        testMap.put("description", "Sample Test");

        Map<String, Object> loadTestConfigMap = new HashMap<String, Object>();
        loadTestConfigMap.put("engineInstances", 1);
        testMap.put("loadTestConfiguration", loadTestConfigMap);

        Map<String, Object> envVarMap = new HashMap<String, Object>();
        envVarMap.put("threads_per_engine", 1);
        envVarMap.put("ramp_up_time", 0);
        envVarMap.put("duration_in_sec", 10);
        envVarMap.put("domain", "azure.microsoft.com");
        envVarMap.put("protocol", "https");
        testMap.put("environmentVariables", envVarMap);

        return testMap;
    }

    private BinaryData getFileBodyFromResource(String fileName) {
        URL url = LoadTestAdministrationTests.class.getClassLoader().getResource(fileName);
        return BinaryData.fromFile(new File(url.getPath()).toPath());
    }

    // Puts and Patches

    @Test
    @Order(1)
    public void createOrUpdateTest() {
        BinaryData body = BinaryData.fromObject(getTestBodyFromDict());
        Response<BinaryData> response = adminBuilder.buildClient().createOrUpdateTestWithResponse(newTestId, body, null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(2)
    public void beginUploadTestFileAdditionalFiles() {
        BinaryData file = getFileBodyFromResource(uploadCsvFileName);
        RequestOptions requestOptions = new RequestOptions().addQueryParam("fileType", "ADDITIONAL_ARTIFACTS");
        PollResponse<BinaryData> response = adminBuilder.buildClient().beginUploadTestFile(
                                                newTestId,
                                                uploadCsvFileName,
                                                file,
                                                requestOptions).poll();
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
    }

    @Test
    @Order(3)
    public void beginUploadTestFileTestScript() {
        BinaryData file = getFileBodyFromResource(uploadJmxFileName);
        RequestOptions fileUploadRequestOptions = new RequestOptions().addQueryParam("fileType", "JMX_FILE");
        SyncPoller<BinaryData, BinaryData> poller = adminBuilder.buildClient().beginUploadTestFile(newTestId, uploadJmxFileName, file, fileUploadRequestOptions);
        poller = setPlaybackSyncPollerPollInterval(poller);
        PollResponse<BinaryData> response = poller.waitForCompletion();
        BinaryData fileBinary = poller.getFinalResult();
        try {
            JsonNode fileNode = OBJECT_MAPPER.readTree(fileBinary.toString());
            String validationStatus = fileNode.get("validationStatus").asText();
            Assertions.assertTrue(fileNode.get("fileName").asText().equals(uploadJmxFileName) && "VALIDATION_SUCCESS".equals(validationStatus));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    @Order(4)
    public void createOrUpdateAppComponents() {
        BinaryData body = BinaryData.fromObject(getAppComponentBodyFromDict());
        Response<BinaryData> response = adminBuilder.buildClient().createOrUpdateAppComponentsWithResponse(
                                                newTestId,
                                                body,
                                                null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(5)
    public void createOrUpdateServerMetricsConfig() {
        BinaryData body = BinaryData.fromObject(getServerMetricsBodyFromDict());
        Response<BinaryData> response = adminBuilder.buildClient().createOrUpdateServerMetricsConfigWithResponse(
                                                newTestId,
                                                body,
                                                null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    // Gets

    @Test
    @Order(6)
    public void getTestFile() {
        Response<BinaryData> response = adminBuilder.buildClient().getTestFileWithResponse(newTestId, uploadJmxFileName, null);
        try {
            JsonNode file = OBJECT_MAPPER.readTree(response.getValue().toString());
            Assertions.assertTrue(file.get("fileName").asText().equals(uploadJmxFileName) && file.get("fileType").asText().equals("JMX_FILE"));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(7)
    public void getTest() {
        Response<BinaryData> response = adminBuilder.buildClient().getTestWithResponse(newTestId, null);
        try {
            JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());
            Assertions.assertTrue(test.get("testId").asText().equals(newTestId));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void getAppComponents() {
        Response<BinaryData> response = adminBuilder.buildClient().getAppComponentsWithResponse(newTestId, null);
        try {
            JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());
            Assertions.assertTrue(test.get("components").has(defaultAppComponentResourceId) && test.get("components").get(defaultAppComponentResourceId).get("resourceId").asText().equalsIgnoreCase(defaultAppComponentResourceId));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(9)
    public void getServerMetricsConfig() {
        Response<BinaryData> response = adminBuilder.buildClient().getServerMetricsConfigWithResponse(newTestId, null);
        try {
            JsonNode test = OBJECT_MAPPER.readTree(response.getValue().toString());
            Assertions.assertTrue(test.get("metrics").has(defaultServerMetricId) && test.get("metrics").get(defaultServerMetricId).get("id").asText().equalsIgnoreCase(defaultServerMetricId));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    // Lists

    @Test
    @Order(10)
    public void listTestFiles() {
        PagedIterable<BinaryData> response = adminBuilder.buildClient().listTestFiles(newTestId, null);
        boolean found = response.stream().anyMatch((fileBinary) -> {
            try {
                JsonNode file = OBJECT_MAPPER.readTree(fileBinary.toString());
                if (file.get("fileName").asText().equals(uploadJmxFileName) && file.get("fileType").asText().equals("JMX_FILE")) {
                    return true;
                }
            } catch (Exception e) {
                // no-op
            }
            return false;
        });
        Assertions.assertTrue(found);
    }

    @Test
    @Order(11)
    public void listTests() {
        RequestOptions reqOpts = new RequestOptions()
                                    .addQueryParam("orderBy", "lastModifiedDateTime desc");
        PagedIterable<BinaryData> response = adminBuilder.buildClient().listTests(reqOpts);
        boolean found = response.stream().anyMatch((testBinary) -> {
            try {
                JsonNode test = OBJECT_MAPPER.readTree(testBinary.toString());
                if (test.get("testId").asText().equals(newTestId)) {
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
    public void deleteTestFile() {
        Assertions.assertDoesNotThrow(() -> {
            adminBuilder.buildClient().deleteTestFileWithResponse(newTestId, uploadCsvFileName, null);
        });
        Assertions.assertDoesNotThrow(() -> {
            adminBuilder.buildClient().deleteTestFileWithResponse(newTestId, uploadJmxFileName, null);
        });
    }

    @Test
    @Order(13)
    public void deleteTest() {
        Assertions.assertDoesNotThrow(() -> {
            adminBuilder.buildClient().deleteTestWithResponse(newTestId, null);
        });
    }
}
