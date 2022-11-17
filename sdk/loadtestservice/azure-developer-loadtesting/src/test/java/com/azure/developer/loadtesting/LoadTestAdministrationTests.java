// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.developer.loadtesting.LoadTestAdministrationAsyncClient.ValidationStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    // Body helpers

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

    private Map<String, Object> getAppComponentBodyFromDict() {
        Map<String, Object> appCompMap = new HashMap<String, Object>();
        Map<String, Object> compsMap = new HashMap<String, Object>();
        Map<String, Object> compMap = new HashMap<String, Object>();
        compMap.put("resourceId", defaultAppComponentResourceId);
        compMap.put("resourceType", "microsoft.insights/components");
        compMap.put("resourceName", "appcomponentresource");
        compMap.put("displayName", "Performance_LoadTest_Insights");
        compMap.put("kind", "web");

        compsMap.put(defaultAppComponentResourceId, compMap);
        appCompMap.put("components", compsMap);

        return appCompMap;
    }

    private Map<String, Object> getServerMetricsBodyFromDict() {
        Map<String, Object> serverMetricsMap = new HashMap<String, Object>();
        Map<String, Object> metricsMap = new HashMap<String, Object>();
        Map<String, Object> metricMap = new HashMap<String, Object>();
        metricMap.put("resourceId", defaultAppComponentResourceId);
        metricMap.put("metricNamespace", "microsoft.insights/components");
        metricMap.put("name", "requests/duration");
        metricMap.put("aggregation", "Average");
        metricMap.put("resourceType", "microsoft.insights/components");

        metricsMap.put(defaultServerMetricId, metricMap);
        serverMetricsMap.put("metrics", metricsMap);

        return serverMetricsMap;
    }

    // Puts and Patches

    @Test
    @Order(1)
    public void createOrUpdateLoadTest() {
        BinaryData body = BinaryData.fromObject(getTestBodyFromDict());
        Response<BinaryData> response = builder.buildLoadTestAdministrationClient().createOrUpdateLoadTestWithResponse(newTestId, body, null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(2)
    public void uploadTestFileResourceNotFound() {
        URL fileJmxUrl = LoadTestAdministrationTests.class.getClassLoader().getResource(defaultUploadFileName);
        BinaryData file = BinaryData.fromFile(new File(fileJmxUrl.getPath()).toPath());
        RequestOptions requestOptions = new RequestOptions().addQueryParam("fileType", "JMX_FILE");
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            builder.buildLoadTestAdministrationClient().uploadFileWithResponse(
                                                "fake_test_id",
                                                defaultUploadFileName,
                                                file,
                                                requestOptions);
        });
    }

    @Test
    @Order(3)
    public void uploadTestFileValid() {
        URL fileJmxUrl = LoadTestAdministrationTests.class.getClassLoader().getResource(defaultUploadFileName);
        BinaryData file = BinaryData.fromFile(new File(fileJmxUrl.getPath()).toPath());
        RequestOptions requestOptions = new RequestOptions().addQueryParam("fileType", "JMX_FILE");
        Response<BinaryData> response = builder.buildLoadTestAdministrationClient().uploadFileWithResponse(
                                                newTestId,
                                                defaultUploadFileName,
                                                file,
                                                requestOptions);
        Assertions.assertEquals(201, response.getStatusCode());
    }

    @Test
    @Order(4)
    public void createOrUpdateAppComponent() {
        BinaryData body = BinaryData.fromObject(getAppComponentBodyFromDict());
        Response<BinaryData> response = builder.buildLoadTestAdministrationClient().createOrUpdateAppComponentWithResponse(
                                                newTestId,
                                                body,
                                                null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(5)
    public void createOrUpdateServerMetricsConfig() {
        BinaryData body = BinaryData.fromObject(getServerMetricsBodyFromDict());
        Response<BinaryData> response = builder.buildLoadTestAdministrationClient().createOrUpdateServerMetricsConfigWithResponse(
                                                newTestId,
                                                body,
                                                null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    // Gets

    @Test
    @Order(6)
    public void beginGetValidationStatus() {
        SyncPoller<ValidationStatus, BinaryData> poller = builder.buildLoadTestAdministrationClient().beginGetValidationStatus(newTestId, 1);
        PollResponse<ValidationStatus> response = poller.waitForCompletion();
        BinaryData testBinary = poller.getFinalResult();
        try {
            JsonNode test = new ObjectMapper().readTree(testBinary.toString());
            String validationStatus = test.get("inputArtifacts")
                                        .get("testScriptFileInfo")
                                        .get("validationStatus")
                                        .asText();
            Assertions.assertTrue(test.get("testId").asText().equals(newTestId) && validationStatus.equals(ValidationStatus.VALIDATION_SUCCESS.toString()));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(ValidationStatus.VALIDATION_SUCCESS, response.getValue());
    }

    @Test
    @Order(7)
    public void getFile() {
        Response<BinaryData> response = builder.buildLoadTestAdministrationClient().getFileWithResponse(newTestId, defaultUploadFileName, null);
        try {
            JsonNode file = new ObjectMapper().readTree(response.getValue().toString());
            Assertions.assertTrue(file.get("filename").asText().equals(defaultUploadFileName) && file.get("fileType").asText().equals("JMX_FILE"));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void getLoadTest() {
        Response<BinaryData> response = builder.buildLoadTestAdministrationClient().getLoadTestWithResponse(newTestId, null);
        try {
            JsonNode test = new ObjectMapper().readTree(response.getValue().toString());
            Assertions.assertTrue(test.get("testId").asText().equals(newTestId));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(9)
    public void getAppComponents() {
        Response<BinaryData> response = builder.buildLoadTestAdministrationClient().getAppComponentsWithResponse(newTestId, null);
        try {
            JsonNode test = new ObjectMapper().readTree(response.getValue().toString());
            Assertions.assertTrue(test.get("components").has(defaultAppComponentResourceId) && test.get("components").get(defaultAppComponentResourceId).get("resourceId").asText().equalsIgnoreCase(defaultAppComponentResourceId));
        } catch (Exception e) {
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(10)
    public void getServerMetricsConfig() {
        Response<BinaryData> response = builder.buildLoadTestAdministrationClient().getServerMetricsConfigWithResponse(newTestId, null);
        try {
            JsonNode test = new ObjectMapper().readTree(response.getValue().toString());
            Assertions.assertTrue(test.get("metrics").has(defaultServerMetricId) && test.get("metrics").get(defaultServerMetricId).get("id").asText().equalsIgnoreCase(defaultServerMetricId));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.assertTrue(false);
        }
        Assertions.assertEquals(200, response.getStatusCode());
    }

    // Lists

    @Test
    @Order(11)
    public void listTestFiles() {
        PagedIterable<BinaryData> response = builder.buildLoadTestAdministrationClient().listTestFiles(newTestId, null);
        boolean found = response.stream().anyMatch((fileBinary) -> {
            try {
                JsonNode file = new ObjectMapper().readTree(fileBinary.toString());
                if (file.get("filename").asText().equals(defaultUploadFileName) && file.get("fileType").asText().equals("JMX_FILE")) {
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
    @Order(12)
    public void listLoadTests() {
        RequestOptions reqOpts = new RequestOptions()
                                    .addQueryParam("orderBy", "lastModifiedDateTime desc");
        PagedIterable<BinaryData> response = builder.buildLoadTestAdministrationClient().listLoadTests(reqOpts);
        boolean found = response.stream().anyMatch((testBinary) -> {
            try {
                JsonNode test = new ObjectMapper().readTree(testBinary.toString());
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

    // Deletions

    @Test
    @Order(13)
    public void deleteFile() {
        Assertions.assertDoesNotThrow(() -> {
            builder.buildLoadTestAdministrationClient().deleteFileWithResponse(newTestId, defaultUploadFileName, null);
        });
    }

    @Test
    @Order(14)
    public void deleteTest() {
        Assertions.assertDoesNotThrow(() -> {
            builder.buildLoadTestAdministrationClient().deleteLoadTestWithResponse(newTestId, null);
        });
    }
}
