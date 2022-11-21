// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ListOperationsAsync {
    public void listTests() {
        // BEGIN: java-listOperationsAsync-sample-listTests
        LoadTestAdministrationAsyncClient client = new LoadTestingClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildLoadTestAdministrationAsyncClient();

        RequestOptions reqOpts = new RequestOptions()
                .addQueryParam("orderBy", "lastModifiedDateTime")
                .addQueryParam("maxPageSize", "10");

        PagedFlux<BinaryData> tests = client.listTests(reqOpts);

        tests.subscribe((testBinary) -> {
            try {
                JsonNode test = new ObjectMapper().readTree(testBinary.toString());
                String testId = test.get("testId").asText();
                String displayName = (test.get("displayName") != null) ? test.get("displayName").asText() : "";
                System.out.println(String.format("%s\t%s", testId, displayName));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        // END: java-listOperationsAsync-sample-listTests
    }

    public void listTestRuns() {
        // BEGIN: java-listOperationsAsync-sample-listTestRuns
        LoadTestRunAsyncClient client = new LoadTestingClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildLoadTestRunAsyncClient();

        RequestOptions reqOpts = new RequestOptions()
                .addQueryParam("search", "scenario1")
                .addQueryParam("orderBy", "lastModifiedDateTime")
                .addQueryParam("status", "EXECUTING,DONE")
                .addQueryParam("maxPageSize", "10");

        PagedFlux<BinaryData> testRuns = client.listTestRuns(reqOpts);

        testRuns.subscribe((testRunBinary) -> {
            try {
                JsonNode testRun = new ObjectMapper().readTree(testRunBinary.toString());
                String testRunId = testRun.get("testRunId").asText();
                String testId = testRun.get("testId").asText();
                String displayName = (testRun.get("displayName") != null) ? testRun.get("displayName").asText() : "";
                System.out.println(String.format("%s\t%s\t%s", testRunId, testId, displayName));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        // END: java-listOperationsAsync-sample-listTestRuns
    }

    public void listTestFiles() {
        // BEGIN: java-listOperationsAsync-sample-listTestFiles
        LoadTestAdministrationAsyncClient client = new LoadTestingClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildLoadTestAdministrationAsyncClient();

        String inputTestId = "12345678-1234-1234-1234-123456789abc";

        PagedFlux<BinaryData> files = client.listTestFiles(inputTestId, null);

        files.subscribe((fileBinary) -> {
            try {
                JsonNode file = new ObjectMapper().readTree(fileBinary.toString());
                String url = file.get("url").asText();
                String fileName = file.get("fileName").asText();
                String fileType = file.get("fileType").asText();
                System.out.println(String.format("%s\t%s\t%s", fileName, fileType, url));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        // END: java-listOperationsAsync-sample-listTestFiles
    }
}
