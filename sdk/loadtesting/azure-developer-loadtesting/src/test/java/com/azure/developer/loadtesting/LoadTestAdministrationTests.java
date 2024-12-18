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
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestAdministrationTests extends LoadTestingClientTestBase {

    // Helpers

    private Map<String, Object> getTestBodyFromDict() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("displayName", "Java SDK Sample Test");
        testMap.put("description", "Sample Test");

        Map<String, Object> loadTestConfigMap = new HashMap<>();
        loadTestConfigMap.put("engineInstances", 1);
        testMap.put("loadTestConfiguration", loadTestConfigMap);

        Map<String, Object> envVarMap = new HashMap<>();
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
        Response<BinaryData> response =
            getLoadTestAdministrationClient().createOrUpdateTestWithResponse(newTestId, body, null);

        assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(2)
    public void beginUploadTestFileAdditionalFiles() {
        BinaryData file = getFileBodyFromResource(uploadCsvFileName);
        RequestOptions requestOptions = new RequestOptions().addQueryParam("fileType", "ADDITIONAL_ARTIFACTS");
        PollResponse<BinaryData> response = getLoadTestAdministrationClient()
            .beginUploadTestFile(newTestId, uploadCsvFileName, file, requestOptions)
            .poll();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
    }

    @Test
    @Order(3)
    public void beginUploadTestFileTestScript() {
        BinaryData file = getFileBodyFromResource(uploadJmxFileName);
        RequestOptions fileUploadRequestOptions = new RequestOptions().addQueryParam("fileType", "JMX_FILE");
        SyncPoller<BinaryData, BinaryData> poller = getLoadTestAdministrationClient()
            .beginUploadTestFile(newTestId, uploadJmxFileName, file, fileUploadRequestOptions);
        poller = setPlaybackSyncPollerPollInterval(poller);
        PollResponse<BinaryData> response = poller.waitForCompletion();
        BinaryData fileBinary = poller.getFinalResult();

        try (JsonReader jsonReader = JsonProviders.createReader(fileBinary.toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            assertEquals("VALIDATION_SUCCESS", jsonTree.get("validationStatus"));
            assertEquals(uploadJmxFileName, jsonTree.get("fileName"));
        } catch (IOException e) {
            fail("Encountered exception while reading test file data", e);
        }

        assertNotNull(response.getValue());
    }

    @Test
    @Order(4)
    public void createOrUpdateAppComponents() {
        BinaryData body = BinaryData.fromObject(getAppComponentBodyFromDict());
        Response<BinaryData> response = getLoadTestAdministrationClient()
            .createOrUpdateAppComponentsWithResponse(newTestId, body, null);

        assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(5)
    public void createOrUpdateServerMetricsConfig() {
        BinaryData body = BinaryData.fromObject(getServerMetricsBodyFromDict());
        Response<BinaryData> response = getLoadTestAdministrationClient()
            .createOrUpdateServerMetricsConfigWithResponse(newTestId, body, null);

        assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    // Gets

    @Test
    @Order(6)
    public void getTestFile() {
        Response<BinaryData> response = getLoadTestAdministrationClient()
            .getTestFileWithResponse(newTestId, uploadJmxFileName, null);

        assertEquals(200, response.getStatusCode());

        try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            assertEquals(uploadJmxFileName, jsonTree.get("fileName"));
            assertEquals("JMX_FILE", jsonTree.get("fileType"));
        } catch (IOException e) {
            fail("Encountered exception while reading test file data", e);
        }
    }

    @Test
    @Order(7)
    public void getTest() {
        Response<BinaryData> response = getLoadTestAdministrationClient().getTestWithResponse(newTestId, null);

        assertEquals(200, response.getStatusCode());

        try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            assertEquals(newTestId, jsonTree.get("testId"));
        } catch (IOException e) {
            fail("Encountered exception while reading test data", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(8)
    public void getAppComponents() {
        Response<BinaryData> response = getLoadTestAdministrationClient().getAppComponentsWithResponse(newTestId, null);

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
            fail("Encountered exception while reading test data", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(9)
    public void getServerMetricsConfig() {
        Response<BinaryData> response = getLoadTestAdministrationClient()
            .getServerMetricsConfigWithResponse(newTestId, null);

        assertEquals(200, response.getStatusCode());

        try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);
            Map<String, Object> components = (Map<String, Object>) jsonTree.get("metrics");

            assertTrue(components.containsKey(defaultServerMetricId));

            Map<String, Object> appComponentResource =
                (Map<String, Object>) components.get(defaultServerMetricId);

            assertTrue(defaultServerMetricId.equalsIgnoreCase(appComponentResource.get("id").toString()));
        } catch (IOException e) {
            fail("Encountered exception while reading test data", e);
        }
    }

    // Lists

    @Test
    @Order(10)
    public void listTestFiles() {
        PagedIterable<BinaryData> response = getLoadTestAdministrationClient().listTestFiles(newTestId, null);
        boolean found = response.stream().anyMatch((fileBinary) -> {
            try (JsonReader jsonReader = JsonProviders.createReader(fileBinary.toBytes())) {
                Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                assertEquals(uploadJmxFileName, jsonTree.get("fileName"));
                assertEquals("JMX_FILE", jsonTree.get("fileType"));

                return true;
            } catch (IOException e) {
                // no-op
            }

            return false;
        });

        assertTrue(found);
    }

    @Test
    @Order(11)
    public void listTests() {
        RequestOptions reqOpts = new RequestOptions().addQueryParam("orderBy", "lastModifiedDateTime desc");
        PagedIterable<BinaryData> response = getLoadTestAdministrationClient().listTests(reqOpts);
        boolean found = response.stream().anyMatch((testBinary) -> {
            try (JsonReader jsonReader = JsonProviders.createReader(testBinary.toBytes())) {
                Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                return newTestId.equals(jsonTree.get("testId"));
            } catch (IOException e) {
                return false;
            }
        });

        assertTrue(found);
    }

    // Deletes

    @Test
    @Order(12)
    public void deleteTestFile() {
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTestFileWithResponse(newTestId, uploadCsvFileName, null);
        });
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTestFileWithResponse(newTestId, uploadJmxFileName, null);
        });
    }

    @Test
    @Order(13)
    public void deleteTest() {
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTestWithResponse(newTestId, null);
        });
    }
}
