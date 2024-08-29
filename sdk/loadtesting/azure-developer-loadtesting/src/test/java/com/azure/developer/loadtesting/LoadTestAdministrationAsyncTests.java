// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestAdministrationAsyncTests extends LoadTestingClientTestBase {

    // Helpers

    private Map<String, Object> getTestBodyFromDict() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("displayName", "Java SDK Sample Test Async");
        testMap.put("description", "Sample Test Async");

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
        URL url = LoadTestAdministrationAsyncTests.class.getClassLoader().getResource(fileName);

        return BinaryData.fromFile(new File(url.getPath()).toPath());
    }

    // Puts and Patches

    @Test
    @Order(1)
    public void createOrUpdateTest() {
        BinaryData body = BinaryData.fromObject(getTestBodyFromDict());
        Mono<Response<BinaryData>> monoResponse = getLoadTestAdministrationAsyncClient()
            .createOrUpdateTestWithResponse(newTestIdAsync, body, null);
        StepVerifier.create(monoResponse)
            .assertNext(response -> assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode())))
            .verifyComplete();
    }

    @Test
    @Order(2)
    public void beginUploadTestFileAdditionalFiles() {
        BinaryData file = getFileBodyFromResource(uploadCsvFileName);
        RequestOptions requestOptions = new RequestOptions().addQueryParam("fileType", "ADDITIONAL_ARTIFACTS");
        PollerFlux<BinaryData, BinaryData> poller = getLoadTestAdministrationAsyncClient()
            .beginUploadTestFile(newTestIdAsync, uploadCsvFileName, file, requestOptions);
        poller = setPlaybackPollerFluxPollInterval(poller);

        StepVerifier.create(poller.last())
            .assertNext(pollResponse ->
                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus()))
            .verifyComplete();
    }

    @Test
    @Order(3)
    public void beginUploadTestFileTestScript() {
        BinaryData file = getFileBodyFromResource(uploadJmxFileName);
        RequestOptions fileUploadRequestOptions = new RequestOptions().addQueryParam("fileType", "JMX_FILE");
        PollerFlux<BinaryData, BinaryData> poller = getLoadTestAdministrationAsyncClient()
            .beginUploadTestFile(newTestIdAsync, uploadJmxFileName, file, fileUploadRequestOptions);
        poller = setPlaybackPollerFluxPollInterval(poller);

        StepVerifier.create(poller.takeUntil(pollResponse -> pollResponse.getStatus().isComplete())
            .last()
            .flatMap(AsyncPollResponse::getFinalResult))
            .assertNext(fileBinary -> {
                try (JsonReader jsonReader = JsonProviders.createReader(fileBinary.toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                    assertEquals("VALIDATION_SUCCESS", jsonTree.get("validationStatus"));
                    assertEquals(uploadJmxFileName, jsonTree.get("fileName"));
                } catch (IOException e) {
                    fail("Encountered exception while reading test file data", e);
                }
            })
            .verifyComplete();
    }

    @Test
    @Order(4)
    public void createOrUpdateAppComponents() {
        BinaryData body = BinaryData.fromObject(getAppComponentBodyFromDict());
        Mono<Response<BinaryData>> monoResponse = getLoadTestAdministrationAsyncClient()
            .createOrUpdateAppComponentsWithResponse(newTestIdAsync, body, null);

        StepVerifier.create(monoResponse)
            .assertNext(response -> assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode())))
            .verifyComplete();
    }

    @Test
    @Order(5)
    public void createOrUpdateServerMetricsConfig() {
        BinaryData body = BinaryData.fromObject(getServerMetricsBodyFromDict());
        Mono<Response<BinaryData>> monoResponse = getLoadTestAdministrationAsyncClient()
            .createOrUpdateServerMetricsConfigWithResponse(newTestIdAsync, body, null);

        StepVerifier.create(monoResponse)
            .assertNext(response -> assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode())))
            .verifyComplete();
    }

    // Gets

    @Test
    @Order(6)
    public void getTestFile() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestAdministrationAsyncClient()
            .getTestFileWithResponse(newTestIdAsync, uploadJmxFileName, null);

        StepVerifier.create(monoResponse)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());

                try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                    assertEquals(uploadJmxFileName, jsonTree.get("fileName"));
                    assertEquals("JMX_FILE", jsonTree.get("fileType"));
                } catch (IOException e) {
                    fail("Encountered exception while reading test file data", e);
                }
            })
            .verifyComplete();
    }

    @Test
    @Order(7)
    public void getTest() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestAdministrationAsyncClient()
            .getTestWithResponse(newTestIdAsync, null);

        StepVerifier.create(monoResponse)
            .assertNext(response -> {
                try (JsonReader jsonReader = JsonProviders.createReader(response.getValue().toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                    assertEquals(newTestIdAsync, jsonTree.get("testId"));
                } catch (IOException e) {
                    fail("Encountered exception while reading test data", e);
                }

                assertEquals(200, response.getStatusCode());
            })
            .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(8)
    public void getAppComponents() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestAdministrationAsyncClient()
            .getAppComponentsWithResponse(newTestIdAsync, null);

        StepVerifier.create(monoResponse)
            .assertNext(response -> {
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
            })
            .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(9)
    public void getServerMetricsConfig() {
        Mono<Response<BinaryData>> monoResponse = getLoadTestAdministrationAsyncClient()
            .getServerMetricsConfigWithResponse(newTestIdAsync, null);

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

    // Lists

    @Test
    @Order(10)
    public void listTestFiles() {
        PagedFlux<BinaryData> response = getLoadTestAdministrationAsyncClient().listTestFiles(newTestIdAsync, null);

        StepVerifier.create(response)
            .expectNextMatches(fileBinary -> {
                try (JsonReader jsonReader = JsonProviders.createReader(fileBinary.toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                    assertEquals(uploadJmxFileName, jsonTree.get("fileName"));
                    assertEquals("JMX_FILE", jsonTree.get("fileType"));

                    return true;
                } catch (IOException e) {
                    // no-op
                }

                return false;
            })
            .thenConsumeWhile(fileBinary -> true)
            .verifyComplete();
    }

    @Test
    @Order(11)
    public void listTests() {
        RequestOptions reqOpts = new RequestOptions().addQueryParam("orderBy", "lastModifiedDateTime desc");
        PagedFlux<BinaryData> response = getLoadTestAdministrationAsyncClient().listTests(reqOpts);

        StepVerifier.create(response)
            .expectNextMatches(testBinary -> {
                try (JsonReader jsonReader = JsonProviders.createReader(testBinary.toBytes())) {
                    Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                    return newTestIdAsync.equals(jsonTree.get("testId"));
                } catch (IOException e) {
                    return false;
                }
            })
            .thenConsumeWhile(fileBinary -> true)
            .verifyComplete();
    }

    // Deletes

    @Test
    @Order(12)
    public void deleteTestFile() {
        StepVerifier.create(getLoadTestAdministrationAsyncClient()
                .deleteTestFileWithResponse(newTestIdAsync, uploadCsvFileName, null))
            .expectNextCount(1)
            .verifyComplete();
        StepVerifier.create(getLoadTestAdministrationAsyncClient()
                .deleteTestFileWithResponse(newTestIdAsync, uploadJmxFileName, null))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    @Order(13)
    public void deleteTest() {
        StepVerifier.create(getLoadTestAdministrationAsyncClient()
                .deleteTestWithResponse(newTestIdAsync, null))
            .expectNextCount(1)
            .verifyComplete();
    }
}
