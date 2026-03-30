// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.DatasetVersion;
import com.azure.ai.projects.models.PendingUploadRequest;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class DatasetsAsyncClientTest extends ClientTestBase {

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateDatasetWithFile(HttpClient httpClient, AIProjectsServiceVersion serviceVersion)
        throws FileNotFoundException, URISyntaxException {
        DatasetsAsyncClient datasetsAsyncClient = getDatasetsAsyncClient(httpClient, serviceVersion);

        String datasetName = "java-test-async-file-" + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersionString = "1";

        Path filePath = getPath("product_info.md");

        StepVerifier.create(datasetsAsyncClient.createDatasetWithFile(datasetName, datasetVersionString, filePath))
            .assertNext(createdDatasetVersion -> assertFileDatasetVersion(createdDatasetVersion, datasetName,
                datasetVersionString, null))
            .verifyComplete();

        StepVerifier.create(datasetsAsyncClient.deleteDatasetVersion(datasetName, datasetVersionString))
            .verifyComplete();
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateDatasetWithFolder(HttpClient httpClient, AIProjectsServiceVersion serviceVersion)
        throws IOException {
        DatasetsAsyncClient datasetsAsyncClient = getDatasetsAsyncClient(httpClient, serviceVersion);

        String datasetName = "java-test-async-folder-" + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersionString = "1";

        Path tempFolder = Files.createTempDirectory("test-folder-dataset");
        Path file1 = tempFolder.resolve("file1.txt");
        Path file2 = tempFolder.resolve("file2.txt");
        Files.write(file1, "Test content 1".getBytes());
        Files.write(file2, "Test content 2".getBytes());

        try {
            StepVerifier
                .create(datasetsAsyncClient.createDatasetWithFolder(datasetName, datasetVersionString, tempFolder))
                .assertNext(createdDatasetVersion -> assertDatasetVersion(createdDatasetVersion, datasetName,
                    datasetVersionString))
                .verifyComplete();

            StepVerifier.create(datasetsAsyncClient.deleteDatasetVersion(datasetName, datasetVersionString))
                .verifyComplete();
        } finally {
            Files.deleteIfExists(file1);
            Files.deleteIfExists(file2);
            Files.deleteIfExists(tempFolder);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListDatasets(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DatasetsAsyncClient datasetsAsyncClient = getDatasetsAsyncClient(httpClient, serviceVersion);

        List<DatasetVersion> datasetsList = new ArrayList<>();

        StepVerifier.create(datasetsAsyncClient.listLatestDatasetVersions().doOnNext(datasetsList::add).then())
            .verifyComplete();

        for (DatasetVersion dataset : datasetsList) {
            assertDatasetVersion(dataset, dataset.getName(), dataset.getVersion());
        }
    }

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateGetAndDeleteDataset(HttpClient httpClient, AIProjectsServiceVersion serviceVersion)
        throws FileNotFoundException, URISyntaxException {
        DatasetsAsyncClient datasetsAsyncClient = getDatasetsAsyncClient(httpClient, serviceVersion);

        String datasetName = "java-test-async-crud-" + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersionString = "1";

        Path filePath = getPath("product_info.md");

        datasetsAsyncClient.createDatasetWithFile(datasetName, datasetVersionString, filePath)
            .block(Duration.ofSeconds(20));

        StepVerifier.create(datasetsAsyncClient.getDatasetVersion(datasetName, datasetVersionString))
            .assertNext(dataset -> assertDatasetVersion(dataset, datasetName, datasetVersionString))
            .verifyComplete();

        List<DatasetVersion> versionsList = new ArrayList<>();
        StepVerifier.create(datasetsAsyncClient.listDatasetVersions(datasetName).doOnNext(versionsList::add).then())
            .verifyComplete();
        boolean found = versionsList.stream().anyMatch(v -> v.getVersion().equals(datasetVersionString));
        Assertions.assertTrue(found, "Created dataset version should appear in listVersions");

        StepVerifier.create(datasetsAsyncClient.deleteDatasetVersion(datasetName, datasetVersionString))
            .verifyComplete();

        StepVerifier.create(datasetsAsyncClient.getDatasetVersion(datasetName, datasetVersionString))
            .expectErrorMatches(e -> e.getMessage().contains("404")
                || e.getMessage().contains("Not Found")
                || e.getMessage().contains("Could not find"))
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testPendingUpload(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DatasetsAsyncClient datasetsAsyncClient = getDatasetsAsyncClient(httpClient, serviceVersion);

        String datasetName = "java-test-async-pending-" + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersion = "1";

        PendingUploadRequest request = new PendingUploadRequest();

        StepVerifier.create(datasetsAsyncClient.pendingUpload(datasetName, datasetVersion, request))
            .assertNext(response -> {
                Assertions.assertNotNull(response);
                Assertions.assertNotNull(response.getBlobReference());
                Assertions.assertNotNull(response.getBlobReference().getBlobUrl());
                Assertions.assertNotNull(response.getBlobReference().getCredential());
            })
            .verifyComplete();
    }
}
