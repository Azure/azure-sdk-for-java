// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.developer.loadtesting.models.LoadTestRun;
import com.azure.developer.loadtesting.models.LoadTestingFileType;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to upload and validate a test file, running a test run and running a test profile run.
 *
 * Authenticates with the load testing resource and shows how to upload and validate a test file, run a test run
 * and run a test profile run in a given resource.
 *
 * @throws ClientAuthenticationException - when the credentials have insufficient permissions for load test resource.
 * @throws ResourceNotFoundException - when test with `testId` does not exist when uploading file.
 */
public final class LongRunningOperations {
    public void beginUploadTestFile() {
        // BEGIN: java-longRunningOperations-sample-beginUploadTestFile
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        String inputTestId = "12345678-1234-1234-1234-123456789abc";
        String inputFileName = "input-test-file.jmx";
        BinaryData fileData = BinaryData.fromFile(new File("C:/fakepath/input-file.jmx").toPath());
        /* Note: file name passed as input argument is used, over the name in local file path */

        Duration pollInterval = Duration.ofSeconds(1);

        RequestOptions reqOpts = new RequestOptions()
            .addQueryParam("fileType", LoadTestingFileType.TEST_SCRIPT.toString());

        SyncPoller<BinaryData, BinaryData> poller = client.beginUploadTestFile(inputTestId, inputFileName, fileData, reqOpts);
        poller = poller.setPollInterval(pollInterval);

        PollResponse<BinaryData> pollResponse = poller.poll();

        while (pollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS
            || pollResponse.getStatus() == LongRunningOperationStatus.NOT_STARTED) {

            try (JsonReader jsonReader = JsonProviders.createReader(pollResponse.getValue().toBytes())) {
                Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                String validationStatus = jsonTree.get("validationStatus").toString();
                System.out.println("Validation Status: " + validationStatus);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException e) {
                // handle interruption
            }

            pollResponse = poller.poll();
        }

        poller.waitForCompletion();

        BinaryData fileBinary = poller.getFinalResult();

        try (JsonReader jsonReader = JsonProviders.createReader(fileBinary.toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            String url = jsonTree.get("url").toString();
            String fileName = jsonTree.get("fileName").toString();
            String fileType = jsonTree.get("fileType").toString();
            String validationStatus = jsonTree.get("validationStatus").toString();
            System.out.println(String.format("%s\t%s\t%s\t%s", fileName, fileType, url, validationStatus));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // END: java-longRunningOperations-sample-beginUploadTestFile
    }

    public void beginTestRun() {
        // BEGIN: java-longRunningOperations-sample-beginTestRun
        LoadTestRunClient client = new LoadTestRunClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        String inputTestRunId = "12345678-1234-1234-1234-123456789abc";
        String inputTestId = "87654321-1234-1234-1234-123456789abc";

        LoadTestRun testRun = new LoadTestRun()
            .setTestId(inputTestId)
            .setDisplayName("Sample Test Run")
            .setDescription("Java SDK Sample Test Run");

        Duration pollInterval = Duration.ofSeconds(5);

        SyncPoller<LoadTestRun, LoadTestRun> poller = client.beginTestRun(inputTestRunId, testRun, null);
        poller = poller.setPollInterval(pollInterval);

        PollResponse<LoadTestRun> pollResponse = poller.poll();

        while (pollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS
            || pollResponse.getStatus() == LongRunningOperationStatus.NOT_STARTED) {

            LoadTestRun testRunResponse = pollResponse.getValue();

            System.out.println("Test Run all info: " + testRunResponse.toString());

            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException e) {
                // handle interruption
            }

            pollResponse = poller.poll();
        }

        poller.waitForCompletion();
        LoadTestRun testRunResponse = poller.getFinalResult();

        String testId = testRunResponse.getTestId();
        String testRunId = testRunResponse.getTestRunId();
        String status = testRunResponse.getStatus().toString();

        System.out.println(String.format("%s\t%s\t%s", testId, testRunId, status));
        // END: java-longRunningOperations-sample-beginTestRun
    }

    public void beginTestProfileRun() {
        // BEGIN: java-longRunningOperations-sample-beginTestProfileRun
        LoadTestRunClient client = new LoadTestRunClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        String inputTestProfileRunId = "12345678-1234-1234-1234-123456789abc";
        String inputTestProfileId = "87654321-1234-1234-1234-123456789abc";

        Map<String, Object> testProfileRunMap = new HashMap<>();
        testProfileRunMap.put("testProfileId", inputTestProfileId);
        testProfileRunMap.put("displayName", "Sample Test Profile Run");
        testProfileRunMap.put("description", "Java SDK Sample Test Profile Run");

        Duration pollInterval = Duration.ofSeconds(30);

        BinaryData inputTestProfileRunBinary = BinaryData.fromObject(testProfileRunMap);

        SyncPoller<BinaryData, BinaryData> poller = client.beginTestProfileRun(inputTestProfileRunId, inputTestProfileRunBinary, null);
        poller = poller.setPollInterval(pollInterval);

        PollResponse<BinaryData> pollResponse = poller.poll();

        while (pollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS
            || pollResponse.getStatus() == LongRunningOperationStatus.NOT_STARTED) {

            BinaryData testProfileRunBinary = pollResponse.getValue();

            System.out.println("Test Profile Run all info: " + testProfileRunBinary.toString());

            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException e) {
                // handle interruption
            }

            pollResponse = poller.poll();
        }

        poller.waitForCompletion();
        BinaryData testProfileRunBinary = poller.getFinalResult();

        try (JsonReader jsonReader = JsonProviders.createReader(testProfileRunBinary.toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            String testProfileId = jsonTree.get("testProfileId").toString();
            String testProfileRunId = jsonTree.get("testProfileRunId").toString();
            String status = jsonTree.get("status").toString();
            System.out.println(String.format("%s\t%s\t%s", testProfileId, testProfileRunId, status));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // END: java-longRunningOperations-sample-beginTestProfileRun
    }
}
