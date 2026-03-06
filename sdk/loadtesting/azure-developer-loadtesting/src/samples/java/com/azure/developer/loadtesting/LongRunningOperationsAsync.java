// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.developer.loadtesting.models.LoadTestRun;
import com.azure.developer.loadtesting.models.LoadTestingFileType;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;
import java.time.Duration;

/**
 * Sample demonstrates how to upload and validate a test file, running a test run.
 *
 * Authenticates with the load testing resource and shows how to upload and validate a test file, run a test run
 * in a given resource.
 *
 * @throws ClientAuthenticationException - when the credentials have insufficient permissions for load test resource.
 * @throws ResourceNotFoundException - when test with `testId` does not exist when uploading file.
 */
public final class LongRunningOperationsAsync {
    public void beginUploadTestFile() {
        // BEGIN: java-longRunningOperationsAsync-sample-beginUploadTestFile
        LoadTestAdministrationAsyncClient client = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildAsyncClient();

        String inputTestId = "12345678-1234-1234-1234-123456789abc";
        String inputFileName = "input-test-file.jmx";
        BinaryData fileData = BinaryData.fromFile(new File("C:/fakepath/input-file.jmx").toPath());
        /* Note: file name passed as input argument is used, over the name in local file path */

        Duration pollInterval = Duration.ofSeconds(1);

        RequestOptions reqOpts = new RequestOptions()
            .addQueryParam("fileType", LoadTestingFileType.JMX_FILE.toString());

        PollerFlux<BinaryData, BinaryData> poller = client.beginUploadTestFile(inputTestId, inputFileName, fileData, reqOpts);
        poller = poller.setPollInterval(pollInterval);

        poller.subscribe(pollResponse -> {
            try (com.azure.json.JsonReader jsonReader = com.azure.json.JsonProviders.createReader(pollResponse.getValue().toBytes())) {
                java.util.Map<String, Object> jsonTree = jsonReader.readMap(com.azure.json.JsonReader::readUntyped);
                String validationStatus = jsonTree.get("validationStatus").toString();
                System.out.println("Validation Status: " + validationStatus);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        });

        AsyncPollResponse<BinaryData, BinaryData> pollResponse = poller.blockLast();
        BinaryData fileBinary = pollResponse.getFinalResult().block();

        try (com.azure.json.JsonReader jsonReader = com.azure.json.JsonProviders.createReader(fileBinary.toBytes())) {
            java.util.Map<String, Object> jsonTree = jsonReader.readMap(com.azure.json.JsonReader::readUntyped);
            String url = jsonTree.get("url").toString();
            String fileName = jsonTree.get("fileName").toString();
            String fileType = jsonTree.get("fileType").toString();
            String validationStatus = jsonTree.get("validationStatus").toString();
            System.out.println(String.format("%s\\t%s\\t%s\\t%s", fileName, fileType, url, validationStatus));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        // END: java-longRunningOperationsAsync-sample-beginUploadTestFile
    }

    public void beginTestRun() {
        // BEGIN: java-longRunningOperationsAsync-sample-beginTestRun
        LoadTestRunAsyncClient client = new LoadTestRunClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildAsyncClient();

        String inputTestRunId = "12345678-1234-1234-1234-123456789abc";
        String inputTestId = "87654321-1234-1234-1234-123456789abc";

        LoadTestRun testRun = new LoadTestRun()
            .setTestId(inputTestId)
            .setDisplayName("Sample Test Run")
            .setDescription("Java SDK Sample Test Run");

        Duration pollInterval = Duration.ofSeconds(5);

        PollerFlux<LoadTestRun, LoadTestRun> poller = client.beginTestRun(inputTestRunId, testRun);
        poller = poller.setPollInterval(pollInterval);

        poller.subscribe(pollResponse -> {
            LoadTestRun testRunResponse = pollResponse.getValue();
            System.out.println("Test Run all info: " + testRunResponse.toString());
        });

        AsyncPollResponse<LoadTestRun, LoadTestRun> finalPollResponse = poller.blockLast();
        LoadTestRun testRunResponse = finalPollResponse.getFinalResult().block();

        String testId = testRunResponse.getTestId();
        String testRunId = testRunResponse.getTestRunId();
        String status = testRunResponse.getStatus().toString();
        System.out.println(String.format("%s\\t%s\\t%s", testId, testRunId, status));
        // END: java-longRunningOperationsAsync-sample-beginTestRun
    }
}
