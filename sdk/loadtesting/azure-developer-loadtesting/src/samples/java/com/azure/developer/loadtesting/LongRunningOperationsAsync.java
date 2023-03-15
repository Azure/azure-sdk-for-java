// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sample demonstrates how to uploading and validating a test file, and running a test run.
 *
 * Authenticates with the load testing resource and shows how to uploading and validating a test file, and running
 * a test run in a given resource.
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
                .addQueryParam("fileType", "JMX_FILE");

        PollerFlux<BinaryData, BinaryData> poller = client.beginUploadTestFile(inputTestId, inputFileName, fileData, reqOpts);
        poller = poller.setPollInterval(pollInterval);

        poller.subscribe(pollResponse -> {
            try {
                JsonNode file = new ObjectMapper().readTree(pollResponse.getValue().toString());
                String validationStatus = file.get("validationStatus").asText();
                System.out.println("Validation Status: " + validationStatus.toString());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        AsyncPollResponse<BinaryData, BinaryData> pollResponse = poller.blockLast();
        BinaryData fileBinary = pollResponse.getFinalResult().block();

        try {
            JsonNode file = new ObjectMapper().readTree(fileBinary.toString());
            String url = file.get("url").asText();
            String fileName = file.get("fileName").asText();
            String fileType = file.get("fileType").asText();
            String validationStatus = file.get("validationStatus").asText();
            System.out.println(String.format("%s\t%s\t%s\t%s", fileName, fileType, url, validationStatus));
        } catch (JsonProcessingException e) {
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

        Map<String, Object> testRunMap = new HashMap<String, Object>();
        testRunMap.put("testId", inputTestId);
        testRunMap.put("displayName", "Sample Test Run");
        testRunMap.put("description", "Java SDK Sample Test Run");

        Duration pollInterval = Duration.ofSeconds(5);

        BinaryData inputTestRunBinary = BinaryData.fromObject(testRunMap);

        PollerFlux<BinaryData, BinaryData> poller = client.beginTestRun(inputTestRunId, inputTestRunBinary, null);
        poller = poller.setPollInterval(pollInterval);

        poller.subscribe(pollResponse -> {
            BinaryData testRunBinary = pollResponse.getValue();
            System.out.println("Test Run all info: " + testRunBinary.toString());
        });

        AsyncPollResponse<BinaryData, BinaryData> pollResponse = poller.blockLast();
        BinaryData testRunBinary = pollResponse.getFinalResult().block();

        try {
            JsonNode file = new ObjectMapper().readTree(testRunBinary.toString());
            String testId = file.get("testId").asText();
            String testRunId = file.get("testRunId").asText();
            String status = file.get("status").asText();
            System.out.println(String.format("%s\t%s\t%s", testId, testRunId, status));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // END: java-longRunningOperationsAsync-sample-beginTestRun
    }
}
