// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.developer.loadtesting.models.FileValidationStatus;
import com.azure.developer.loadtesting.models.FunctionFlexConsumptionResourceConfiguration;
import com.azure.developer.loadtesting.models.FunctionFlexConsumptionTargetResourceConfigurations;
import com.azure.developer.loadtesting.models.LoadTest;
import com.azure.developer.loadtesting.models.LoadTestConfiguration;
import com.azure.developer.loadtesting.models.LoadTestingFileType;
import com.azure.developer.loadtesting.models.TestAppComponents;
import com.azure.developer.loadtesting.models.TestFileInfo;
import com.azure.developer.loadtesting.models.TestProfile;
import com.azure.developer.loadtesting.models.TestServerMetricsConfiguration;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    private BinaryData getFileBodyFromResource(String fileName) {
        URL url = LoadTestAdministrationTests.class.getClassLoader().getResource(fileName);

        return BinaryData.fromFile(new File(url.getPath()).toPath());
    }

    // Puts and Patches

    @Test
    @Order(1)
    public void createOrUpdateTest() {
        LoadTest loadTest = new LoadTest().setDisplayName("Java SDK Sample Test")
            .setDescription("Sample Test")
            .setLoadTestConfiguration(new LoadTestConfiguration().setEngineInstances(1));
        LoadTestAdministrationClient adminClient = getLoadTestAdministrationClient();
        LoadTest response = adminClient.createOrUpdateTest(newTestId, loadTest);

        assertEquals(newTestId, response.getTestId());
    }

    @Test
    @Order(2)
    public void beginUploadTestFileAdditionalFiles() {
        BinaryData file = getFileBodyFromResource(uploadCsvFileName);
        RequestOptions requestOptions
            = new RequestOptions().addQueryParam("fileType", LoadTestingFileType.ADDITIONAL_ARTIFACTS.toString());
        PollResponse<BinaryData> response
            = getLoadTestAdministrationClient().beginUploadTestFile(newTestId, uploadCsvFileName, file, requestOptions)
                .poll();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
    }

    @Test
    @Order(3)
    public void beginUploadTestFileTestScript() {
        BinaryData file = getFileBodyFromResource(uploadJmxFileName);
        RequestOptions fileUploadRequestOptions
            = new RequestOptions().addQueryParam("fileType", LoadTestingFileType.TEST_SCRIPT.toString());
        SyncPoller<BinaryData, BinaryData> poller = getLoadTestAdministrationClient().beginUploadTestFile(newTestId,
            uploadJmxFileName, file, fileUploadRequestOptions);
        poller = setPlaybackSyncPollerPollInterval(poller);
        PollResponse<BinaryData> response = poller.waitForCompletion();
        BinaryData fileBinary = poller.getFinalResult();

        try (JsonReader jsonReader = JsonProviders.createReader(fileBinary.toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            assertEquals(FileValidationStatus.VALIDATION_SUCCESS.toString(), jsonTree.get("validationStatus"));
            assertEquals(uploadJmxFileName, jsonTree.get("fileName"));
        } catch (IOException e) {
            fail("Encountered exception while reading test file data", e);
        }

        assertNotNull(response.getValue());
    }

    @Test
    @Order(4)
    public void createOrUpdateAppComponents() {

        TestAppComponents appComponents = getTestAppComponents();
        LoadTestAdministrationClient adminClient = getLoadTestAdministrationClient();

        TestAppComponents response = adminClient.createOrUpdateAppComponents(newTestId, appComponents);
        assertNotNull(response);
    }

    @Test
    @Order(5)
    public void createOrUpdateServerMetricsConfig() {

        TestServerMetricsConfiguration serverMetricsConfig = getTestServerMetricsConfiguration();
        LoadTestAdministrationClient adminClient = getLoadTestAdministrationClient();

        TestServerMetricsConfiguration response
            = adminClient.createOrUpdateServerMetricsConfig(newTestId, serverMetricsConfig);
        assertNotNull(response);
    }

    @Test
    @Order(6)
    public void createOrUpdateTestProfile() {

        Map<String, FunctionFlexConsumptionResourceConfiguration> configurations = new HashMap<>();
        configurations.put("config1",
            new FunctionFlexConsumptionResourceConfiguration().setInstanceMemoryMB(2048).setHttpConcurrency(100L));
        configurations.put("config2",
            new FunctionFlexConsumptionResourceConfiguration().setInstanceMemoryMB(4096).setHttpConcurrency(100L));

        LoadTestAdministrationClient adminClient = getLoadTestAdministrationClient();
        TestProfile testProfile = new TestProfile().setTestId(newTestId)
            .setDisplayName("Java SDK Sample Test Profile")
            .setDescription("Sample Test Profile")
            .setTargetResourceId(targetResourceId)
            .setTargetResourceConfigurations(
                new FunctionFlexConsumptionTargetResourceConfigurations().setConfigurations(configurations));

        TestProfile response = adminClient.createOrUpdateTestProfile(newTestProfileId, testProfile);
        assertNotNull(response);
        assertEquals(newTestProfileId, response.getTestProfileId());
    }

    // Gets

    @Test
    @Order(7)
    public void getTestFile() {
        TestFileInfo response = getLoadTestAdministrationClient().getTestFile(newTestId, uploadJmxFileName);

        assertEquals(LoadTestingFileType.TEST_SCRIPT.toString(), response.getFileType().toString());
        assertEquals(uploadJmxFileName, response.getFileName());

        TestFileInfo additionalFileResponse
            = getLoadTestAdministrationClient().getTestFile(newTestId, uploadCsvFileName);

        assertEquals(LoadTestingFileType.ADDITIONAL_ARTIFACTS.toString(),
            additionalFileResponse.getFileType().toString());
        assertEquals(uploadCsvFileName, additionalFileResponse.getFileName());
    }

    @Test
    @Order(8)
    public void getTest() {
        LoadTest response = getLoadTestAdministrationClient().getTest(newTestId);

        assertNotNull(response);
        assertEquals(newTestId, response.getTestId());
    }

    @Test
    @Order(9)
    public void getTestProfile() {
        TestProfile response = getLoadTestAdministrationClient().getTestProfile(newTestProfileId);

        assertNotNull(response);
        assertEquals(newTestProfileId, response.getTestProfileId());
    }

    @Test
    @Order(10)
    public void getAppComponents() {
        TestAppComponents response = getLoadTestAdministrationClient().getAppComponents(newTestId);
        assertNotNull(response);

        assertTrue(response.getComponents().containsKey(defaultAppComponentResourceId));
        assertEquals(defaultAppComponentResourceId,
            response.getComponents().get(defaultAppComponentResourceId).getResourceId());
    }

    @Test
    @Order(11)
    public void getServerMetricsConfig() {
        TestServerMetricsConfiguration response = getLoadTestAdministrationClient().getServerMetricsConfig(newTestId);

        assertNotNull(response);
        assertTrue(response.getMetrics().containsKey(defaultServerMetricId));
    }

    // Lists

    @Test
    @Order(12)
    public void listTestFiles() {
        PagedIterable<TestFileInfo> response = getLoadTestAdministrationClient().listTestFiles(newTestId);
        boolean uploadFilesFound = response.stream().anyMatch((fileInfo) -> {
            return fileInfo.getFileName().equals(uploadJmxFileName) || fileInfo.getFileName().equals(uploadCsvFileName);
        });

        assertTrue(uploadFilesFound);
    }

    @Test
    @Order(13)
    public void listTests() {
        PagedIterable<LoadTest> response
            = getLoadTestAdministrationClient().listTests("lastModifiedDateTime desc", null, null, null);
        boolean found = response.stream().anyMatch((loadTest) -> {
            return loadTest.getTestId().equals(newTestId);
        });

        assertTrue(found);
    }

    @Test
    @Order(14)
    public void listTestProfiles() {
        PagedIterable<TestProfile> response = getLoadTestAdministrationClient().listTestProfiles();
        boolean found = response.stream().anyMatch((testProfile) -> {
            return testProfile.getTestProfileId().equals(newTestProfileId);
        });

        assertTrue(found);
    }

    // Deletes

    @Test
    @Order(15)
    public void deleteTestFile() {
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTestFileWithResponse(newTestId, uploadCsvFileName, null);
        });
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTestFileWithResponse(newTestId, uploadJmxFileName, null);
        });
    }

    @Test
    @Order(16)
    public void deleteTestProfile() {
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTestProfileWithResponse(newTestProfileId, null);
        });
    }

    @Test
    @Order(17)
    public void deleteTest() {
        assertDoesNotThrow(() -> {
            getLoadTestAdministrationClient().deleteTestWithResponse(newTestId, null);
        });
    }
}
