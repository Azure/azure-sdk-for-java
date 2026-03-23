// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.DatasetVersion;
import com.azure.ai.projects.models.FileDatasetVersion;
import com.azure.ai.projects.models.FolderDatasetVersion;
import com.azure.ai.projects.models.PendingUploadRequest;
import com.azure.ai.projects.models.PendingUploadResponse;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class DatasetsClientTest extends ClientTestBase {

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateDatasetWithFile(HttpClient httpClient, AIProjectsServiceVersion serviceVersion)
        throws FileNotFoundException, URISyntaxException {
        DatasetsClient datasetsClient = getDatasetsClient(httpClient, serviceVersion);

        String datasetName = "java-test-file-" + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersionString = "1";

        Path filePath = getPath("product_info.md");

        FileDatasetVersion createdDatasetVersion
            = datasetsClient.createDatasetWithFile(datasetName, datasetVersionString, filePath);

        assertFileDatasetVersion(createdDatasetVersion, datasetName, datasetVersionString, null);

        datasetsClient.deleteVersion(datasetName, datasetVersionString);
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateDatasetWithFolder(HttpClient httpClient, AIProjectsServiceVersion serviceVersion)
        throws IOException {
        DatasetsClient datasetsClient = getDatasetsClient(httpClient, serviceVersion);

        String datasetName = "java-test-folder-" + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersionString = "1";

        Path tempFolder = Files.createTempDirectory("test-folder-dataset");
        Path file1 = tempFolder.resolve("file1.txt");
        Path file2 = tempFolder.resolve("file2.txt");
        Files.write(file1, "Test content 1".getBytes());
        Files.write(file2, "Test content 2".getBytes());

        try {
            FolderDatasetVersion createdDatasetVersion
                = datasetsClient.createDatasetWithFolder(datasetName, datasetVersionString, tempFolder);

            assertDatasetVersion(createdDatasetVersion, datasetName, datasetVersionString);

            datasetsClient.deleteVersion(datasetName, datasetVersionString);
        } finally {
            Files.deleteIfExists(file1);
            Files.deleteIfExists(file2);
            Files.deleteIfExists(tempFolder);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListDatasets(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DatasetsClient datasetsClient = getDatasetsClient(httpClient, serviceVersion);

        Iterable<DatasetVersion> datasets = datasetsClient.listLatestVersion();
        Assertions.assertNotNull(datasets);

        datasets.forEach(dataset -> {
            assertDatasetVersion(dataset, dataset.getName(), dataset.getVersion());
        });
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateGetAndDeleteDataset(HttpClient httpClient, AIProjectsServiceVersion serviceVersion)
        throws FileNotFoundException, URISyntaxException {
        DatasetsClient datasetsClient = getDatasetsClient(httpClient, serviceVersion);

        String datasetName = "java-test-crud-" + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersionString = "1";

        Path filePath = getPath("product_info.md");

        FileDatasetVersion createdDataset
            = datasetsClient.createDatasetWithFile(datasetName, datasetVersionString, filePath);
        Assertions.assertNotNull(createdDataset);
        assertFileDatasetVersion(createdDataset, datasetName, datasetVersionString, null);

        DatasetVersion retrievedDataset = datasetsClient.getDatasetVersion(datasetName, datasetVersionString);
        assertDatasetVersion(retrievedDataset, datasetName, datasetVersionString);

        Iterable<DatasetVersion> versions = datasetsClient.listVersions(datasetName);
        Assertions.assertNotNull(versions);
        boolean found = false;
        for (DatasetVersion version : versions) {
            if (version.getVersion().equals(datasetVersionString)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Created dataset version should appear in listVersions");

        datasetsClient.deleteVersion(datasetName, datasetVersionString);

        try {
            datasetsClient.getDatasetVersion(datasetName, datasetVersionString);
            Assertions.fail("Expected ResourceNotFoundException was not thrown");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("404")
                || e.getMessage().contains("Not Found")
                || e.getMessage().contains("Could not find"));
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testPendingUpload(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DatasetsClient datasetsClient = getDatasetsClient(httpClient, serviceVersion);

        String datasetName = "java-test-pending-" + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersion = "1";

        PendingUploadRequest request = new PendingUploadRequest();

        PendingUploadResponse response = datasetsClient.pendingUpload(datasetName, datasetVersion, request);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getBlobReference());
        Assertions.assertNotNull(response.getBlobReference().getBlobUrl());
        Assertions.assertNotNull(response.getBlobReference().getCredential());
    }
}
