// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
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
import com.azure.developer.loadtesting.models.TestFileInfo;
import com.azure.developer.loadtesting.models.TestRunStatus;
import com.azure.developer.loadtesting.models.TestSecret;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to create and successfully run a test.
 */
public final class HelloWorld {
    /**
     * Authenticates with the load testing resource and shows how to list tests, test files and test runs for a given
     * resource.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws ClientAuthenticationException - when the credentials have insufficient permissions for load test
     * resource.
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
        final String testId = "sample-test-id";
        final String testRunId = "sample-test-run-id";
        final String testFileName = "test-script.jmx";
        final String testFilePath = "C:/path/to/file/sample-script.jmx";

        /*
         * BEGIN: Create test
         */
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
        LoadTest testResponse = adminClient.createOrUpdateTest(testId, loadTest);

        System.out.println(testResponse.toString());
        /*
         * END: Create test
         */

        /*
         * BEGIN: Upload test file
         */
        // Extract file contents to BinaryData
        BinaryData fileData = BinaryData.fromFile(new File(testFilePath).toPath());

        // Receive response with BinaryData content
        // NOTE: file name should be passed as input argument `testFileName`. File name in local path is ignored
        PollResponse<TestFileInfo> fileUrlOut = adminClient.beginUploadTestFile(testId, testFileName, fileData)
            .waitForCompletion(Duration.ofMinutes(2));

        System.out.println(fileUrlOut.getValue().toString());
        /*
         * END: Upload test file
         */

        /*
         * BEGIN: Start test run
         */
        // Create a Test Run object
        LoadTestRun testRun = new LoadTestRun()
            .setDisplayName("Sample Test Run Display Name")
            .setDescription("Sample Test Run Description")
            .setTestId(testId);

        // Now can create the test run using the client and receive response
        SyncPoller<LoadTestRun, LoadTestRun> testRunPoller = testRunClient.beginTestRun(testRunId, testRun, null);

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

        LoadTestRun stoppedTestRunOut = testRunClient.stopTestRun(testRunId);

        System.out.println(stoppedTestRunOut.toString());
        /*
         * END: Stop test run
         */

        /*
         * BEGIN: List metrics
         */
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
        MetricNamespaces metricNamespaces = testRunClient.getMetricNamespaces(testRunId);
        String metricNamespace = null;

        metricNamespace = metricNamespaces.getValue().get(0).getName();

        // Get list of all metric definitions and pick the first one
        MetricDefinitions metricDefinitions = testRunClient.getMetricDefinitions(testRunId, metricNamespace);
        String metricName = null;
        metricName = metricDefinitions.getValue().get(0).getName();

        // Fetch client metrics using metric namespace and metric name
        PagedIterable<BinaryData> clientMetricsOut = testRunClient.listMetrics(testRunId, metricName, metricNamespace, startDateTime + '/' + endDateTime, null);

        clientMetricsOut.forEach((clientMetric) -> {
            System.out.println(clientMetric.toString());
        });
        /*
         * END: List metrics
         */
    }
}
