// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.developer.loadtesting.models.LoadTest;
import com.azure.developer.loadtesting.models.LoadTestRun;
import com.azure.developer.loadtesting.models.LoadTestingFileType;
import com.azure.developer.loadtesting.models.NotificationRule;
import com.azure.developer.loadtesting.models.TestFileInfo;
import com.azure.developer.loadtesting.models.Trigger;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample demonstrates how to list tests, test files, test runs, triggers
 * and notification rules for a given resource.
 */
public final class ListOperationsAsync {
    /**
     * Authenticates with the load testing resource and shows how to list tests, test files, test runs, triggers
     * and notification rules for a given resource.
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
        listTriggers();
        listNotificationRules();
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
            null, // testRunIds (List<String>)
            null
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

    public static void listTriggers() {
        // BEGIN: java-listOperationsAsync-sample-listTriggers
        LoadTestAdministrationAsyncClient client = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildAsyncClient();

        PagedFlux<Trigger> triggers = client.listTriggers();

        triggers.subscribe(trigger -> {
            System.out.println(String.format("%s\\t%s", trigger.getTriggerId(), trigger.getDisplayName()));
        });
        // END: java-listOperationsAsync-sample-listTriggers
    }

    public static void listNotificationRules() {
        // BEGIN: java-listOperationsAsync-sample-listNotificationRules
        LoadTestAdministrationAsyncClient client = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildAsyncClient();

        PagedFlux<NotificationRule> notificationRules = client.listNotificationRules();

        notificationRules.subscribe(rule -> {
            System.out.println(String.format("%s\\t%s", rule.getNotificationRuleId(), rule.getDisplayName()));
        });
        // END: java-listOperationsAsync-sample-listNotificationRules
    }
}
