// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.DatasetVersion;
import com.azure.ai.projects.models.FileDatasetVersion;
import com.azure.ai.projects.models.FolderDatasetVersion;
import com.azure.ai.projects.models.PendingUploadRequest;
import com.azure.ai.projects.models.PendingUploadResponse;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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

    private AIProjectClientBuilder clientBuilder;
    private DatasetsClient datasetsClient;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        datasetsClient = clientBuilder.buildDatasetsClient();
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

        FileDatasetVersion createdDatasetVersion
            = datasetsClient.createDatasetWithFile(datasetName, datasetVersionString, filePath);

        assertFileDatasetVersion(createdDatasetVersion, datasetName, datasetVersionString, null);
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
            FolderDatasetVersion createdDatasetVersion
                = datasetsClient.createDatasetWithFolder(datasetName, datasetVersionString, tempFolder);

            assertDatasetVersion(createdDatasetVersion, datasetName, datasetVersionString);
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

        // Verify that listing datasets returns results
        Iterable<DatasetVersion> datasets = datasetsClient.listLatestDatasetVersions();
        Assertions.assertNotNull(datasets);

        // Verify that at least one dataset can be retrieved
        // Note: This test assumes at least one dataset exists
        datasets.forEach(dataset -> {
            assertDatasetVersion(dataset, dataset.getName(), dataset.getVersion());
        });
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListDatasetVersions(HttpClient httpClient) {
        setup(httpClient);

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");

        // Verify that listing dataset versions returns results
        Iterable<DatasetVersion> versions = datasetsClient.listDatasetVersions(datasetName);
        Assertions.assertNotNull(versions);

        // Verify that at least one dataset version can be retrieved
        // Note: This test assumes the specified dataset exists with at least one version
        versions.forEach(version -> {
            assertDatasetVersion(version, datasetName, version.getVersion());
        });
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetDataset(HttpClient httpClient) {
        setup(httpClient);

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        // Get a specific dataset version
        DatasetVersion dataset = datasetsClient.getDatasetVersion(datasetName, datasetVersion);

        // Verify the dataset properties
        assertDatasetVersion(dataset, datasetName, datasetVersion);
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

        // Create or update the dataset
        FileDatasetVersion createdDataset = (FileDatasetVersion) datasetsClient
            .createOrUpdateDatasetVersion(datasetName, datasetVersion, fileDataset);

        // Verify the created dataset
        assertFileDatasetVersion(createdDataset, datasetName, datasetVersion, dataUri);
        Assertions.assertEquals("Test dataset created via SDK tests", createdDataset.getDescription());
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

        // Get the pending upload response
        PendingUploadResponse response = datasetsClient.pendingUpload(datasetName, datasetVersion, request);

        // Verify the response
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getPendingUploadId());
        Assertions.assertNotNull(response.getBlobReference());
        Assertions.assertNotNull(response.getBlobReference().getBlobUri());
        Assertions.assertNotNull(response.getBlobReference().getCredential());
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

        // Create a dataset
        FileDatasetVersion createdDataset = datasetsClient.createDatasetWithFile(datasetName, datasetVersion, filePath);
        Assertions.assertNotNull(createdDataset);

        // Delete the dataset
        datasetsClient.deleteDatasetVersion(datasetName, datasetVersion);

        // Verify deletion - this should throw ResourceNotFoundException
        try {
            datasetsClient.getDatasetVersion(datasetName, datasetVersion);
            Assertions.fail("Expected ResourceNotFoundException was not thrown");
        } catch (Exception e) {
            // Expected exception
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }

}
