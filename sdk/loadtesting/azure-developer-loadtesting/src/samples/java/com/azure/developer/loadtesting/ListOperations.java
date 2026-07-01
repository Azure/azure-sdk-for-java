// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import java.io.IOException;
import java.util.Map;

/**
 * Sample demonstrates how to list tests, test files and test runs for a given resource.
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
    }

    public static void listTests() {
        // BEGIN: java-listOperations-sample-listTests
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        RequestOptions reqOpts = new RequestOptions()
            .addQueryParam("orderBy", "lastModifiedDateTime")
            .addQueryParam("maxPageSize", "10");

        PagedIterable<BinaryData> tests = client.listTests(reqOpts);

        tests.forEach(testBinary -> {
            try (JsonReader jsonReader = JsonProviders.createReader(testBinary.toBytes())) {
                Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                String testId = jsonTree.get("testId").toString();
                String displayName = (jsonTree.get("displayName") != null)
                    ? jsonTree.get("displayName").toString()
                    : "";
                System.out.println(String.format("%s\t%s", testId, displayName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // END: java-listOperations-sample-listTests
    }

    public static void listTestRuns() {
        // BEGIN: java-listOperations-sample-listTestRuns
        LoadTestRunClient client = new LoadTestRunClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<endpoint>")
            .buildClient();

        RequestOptions reqOpts = new RequestOptions()
            .addQueryParam("search", "scenario1")
            .addQueryParam("orderBy", "lastModifiedDateTime")
            .addQueryParam("status", "EXECUTING,DONE")
            .addQueryParam("maxPageSize", "10");

        PagedIterable<BinaryData> testRuns = client.listTestRuns(reqOpts);

        testRuns.forEach(testRunBinary -> {
            try (JsonReader jsonReader = JsonProviders.createReader(testRunBinary.toBytes())) {
                Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                String testRunId = jsonTree.get("testRunId").toString();
                String testId = jsonTree.get("testId").toString();
                String displayName = (jsonTree.get("displayName") != null)
                    ? jsonTree.get("displayName").toString()
                    : "";
                System.out.println(String.format("%s\t%s\t%s", testRunId, testId, displayName));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        PagedIterable<BinaryData> files = client.listTestFiles(inputTestId, null);

        files.forEach(fileBinary -> {
            try (JsonReader jsonReader = JsonProviders.createReader(fileBinary.toBytes())) {
                Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

                String blobUrl = jsonTree.get("url").toString();
                String fileName = jsonTree.get("fileName").toString();
                String fileType = jsonTree.get("fileType").toString();
                System.out.println(String.format("%s\t%s\t%s", fileName, fileType, blobUrl));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // END: java-listOperations-sample-listTestFiles
    }
}
