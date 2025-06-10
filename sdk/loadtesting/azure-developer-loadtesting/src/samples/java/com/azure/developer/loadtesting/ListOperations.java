// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.developer.loadtesting.models.LoadTest;
import com.azure.developer.loadtesting.models.LoadTestRun;
import com.azure.developer.loadtesting.models.LoadTestingFileType;
import com.azure.developer.loadtesting.models.TestFileInfo;
import com.azure.developer.loadtesting.models.TestProfile;
import com.azure.developer.loadtesting.models.TestProfileRun;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample demonstrates how to list tests, test files, test runs, test profiles and test profile runs for a given resource.
 */
public final class ListOperations {
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
        listTests();
        listTestRuns();
        listTestFiles();
        listTestProfiles();
        listTestProfileRuns();
    }

    public static void listTests() {
        // BEGIN: java-listOperations-sample-listTests
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        PagedIterable<LoadTest> tests = client.listTests(
            "lastModifiedDateTime desc",
            null,
            null,
            null
        );

        tests.forEach(test -> {
            String testId = test.getTestId();
            String displayName = test.getDisplayName();

            System.out.println(String.format("%s\t%s", testId, displayName));
        });
        // END: java-listOperations-sample-listTests
    }

    public static void listTestRuns() {
        // BEGIN: java-listOperations-sample-listTestRuns
        LoadTestRunClient client = new LoadTestRunClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        PagedIterable<LoadTestRun> testRuns = client.listTestRuns(
            "lastModifiedDateTime desc",
            "scenario1",
            null,
            null,
            null,
            "EXECUTING,DONE",
            null
        );

        testRuns.forEach(testRun -> {
            String testRunId = testRun.getTestRunId();
            String testId = testRun.getTestId();
            String displayName = testRun.getDisplayName();

            System.out.println(String.format("%s\t%s\t%s", testRunId, testId, displayName));
        });
        // END: java-listOperations-sample-listTestRuns
    }

    public static void listTestFiles() {
        // BEGIN: java-listOperations-sample-listTestFiles
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        String inputTestId = "12345678-1234-1234-1234-123456789abc";
        PagedIterable<TestFileInfo> files = client.listTestFiles(inputTestId);

        files.forEach(fileInfo -> {
            String fileName = fileInfo.getFileName();
            LoadTestingFileType fileType = fileInfo.getFileType();
            String blobUrl = fileInfo.getUrl();

            System.out.println(String.format("%s\t%s\t%s", fileName, fileType, blobUrl));
        });
        // END: java-listOperations-sample-listTestFiles
    }

    public static void listTestProfiles() {
        // BEGIN: java-listOperations-sample-listTestProfiles
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        PagedIterable<TestProfile> testProfiles = client.listTestProfiles();

        testProfiles.forEach((TestProfile testProfile) -> {
            System.out.println(String.format("%s:\t%s", testProfile.getTestProfileId(), testProfile.getDisplayName()));
        });
        // END: java-listOperations-sample-listTestProfiles
    }

    public static void listTestProfileRuns() {
        // BEGIN: java-listOperations-sample-listTestProfileRuns
        LoadTestRunClient client = new LoadTestRunClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        PagedIterable<TestProfileRun> testProfileRuns = client.listTestProfileRuns();

        testProfileRuns.forEach((TestProfileRun testProfileRun) -> {
            System.out.println(String.format("%s:\t%s", testProfileRun.getTestProfileRunId(), testProfileRun.getDisplayName()));
        });
        // END: java-listOperations-sample-listTestProfileRuns
    }
}
