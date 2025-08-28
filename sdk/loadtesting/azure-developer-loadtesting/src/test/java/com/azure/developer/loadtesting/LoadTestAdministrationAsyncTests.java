// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestAdministrationAsyncTests extends LoadTestingClientTestBase {

    // Helpers

    private BinaryData getFileBodyFromResource(String fileName) {
        URL url = LoadTestAdministrationAsyncTests.class.getClassLoader().getResource(fileName);

        return BinaryData.fromFile(new File(url.getPath()).toPath());
    }

    // Puts and Patches

    @Test
    @Order(1)
    public void createOrUpdateTest() {
        LoadTest loadTest = new LoadTest().setDisplayName("Java SDK Sample Test")
            .setDescription("Sample Test")
            .setLoadTestConfiguration(new LoadTestConfiguration().setEngineInstances(1));

        LoadTestAdministrationAsyncClient adminClient = getLoadTestAdministrationAsyncClient();
        Mono<LoadTest> monoResponse = adminClient.createOrUpdateTest(newTestIdAsync, loadTest);
        StepVerifier.create(monoResponse).assertNext(response -> assertNotNull(response)).verifyComplete();
    }

    @Test
    @Order(2)
    public void beginUploadTestFileAdditionalFiles() {
        BinaryData file = getFileBodyFromResource(uploadCsvFileName);
        RequestOptions requestOptions
            = new RequestOptions().addQueryParam("fileType", LoadTestingFileType.ADDITIONAL_ARTIFACTS.toString());
        PollerFlux<BinaryData, BinaryData> poller = getLoadTestAdministrationAsyncClient()
            .beginUploadTestFile(newTestIdAsync, uploadCsvFileName, file, requestOptions);
        poller = setPlaybackPollerFluxPollInterval(poller);

        StepVerifier.create(poller.last())
            .assertNext(pollResponse -> assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                pollResponse.getStatus()))
            .verifyComplete();
    }

    @Test
    @Order(3)
    public void beginUploadTestFileTestScript() {
        BinaryData file = getFileBodyFromResource(uploadJmxFileName);
        RequestOptions fileUploadRequestOptions
            = new RequestOptions().addQueryParam("fileType", LoadTestingFileType.TEST_SCRIPT.toString());
        PollerFlux<BinaryData, BinaryData> poller = getLoadTestAdministrationAsyncClient()
            .beginUploadTestFile(newTestIdAsync, uploadJmxFileName, file, fileUploadRequestOptions);
        poller = setPlaybackPollerFluxPollInterval(poller);

        StepVerifier.create(poller.takeUntil(pollResponse -> pollResponse.getStatus().isComplete())
            .last()
            .flatMap(AsyncPollResponse::getFinalResult)).assertNext(fileBinary -> {
                try (JsonReader jsonReader = JsonProviders.createReader(fileBinary.toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                    assertEquals(FileValidationStatus.VALIDATION_SUCCESS.toString(), jsonTree.get("validationStatus"));
                    assertEquals(uploadJmxFileName, jsonTree.get("fileName"));
                } catch (IOException e) {
                    fail("Encountered exception while reading test file data", e);
                }
            }).verifyComplete();
    }

    @Test
    @Order(4)
    public void createOrUpdateAppComponents() {
        TestAppComponents appComponents = getTestAppComponents();
        LoadTestAdministrationAsyncClient adminClient = getLoadTestAdministrationAsyncClient();
        Mono<TestAppComponents> monoResponse = adminClient.createOrUpdateAppComponents(newTestIdAsync, appComponents);

        StepVerifier.create(monoResponse).assertNext(response -> assertNotNull(response)).verifyComplete();
    }

    @Test
    @Order(5)
    public void createOrUpdateServerMetricsConfig() {

        TestServerMetricsConfiguration serverMetricsConfig = getTestServerMetricsConfiguration();
        LoadTestAdministrationAsyncClient adminClient = getLoadTestAdministrationAsyncClient();
        Mono<TestServerMetricsConfiguration> monoResponse
            = adminClient.createOrUpdateServerMetricsConfig(newTestIdAsync, serverMetricsConfig);

        StepVerifier.create(monoResponse).assertNext(response -> assertNotNull(response)).verifyComplete();
    }

    @Test
    @Order(6)
    public void createOrUpdateTestProfile() {

        Map<String, FunctionFlexConsumptionResourceConfiguration> configurations = new HashMap<>();
        configurations.put("config1",
            new FunctionFlexConsumptionResourceConfiguration().setInstanceMemoryMB(2048).setHttpConcurrency(100L));
        configurations.put("config2",
            new FunctionFlexConsumptionResourceConfiguration().setInstanceMemoryMB(4096).setHttpConcurrency(100L));

        TestProfile testProfile = new TestProfile().setTestId(newTestIdAsync)
            .setDisplayName("Java SDK Sample Test Profile")
            .setDescription("Sample Test Profile")
            .setTargetResourceId(targetResourceId)
            .setTargetResourceConfigurations(
                new FunctionFlexConsumptionTargetResourceConfigurations().setConfigurations(configurations));
        LoadTestAdministrationAsyncClient adminClient = getLoadTestAdministrationAsyncClient();
        Mono<TestProfile> monoResponse = adminClient.createOrUpdateTestProfile(newTestProfileIdAsync, testProfile);
        StepVerifier.create(monoResponse).assertNext(response -> assertNotNull(response)).verifyComplete();
    }

    // Gets

    @Test
    @Order(7)
    public void getTestFile() {

        Mono<TestFileInfo> monoTestScriptFileResponse
            = getLoadTestAdministrationAsyncClient().getTestFile(newTestIdAsync, uploadJmxFileName);

        Mono<TestFileInfo> monoAdditionalFileResponse
            = getLoadTestAdministrationAsyncClient().getTestFile(newTestIdAsync, uploadCsvFileName);

        StepVerifier.create(monoTestScriptFileResponse).assertNext(testFileInfo -> {
            assertEquals(LoadTestingFileType.TEST_SCRIPT.toString(), testFileInfo.getFileType().toString());
            assertEquals(uploadJmxFileName, testFileInfo.getFileName());
        }).verifyComplete();

        StepVerifier.create(monoAdditionalFileResponse).assertNext(testFileInfo -> {
            assertEquals(LoadTestingFileType.ADDITIONAL_ARTIFACTS.toString(), testFileInfo.getFileType().toString());
            assertEquals(uploadCsvFileName, testFileInfo.getFileName());
        }).verifyComplete();
    }

    @Test
    @Order(8)
    public void getTest() {
        Mono<LoadTest> monoResponse = getLoadTestAdministrationAsyncClient().getTest(newTestIdAsync);

        StepVerifier.create(monoResponse).assertNext(response -> {
            assertNotNull(response);
            assertEquals(newTestIdAsync, response.getTestId());
        }).verifyComplete();
    }

    @Test
    @Order(9)
    public void getTestProfile() {
        Mono<TestProfile> monoResponse = getLoadTestAdministrationAsyncClient().getTestProfile(newTestProfileIdAsync);

        StepVerifier.create(monoResponse).assertNext(response -> {
            assertNotNull(response);
            assertEquals(newTestProfileIdAsync, response.getTestProfileId());
        }).verifyComplete();
    }

    @Test
    @Order(10)
    public void getAppComponents() {

        Mono<TestAppComponents> monoResponse = getLoadTestAdministrationAsyncClient().getAppComponents(newTestIdAsync);

        StepVerifier.create(monoResponse).assertNext(appComponents -> {
            assertNotNull(appComponents);
            assertTrue(appComponents.getComponents().containsKey(defaultAppComponentResourceId));
            assertEquals(defaultAppComponentResourceId,
                appComponents.getComponents().get(defaultAppComponentResourceId).getResourceId());
        }).verifyComplete();
    }

    @Test
    @Order(11)
    public void getServerMetricsConfig() {

        Mono<TestServerMetricsConfiguration> monoResponse
            = getLoadTestAdministrationAsyncClient().getServerMetricsConfig(newTestIdAsync);
        StepVerifier.create(monoResponse).assertNext(serverMetricsConfig -> {
            assertNotNull(serverMetricsConfig);
            assertTrue(serverMetricsConfig.getMetrics().containsKey(defaultServerMetricId));
            assertEquals(defaultServerMetricId,
                serverMetricsConfig.getMetrics().get(defaultServerMetricId).getResourceId());
        }).verifyComplete();
    }

    // Lists

    @Test
    @Order(12)
    public void listTestFiles() {
        PagedFlux<TestFileInfo> response = getLoadTestAdministrationAsyncClient().listTestFiles(newTestIdAsync);

        StepVerifier.create(response).expectNextMatches(testFileInfo -> {
            boolean uploadedFileFound = testFileInfo.getFileName().equals(uploadJmxFileName)
                || testFileInfo.getFileName().equals(uploadCsvFileName);

            return uploadedFileFound;
        }).thenConsumeWhile(testFileInfo -> true).verifyComplete();
    }

    @Test
    @Order(13)
    public void listTests() {
        List<LoadTest> testList = new ArrayList<>();
        PagedFlux<LoadTest> response
            = getLoadTestAdministrationAsyncClient().listTests("lastModifiedDateTime desc", null, null, null);

        StepVerifier.create(response).thenConsumeWhile(testList::add).expectComplete().verify();

        assertTrue(testList.size() > 0);
        assertTrue(testList.stream().anyMatch(test -> test.getTestId().equals(newTestIdAsync)));
    }

    @Test
    @Order(14)
    public void listTestProfiles() {

        List<TestProfile> testProfileList = new ArrayList<>();
        PagedFlux<TestProfile> response = getLoadTestAdministrationAsyncClient().listTestProfiles();

        StepVerifier.create(response).thenConsumeWhile(testProfileList::add).expectComplete().verify();

        assertTrue(testProfileList.size() > 0);
        assertTrue(testProfileList.stream()
            .anyMatch(testProfile -> testProfile.getTestProfileId().equals(newTestProfileIdAsync)));
    }

    // Deletes

    @Test
    @Order(15)
    public void deleteTestFile() {
        StepVerifier
            .create(getLoadTestAdministrationAsyncClient().deleteTestFileWithResponse(newTestIdAsync, uploadCsvFileName,
                null))
            .expectNextCount(1)
            .verifyComplete();
        StepVerifier
            .create(getLoadTestAdministrationAsyncClient().deleteTestFileWithResponse(newTestIdAsync, uploadJmxFileName,
                null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    @Order(16)
    public void deleteTestProfile() {
        StepVerifier
            .create(getLoadTestAdministrationAsyncClient().deleteTestProfileWithResponse(newTestProfileIdAsync, null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    @Order(17)
    public void deleteTest() {
        StepVerifier.create(getLoadTestAdministrationAsyncClient().deleteTestWithResponse(newTestIdAsync, null))
            .expectNextCount(1)
            .verifyComplete();
    }
}
