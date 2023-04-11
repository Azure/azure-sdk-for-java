// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sample demonstrates how to create and successfully run a test.
 */
public final class HelloWorld {
    /**
     * Authenticates with the load testing resource and shows how to list tests, test files and test runs
     * for a given resource.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws ClientAuthenticationException - when the credentials have insufficient permissions for load test resource.
     * @throws ResourceNotFoundException - when test with `testId` does not exist when listing files.
     */
    public static void main(String[] args) {
        // Initialize the clients
        LoadTestAdministrationClient adminClient = new LoadTestAdministrationClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildClient();
        LoadTestRunClient testRunClient = new LoadTestRunClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildClient();

        // Constants and parameters
        final String testId = "6758667a-a57c-47e5-9cef-9b1f1432daca";
        final String testRunId = "f758667a-c5ac-269a-dce1-5c1f14f2d142";
        final String testFileName = "test-script.jmx";
        final String testFilePath = "C:/path/to/file/sample-script.jmx";

        /*
         * BEGIN: Create test
         */
        // construct Test object using nested String:Object Maps
        Map<String, Object> testMap = new HashMap<String, Object>();
        testMap.put("displayName", "Sample Display Name");
        testMap.put("description", "Sample Description");

        // loadTestConfig describes the number of test engines to generate load
        Map<String, Object> loadTestConfigMap = new HashMap<String, Object>();
        loadTestConfigMap.put("engineInstances", 1);
        testMap.put("loadTestConfiguration", loadTestConfigMap);

        // environmentVariables are plain-text data passed to test engines
        Map<String, Object> envVarMap = new HashMap<String, Object>();
        envVarMap.put("a", "b");
        envVarMap.put("x", "y");
        testMap.put("environmentVariables", envVarMap);

        // secrets are secure data sent using Azure Key Vault
        Map<String, Object> secretMap = new HashMap<String, Object>();
        Map<String, Object> sampleSecretMap = new HashMap<String, Object>();
        sampleSecretMap.put("value", "https://samplevault.vault.azure.net/secrets/samplesecret/f113f91fd4c44a368049849c164db827");
        sampleSecretMap.put("type", "AKV_SECRET_URI");
        secretMap.put("sampleSecret", sampleSecretMap);
        testMap.put("secrets", secretMap);

        // passFailCriteria define the conditions to conclude the test as success
        Map<String, Object> passFailMap = new HashMap<String, Object>();
        Map<String, Object> passFailMetrics = new HashMap<String, Object>();
        Map<String, Object> samplePassFailMetric = new HashMap<String, Object>();
        samplePassFailMetric.put("clientmetric", "response_time_ms");
        samplePassFailMetric.put("aggregate", "percentage");
        samplePassFailMetric.put("condition", ">");
        samplePassFailMetric.put("value", "20");
        samplePassFailMetric.put("action", "continue");
        passFailMetrics.put("fefd759d-7fe8-4f83-8b6d-aeebe0f491fe", samplePassFailMetric);
        passFailMap.put("passFailMetrics", passFailMetrics);
        testMap.put("passFailCriteria", passFailMap);

        // convert the object Map to JSON BinaryData
        BinaryData test = BinaryData.fromObject(testMap);

        // receive response with BinaryData content
        Response<BinaryData> testOutResponse = adminClient.createOrUpdateTestWithResponse(testId, test, null);
        System.out.println(testOutResponse.getValue().toString());
        /*
         * END: Create test
         */

        /*
         * BEGIN: Upload test file
         */
        // extract file contents to BinaryData
        BinaryData fileData = BinaryData.fromFile(new File(testFilePath).toPath());

        // receive response with BinaryData content
        // NOTE: file name should be passed as input argument `testFileName`. File name in local path is ignored
        PollResponse<BinaryData> fileUrlOut = adminClient.beginUploadTestFile(testId, testFileName, fileData, null).waitForCompletion(Duration.ofMinutes(2));
        System.out.println(fileUrlOut.getValue().toString());
        /*
         * END: Upload test file
         */

        /*
         * BEGIN: Start test run
         */
        // construct Test Run object using nested String:Object Maps
        Map<String, Object> testRunMap = new HashMap<String, Object>();
        testRunMap.put("testId", testId);
        testRunMap.put("displayName", "SDK-Created-TestRun");

        // convert the object Map to JSON BinaryData
        BinaryData testRun = BinaryData.fromObject(testRunMap);

        // receive response with BinaryData content
        SyncPoller<BinaryData, BinaryData> testRunPoller = testRunClient.beginTestRun(testRunId, testRun, null);
        System.out.println(testRunPoller.poll().getValue().toString());
        /*
         * END: Start test run
         */

        /*
         * BEGIN: Stop test run
         */
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            // handle interruption
        }

        Response<BinaryData> stoppedTestRunOut = testRunClient.stopTestRunWithResponse(testRunId, null);
        System.out.println(stoppedTestRunOut.getValue().toString());
        /*
         * END: Stop test run
         */

        /*
         * BEGIN: List metrics
         */
        // wait for test to reach terminal state
        PollResponse<BinaryData> testRunOut = testRunPoller.poll();
        JsonNode testRunJson = null;
        String testStatus = null, startDateTime = null, endDateTime = null;
        while (!testRunOut.getStatus().isComplete()) {
            testRunOut = testRunPoller.poll();
            // parse JSON and read status value
            try {
                testRunJson = new ObjectMapper().readTree(testRunOut.getValue().toString());
                testStatus = testRunJson.get("status").asText();
                System.out.println("Status of test run: " +  testStatus);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                // handle error condition
            }

            // wait and check test status every 5 seconds
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                // handle interruption
            }
        }

        try {
            testRunJson = new ObjectMapper().readTree(testRunPoller.getFinalResult().toString());
            startDateTime = testRunJson.get("startDateTime").asText();
            endDateTime = testRunJson.get("endDateTime").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // handle error condition
        }

        // get list of all metric namespaces and pick the first one
        Response<BinaryData> metricNamespacesOut = testRunClient.getMetricNamespacesWithResponse(testRunId, null);
        String metricNamespace = null;
        // parse JSON and read first value
        try {
            JsonNode metricNamespacesJson = new ObjectMapper().readTree(metricNamespacesOut.getValue().toString());
            metricNamespace = metricNamespacesJson.get("value").get(0).get("metricNamespaceName").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // handle error condition
        }

        // get list of all metric definitions and pick the first one
        Response<BinaryData> metricDefinitionsOut = testRunClient.getMetricDefinitionsWithResponse(testRunId, metricNamespace, null);
        String metricName = null;
        // parse JSON and read first value
        try {
            JsonNode metricDefinitionsJson = new ObjectMapper().readTree(metricDefinitionsOut.getValue().toString());
            metricName = metricDefinitionsJson.get("value").get(0).get("name").get("value").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // handle error condition
        }

        // fetch client metrics using metric namespace and metric name
        PagedIterable<BinaryData> clientMetricsOut = testRunClient.listMetrics(testRunId, metricName, metricNamespace, startDateTime + '/' + endDateTime, null);
        clientMetricsOut.forEach((clientMetric) -> {
            System.out.println(clientMetric.toString());
        });
        /*
         * END: List metrics
         */
    }
}
