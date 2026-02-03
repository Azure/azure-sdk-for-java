// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedFlux;
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
public final class ListOperationsAsync {
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
        // BEGIN: java-listOperationsAsync-sample-listTests
        LoadTestAdministrationAsyncClient client = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildAsyncClient();

        PagedFlux<LoadTest> tests = client.listTests(
            "lastModifiedDateTime desc", // orderBy
            null, // search
            null, // lastModifiedStartTime
            null // lastModifiedEndTime
        );

        tests.subscribe(test -> {
            String testId = test.getTestId();
            String displayName = test.getDisplayName();
            System.out.println(String.format("%s\\t%s", testId, displayName));
        });
        // END: java-listOperationsAsync-sample-listTests
    }

    public static void listTestRuns() {
        // BEGIN: java-listOperationsAsync-sample-listTestRuns
        LoadTestRunAsyncClient client = new LoadTestRunClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildAsyncClient();

        PagedFlux<LoadTestRun> testRuns = client.listTestRuns(
            "lastModifiedDateTime desc", // orderBy
            "scenario1", // search
            null, // testId
            null, // executionFrom
            null, // executionTo
            "EXECUTING,DONE", // status
            null // testRunIds (List<String>)
        );

        testRuns.subscribe(testRun -> {
            String testRunId = testRun.getTestRunId();
            String testId = testRun.getTestId();
            String displayName = testRun.getDisplayName();
            System.out.println(String.format("%s\\t%s\\t%s", testRunId, testId, displayName));
        });
        // END: java-listOperationsAsync-sample-listTestRuns
    }

    public static void listTestFiles() {
        // BEGIN: java-listOperationsAsync-sample-listTestFiles
        LoadTestAdministrationAsyncClient client = new LoadTestAdministrationClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildAsyncClient();

        String inputTestId = "12345678-1234-1234-1234-123456789abc";

        PagedFlux<TestFileInfo> files = client.listTestFiles(inputTestId);

        files.subscribe(fileInfo -> {
            String fileName = fileInfo.getFileName();
            LoadTestingFileType fileType = fileInfo.getFileType();
            String blobUrl = fileInfo.getUrl();
            System.out.println(String.format("%s\\t%s\\t%s", fileName, fileType, blobUrl));
        });
        // END: java-listOperationsAsync-sample-listTestFiles
    }

    public static void listTestProfiles() {
        // BEGIN: java-listOperationsAsync-sample-listTestProfiles
        LoadTestAdministrationAsyncClient client = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildAsyncClient();

        PagedFlux<TestProfile> testProfiles = client.listTestProfiles();

        testProfiles.subscribe(testProfile -> {
            System.out.println(String.format("%s\\t%s", testProfile.getTestProfileId(), testProfile.getDisplayName()));
        });
        // END: java-listOperationsAsync-sample-listTestProfiles
    }

    public static void listTestProfileRuns() {
        // BEGIN: java-listOperationsAsync-sample-listTestProfileRuns
        LoadTestRunAsyncClient client = new LoadTestRunClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildAsyncClient();

        PagedFlux<TestProfileRun> testProfileRuns = client.listTestProfileRuns();

        testProfileRuns.subscribe(testProfileRun -> {
            System.out.println(String.format("%s\\t%s", testProfileRun.getTestProfileRunId(), testProfileRun.getDisplayName()));
        });
        // END: java-listOperationsAsync-sample-listTestProfileRuns
    }
}
