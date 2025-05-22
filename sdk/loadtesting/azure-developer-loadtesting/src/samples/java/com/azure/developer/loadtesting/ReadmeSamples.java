// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.developer.loadtesting.models.LoadTest;
import com.azure.developer.loadtesting.models.LoadTestConfiguration;
import com.azure.developer.loadtesting.models.LoadTestRun;
import com.azure.developer.loadtesting.models.MetricDefinitions;
import com.azure.developer.loadtesting.models.MetricNamespaces;
import com.azure.developer.loadtesting.models.PassFailAction;
import com.azure.developer.loadtesting.models.PassFailAggregationFunction;
import com.azure.developer.loadtesting.models.PassFailCriteria;
import com.azure.developer.loadtesting.models.PassFailMetric;
import com.azure.developer.loadtesting.models.PfMetrics;
import com.azure.developer.loadtesting.models.SecretType;
import com.azure.developer.loadtesting.models.TestRunStatus;
import com.azure.developer.loadtesting.models.TestSecret;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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
        
        // Create a test object
        LoadTest loadTest = new LoadTest()
            .setDisplayName("Sample Display Name")
            .setDescription("Sample Description")
            .setLoadTestConfiguration(new LoadTestConfiguration() // Load Test Configuration describes the number of test engines to generate load
                .setEngineInstances(1));

        // Environment Variables are plain-text data passed to test engines
        Map<String, String> envVarMap = new HashMap<>();
        envVarMap.put("a", "b");
        envVarMap.put("x", "y");
        loadTest.setEnvironmentVariables(envVarMap);

        // Secrets are secure data sent using Azure Key Vault
        TestSecret testSecrets = new TestSecret()
            .setType(SecretType.KEY_VAULT_SECRET_URI)
            .setValue("https://samplevault.vault.azure.net/secrets/samplesecret/f113f91fd4c44a368049849c164db827");
        Map<String, TestSecret> secretsMap = new HashMap<>();
        secretsMap.put("sampleSecret", testSecrets);
        loadTest.setSecrets(secretsMap);

        // PassFailCriteria defines the conditions to conclude the test as success
        Map<String, PassFailMetric> passFailMetrics = new HashMap<>();
        PassFailMetric samplePassFailMetric = new PassFailMetric()
            .setClientMetric(PfMetrics.RESPONSE_TIME_IN_MILLISECONDS)
            .setAggregate(PassFailAggregationFunction.AVERAGE)
            .setCondition(">")
            .setValue(20D)
            .setAction(PassFailAction.CONTINUE);
        passFailMetrics.put("fefd759d-7fe8-4f83-8b6d-aeebe0f491fe", samplePassFailMetric);
        PassFailCriteria passFailCriteria = new PassFailCriteria()
            .setPassFailMetrics(passFailMetrics);
        loadTest.setPassFailCriteria(passFailCriteria);

        // Now can create the test using the client and receive the response
        LoadTest testResponse = adminClient.createOrUpdateTest("test12345", loadTest);

        System.out.println(testResponse.toString());
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

        LoadTestRun testRun = new LoadTestRun()
            .setDisplayName("Sample Test Run Display Name")
            .setDescription("Sample Test Run Description")
            .setTestId("test12345");

        // Now can create the test run using the client and receive response
        SyncPoller<LoadTestRun, LoadTestRun> testRunPoller = testRunClient.beginTestRun("testrun12345", testRun, null);

        System.out.println(testRunPoller.poll().getValue().toString());

        // Wait for test to reach terminal state
        // Wait for test to reach terminal state
        PollResponse<LoadTestRun> testRunOut = testRunPoller.poll();
        TestRunStatus testStatus = null;
        String startDateTime = null, endDateTime = null;

        while (!testRunOut.getStatus().isComplete()) {
            testRunOut = testRunPoller.poll();

            // Get the status of the test run
            testStatus = testRunOut.getValue().getStatus();
            System.out.println("Test run status: " + testStatus.toString());

            // Wait and check test status every 5 seconds
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                // handle interruption
            }
        }

        LoadTestRun finalResponse = testRunPoller.getFinalResult();
        startDateTime = finalResponse.getStartDateTime().toString();
        endDateTime = finalResponse.getEndDateTime().toString();

        // Get list of all metric namespaces and pick the first one
        MetricNamespaces metricNamespaces = testRunClient.getMetricNamespaces("testrun12345");
        String metricNamespace = null;

        metricNamespace = metricNamespaces.getValue().get(0).getName();

        // Get list of all metric definitions and pick the first one
        MetricDefinitions metricDefinitions = testRunClient.getMetricDefinitions("testrun12345", metricNamespace);
        String metricName = null;
        metricName = metricDefinitions.getValue().get(0).getName();

        // Fetch client metrics using metric namespace and metric name
        PagedIterable<BinaryData> clientMetricsOut = testRunClient.listMetrics("testrun12345", metricName, metricNamespace, startDateTime + '/' + endDateTime, null);

        clientMetricsOut.forEach((clientMetric) -> {
            System.out.println(clientMetric.toString());
        });
        // END: java-readme-sample-runTest
    }
}
