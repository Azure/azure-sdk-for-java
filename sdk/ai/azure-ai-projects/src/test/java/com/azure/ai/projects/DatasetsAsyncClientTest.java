// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.DatasetVersion;
import com.azure.ai.projects.models.FileDatasetVersion;
import com.azure.ai.projects.models.PendingUploadRequest;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

@Disabled("Disabled for lack of recordings. Needs to be enabled on the Public Preview release.")
public class DatasetsAsyncClientTest extends ClientTestBase {

    private AIProjectClientBuilder clientBuilder;
    private DatasetsAsyncClient datasetsAsyncClient;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        datasetsAsyncClient = clientBuilder.buildDatasetsAsyncClient();
    }

    /**
     * Helper method to validate common properties of a DatasetVersion
     *
     * @param datasetVersion The dataset version to validate
     * @param expectedName The expected name of the dataset
     * @param expectedVersion The expected version string
     */
    private void assertDatasetVersion(DatasetVersion datasetVersion, String expectedName, String expectedVersion) {
        Assertions.assertNotNull(datasetVersion, "Dataset version should not be null");
        Assertions.assertEquals(expectedName, datasetVersion.getName(), "Dataset name should match expected value");
        Assertions.assertEquals(expectedVersion, datasetVersion.getVersion(),
            "Dataset version should match expected value");
        Assertions.assertNotNull(datasetVersion.getType(), "Dataset type should not be null");
    }

    /**
     * Helper method to validate common properties of a FileDatasetVersion
     *
     * @param fileDatasetVersion The file dataset version to validate
     * @param expectedName The expected name of the dataset
     * @param expectedVersion The expected version string
     * @param expectedDataUri The expected data URI (optional)
     */
    private void assertFileDatasetVersion(FileDatasetVersion fileDatasetVersion, String expectedName,
        String expectedVersion, String expectedDataUri) {
        assertDatasetVersion(fileDatasetVersion, expectedName, expectedVersion);
        if (expectedDataUri != null) {
            Assertions.assertEquals(expectedDataUri, fileDatasetVersion.getDataUri(),
                "Dataset dataUri should match expected value");
        }
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateDatasetWithFile(HttpClient httpClient) throws FileNotFoundException, URISyntaxException {
        setup(httpClient);

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersionString = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        Path filePath = getPath("product_info.md");

        StepVerifier.create(datasetsAsyncClient.createDatasetWithFile(datasetName, datasetVersionString, filePath))
            .assertNext(createdDatasetVersion -> assertFileDatasetVersion(createdDatasetVersion, datasetName,
                datasetVersionString, null))
            .verifyComplete();
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateDatasetWithFolder(HttpClient httpClient) throws IOException, URISyntaxException {
        setup(httpClient);

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "folder-dataset")
            + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersionString = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        // Create a temporary folder with test files
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
        } finally {
            // Clean up temporary files
            Files.deleteIfExists(file1);
            Files.deleteIfExists(file2);
            Files.deleteIfExists(tempFolder);
        }
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListDatasets(HttpClient httpClient) {
        setup(httpClient);

        // Collect datasets into a list
        List<DatasetVersion> datasetsList = new ArrayList<>();

        StepVerifier.create(datasetsAsyncClient.list().doOnNext(datasetsList::add).then()).verifyComplete();

        // Verify we found at least one dataset
        Assertions.assertFalse(datasetsList.isEmpty(), "Expected at least one dataset");

        // Verify each dataset
        for (DatasetVersion dataset : datasetsList) {
            assertDatasetVersion(dataset, dataset.getName(), dataset.getVersion());
        }
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListDatasetVersions(HttpClient httpClient) {
        setup(httpClient);

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");

        // Collect dataset versions into a list
        List<DatasetVersion> versionsList = new ArrayList<>();

        StepVerifier.create(datasetsAsyncClient.listVersions(datasetName).doOnNext(versionsList::add).then())
            .verifyComplete();

        // Verify we found at least one version
        Assertions.assertFalse(versionsList.isEmpty(), "Expected at least one dataset version");

        // Verify each version
        for (DatasetVersion version : versionsList) {
            assertDatasetVersion(version, datasetName, version.getVersion());
        }
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetDataset(HttpClient httpClient) {
        setup(httpClient);

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        StepVerifier.create(datasetsAsyncClient.get(datasetName, datasetVersion))
            .assertNext(dataset -> assertDatasetVersion(dataset, datasetName, datasetVersion))
            .verifyComplete();
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testCreateOrUpdateDataset(HttpClient httpClient) {
        setup(httpClient);

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "updated-dataset")
            + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");
        String dataUri = Configuration.getGlobalConfiguration().get("DATA_URI", "https://example.com/data.txt");

        // Create a new FileDatasetVersion
        FileDatasetVersion fileDataset
            = new FileDatasetVersion().setDataUri(dataUri).setDescription("Test dataset created via SDK tests");

        StepVerifier.create(datasetsAsyncClient.createOrUpdate(datasetName, datasetVersion, fileDataset))
            .assertNext(createdDataset -> {
                FileDatasetVersion fileDatasetVersion = (FileDatasetVersion) createdDataset;
                assertFileDatasetVersion(fileDatasetVersion, datasetName, datasetVersion, dataUri);
                Assertions.assertEquals("Test dataset created via SDK tests", fileDatasetVersion.getDescription());
            })
            .verifyComplete();
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testPendingUpload(HttpClient httpClient) {
        setup(httpClient);

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "pending-upload-dataset")
            + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        // Create a pending upload request
        PendingUploadRequest request = new PendingUploadRequest();

        StepVerifier.create(datasetsAsyncClient.pendingUpload(datasetName, datasetVersion, request))
            .assertNext(response -> {
                Assertions.assertNotNull(response);
                Assertions.assertNotNull(response.getPendingUploadId());
                Assertions.assertNotNull(response.getBlobReference());
                Assertions.assertNotNull(response.getBlobReference().getBlobUri());
                Assertions.assertNotNull(response.getBlobReference().getCredential());
            })
            .verifyComplete();
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testDeleteDataset(HttpClient httpClient) throws FileNotFoundException, URISyntaxException {
        setup(httpClient);

        // First create a dataset that we can then delete
        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "delete-test-dataset")
            + UUID.randomUUID().toString().substring(0, 8);
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        Path filePath = getPath("product_info.md");

        // Create and verify a dataset exists first
        datasetsAsyncClient.createDatasetWithFile(datasetName, datasetVersion, filePath).block(); // We need to ensure the dataset is created before continuing

        // Delete the dataset
        StepVerifier.create(datasetsAsyncClient.delete(datasetName, datasetVersion)).verifyComplete();

        // Verify deletion - this should cause an error
        StepVerifier.create(datasetsAsyncClient.get(datasetName, datasetVersion))
            .expectErrorMatches(e -> e.getMessage().contains("404") || e.getMessage().contains("Not Found"))
            .verify();
    }
}
