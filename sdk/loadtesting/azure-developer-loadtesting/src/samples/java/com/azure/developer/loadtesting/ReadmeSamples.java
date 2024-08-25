// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class ReadmeSamples {
    public void auth() {
        // BEGIN: java-readme-sample-auth
        // ensure the user, service principal or managed identity used has Loadtesting Contributor role for the resource
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        // create client using DefaultAzureCredential
        LoadTestAdministrationClient adminClient = new LoadTestAdministrationClientBuilder()
            .credential(credential)
            .endpoint("<Enter Azure Load Testing Data-Plane URL>")
            .buildClient();
        LoadTestRunClient testRunClient = new LoadTestRunClientBuilder()
            .credential(credential)
            .endpoint("<Enter Azure Load Testing Data-Plane URL>")
            .buildClient();

        RequestOptions reqOpts = new RequestOptions()
            .addQueryParam("orderBy", "lastModifiedDateTime")
            .addQueryParam("maxPageSize", "10");
        adminClient.listTests(reqOpts);

        reqOpts = new RequestOptions()
            .addQueryParam("orderBy", "lastModifiedDateTime")
            .addQueryParam("status", "EXECUTING,DONE")
            .addQueryParam("maxPageSize", "10");
        testRunClient.listTestRuns(reqOpts);
        // END: java-readme-sample-auth
    }

    public void createTest() {
        // BEGIN: java-readme-sample-createTest
        LoadTestAdministrationClient adminClient = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        // construct Test object using nested String:Object Maps
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("displayName", "Sample Display Name");
        testMap.put("description", "Sample Description");

        // loadTestConfig describes the number of test engines to generate load
        Map<String, Object> loadTestConfigMap = new HashMap<>();
        loadTestConfigMap.put("engineInstances", 1);
        testMap.put("loadTestConfiguration", loadTestConfigMap);

        // environmentVariables are plain-text data passed to test engines
        Map<String, Object> envVarMap = new HashMap<>();
        envVarMap.put("a", "b");
        envVarMap.put("x", "y");
        testMap.put("environmentVariables", envVarMap);

        // secrets are secure data sent using Azure Key Vault
        Map<String, Object> secretMap = new HashMap<>();
        Map<String, Object> sampleSecretMap = new HashMap<>();
        sampleSecretMap.put("value", "https://samplevault.vault.azure.net/secrets/samplesecret/f113f91fd4c44a368049849c164db827");
        sampleSecretMap.put("type", "AKV_SECRET_URI");
        secretMap.put("sampleSecret", sampleSecretMap);
        testMap.put("secrets", secretMap);

        // passFailCriteria define the conditions to conclude the test as success
        Map<String, Object> passFailMap = new HashMap<>();
        Map<String, Object> passFailMetrics = new HashMap<>();
        Map<String, Object> samplePassFailMetric = new HashMap<>();
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
        Response<BinaryData> testOutResponse = adminClient.createOrUpdateTestWithResponse("test12345", test, null);
        System.out.println(testOutResponse.getValue().toString());
        // END: java-readme-sample-createTest
    }

    public void uploadTestFile() throws IOException {
        // BEGIN: java-readme-sample-uploadTestFile
        LoadTestAdministrationClient adminClient = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        // extract file contents to BinaryData
        BinaryData fileData = BinaryData.fromFile(new File("path/to/file").toPath());

        // receive response with BinaryData content
        PollResponse<BinaryData> fileUrlOut = adminClient.beginUploadTestFile("test12345", "sample-file.jmx", fileData, null)
            .waitForCompletion(Duration.ofMinutes(2));
        System.out.println(fileUrlOut.getValue().toString());
        // END: java-readme-sample-uploadTestFile
    }

    public void runTest() {
        // BEGIN: java-readme-sample-runTest
        LoadTestRunClient testRunClient = new LoadTestRunClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        // construct Test Run object using nested String:Object Maps
        Map<String, Object> testRunMap = new HashMap<>();
        testRunMap.put("testId", "test12345");
        testRunMap.put("displayName", "SDK-Created-TestRun");

        // convert the object Map to JSON BinaryData
        BinaryData testRun = BinaryData.fromObject(testRunMap);

        // start test with poller
        SyncPoller<BinaryData, BinaryData> poller = testRunClient.beginTestRun("testrun12345", testRun, null);
        Duration pollInterval = Duration.ofSeconds(5);
        poller = poller.setPollInterval(pollInterval);

        // wait for test to reach terminal state
        JsonNode testRunJson = null;
        String testStatus;
        PollResponse<BinaryData> pollResponse = poller.poll();
        while (pollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS
            || pollResponse.getStatus() == LongRunningOperationStatus.NOT_STARTED) {

            try (JsonReader jsonReader = JsonProviders.createReader(pollResponse.getValue().toBytes())) {
                Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                testStatus = jsonTree.get("status").toString();
                System.out.println("Test run status: " + testStatus);
            } catch (IOException e) {
                System.out.println("Error processing JSON response");
                // handle error condition
            }

            // wait and check test status every 5 seconds
            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException e) {
                // handle interruption
            }

            pollResponse = poller.poll();
        }

        poller.waitForCompletion();
        BinaryData testRunBinary = poller.getFinalResult();

        try (JsonReader jsonReader = JsonProviders.createReader(testRunBinary.toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            testStatus = jsonTree.get("status").toString();
            System.out.println("Test run status: " + testStatus);
        } catch (IOException e) {
            System.out.println("Error processing JSON response");
            // handle error condition
        }

        String startDateTime = testRunJson.get("startDateTime").asText();
        String endDateTime = testRunJson.get("endDateTime").asText();

        // get list of all metric namespaces and pick the first one
        Response<BinaryData> metricNamespacesOut = testRunClient.getMetricNamespacesWithResponse("testrun12345", null);
        String metricNamespace = null;
        // parse JSON and read first value
        try (JsonReader jsonReader = JsonProviders.createReader(metricNamespacesOut.getValue().toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);
            List<Object> metricNamespaces = (List<Object>) jsonTree.get("value");
            Map<String, Object> namespaceMap = (Map<String, Object>) metricNamespaces.get(0);
            metricNamespace = namespaceMap.get("name").toString();
        } catch (IOException e) {
            System.out.println("Error processing JSON response");
            // handle error condition
        }

        // get list of all metric definitions and pick the first one
        Response<BinaryData> metricDefinitionsOut = testRunClient.getMetricDefinitionsWithResponse("testrun12345", metricNamespace, null);
        String metricName = null;
        // parse JSON and read first value
        try (JsonReader jsonReader = JsonProviders.createReader(metricDefinitionsOut.getValue().toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);
            List<Object> metricDefinitions = (List<Object>) jsonTree.get("value");
            Map<String, Object> definitionMap = (Map<String, Object>) metricDefinitions.get(0);
            Map<String, Object> nameMap = (Map<String, Object>) definitionMap.get("name");
            metricName = nameMap.get("value").toString();
        } catch (IOException e) {
            System.out.println("Error processing JSON response");
            // handle error condition
        }

        // fetch client metrics using metric namespace and metric name
        PagedIterable<BinaryData> clientMetricsOut = testRunClient.listMetrics("testrun12345", metricName, metricNamespace, startDateTime + '/' + endDateTime, null);
        clientMetricsOut.forEach((clientMetric) -> {
            System.out.println(clientMetric.toString());
        });
        // END: java-readme-sample-runTest
    }
}
