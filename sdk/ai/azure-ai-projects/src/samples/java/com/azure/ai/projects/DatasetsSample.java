// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.DatasetVersion;
import com.azure.ai.projects.models.FileDatasetVersion;
import com.azure.ai.projects.models.PendingUploadRequest;
import com.azure.ai.projects.models.PendingUploadResponse;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class DatasetsSample {

    private static DatasetsClient datasetsClient
            = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildDatasetsClient();

    public static void main(String[] args) throws IOException, URISyntaxException {

        createDatasetWithFile();
        listDatasets();
        //listDatasetVersions();
        //getDataset();
        //pendingUploadSample();

        //deleteDataset();
        //createOrUpdateDataset();
    }

    public static void createDatasetWithFile() throws IOException, URISyntaxException {
        // BEGIN:com.azure.ai.projects.DatasetsSample.createDatasetWithFile

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersionString = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        Path filePath = getPath("product_info.md");

        FileDatasetVersion createdDatasetVersion = datasetsClient.createDatasetWithFile(datasetName, datasetVersionString, filePath);

        System.out.println("Created dataset version: " + createdDatasetVersion.getId());

        // END:com.azure.ai.projects.DatasetsSample.createDatasetWithFile
    }

    public static void listDatasets() {
        // BEGIN:com.azure.ai.projects.DatasetsSample.listDatasets

        System.out.println("Listing all datasets (latest versions):");
        datasetsClient.listLatestDatasetVersions().forEach(dataset -> {
            System.out.println("\nDataset name: " + dataset.getName());
            System.out.println("Dataset Id: " + dataset.getId());
            System.out.println("Dataset version: " + dataset.getVersion());
            System.out.println("Dataset type: " + dataset.getType());
            if (dataset.getDescription() != null) {
                System.out.println("Description: " + dataset.getDescription());
            }
        });

        // END:com.azure.ai.projects.DatasetsSample.listDatasets
    }

    public static void listDatasetVersions() {
        // BEGIN:com.azure.ai.projects.DatasetsSample.listDatasetVersions

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "test");

        System.out.println("Listing all versions of dataset: " + datasetName);
        datasetsClient.listDatasetVersions(datasetName).forEach(version -> {
            System.out.println("\nDataset name: " + version.getName());
            System.out.println("Dataset version: " + version.getVersion());
            System.out.println("Dataset type: " + version.getType());
            if (version.getDataUri() != null) {
                System.out.println("Data URI: " + version.getDataUri());
            }
        });

        // END:com.azure.ai.projects.DatasetsSample.listDatasetVersions
    }

    public static void getDataset() {
        // BEGIN:com.azure.ai.projects.DatasetsSample.getDataset

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "test");
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1");

        DatasetVersion dataset = datasetsClient.getDatasetVersion(datasetName, datasetVersion);

        System.out.println("Retrieved dataset:");
        System.out.println("Name: " + dataset.getName());
        System.out.println("Version: " + dataset.getVersion());
        System.out.println("Type: " + dataset.getType());
        if (dataset.getDataUri() != null) {
            System.out.println("Data URI: " + dataset.getDataUri());
        }
        if (dataset.getDescription() != null) {
            System.out.println("Description: " + dataset.getDescription());
        }

        // END:com.azure.ai.projects.DatasetsSample.getDataset
    }

    public static void deleteDataset() {
        // BEGIN:com.azure.ai.projects.DatasetsSample.deleteDataset

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        // Delete the specific version of the dataset
        datasetsClient.deleteDatasetVersion(datasetName, datasetVersion);

        System.out.println("Deleted dataset: " + datasetName + ", version: " + datasetVersion);

        // END:com.azure.ai.projects.DatasetsSample.deleteDataset
    }

    public static void createOrUpdateDataset() {
        // BEGIN:com.azure.ai.projects.DatasetsSample.createOrUpdateDataset

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");
        String dataUri = Configuration.getGlobalConfiguration().get("DATA_URI", "https://example.com/data.txt");

        // Create a new FileDatasetVersion with provided dataUri
        FileDatasetVersion fileDataset = new FileDatasetVersion()
            .setDataUri(dataUri)
            .setDescription("Sample dataset created via SDK");

        // Create or update the dataset
        FileDatasetVersion createdDataset = (FileDatasetVersion) datasetsClient.createOrUpdateDatasetVersion(
            datasetName, 
            datasetVersion, 
            fileDataset
        );

        System.out.println("Created/Updated dataset:");
        System.out.println("Name: " + createdDataset.getName());
        System.out.println("Version: " + createdDataset.getVersion());
        System.out.println("Data URI: " + createdDataset.getDataUri());

        // END:com.azure.ai.projects.DatasetsSample.createOrUpdateDataset
    }

    public static void pendingUploadSample() throws URISyntaxException, IOException {
        // BEGIN:com.azure.ai.projects.DatasetsSample.pendingUploadSample

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        // Create a pending upload request for the dataset
        PendingUploadRequest request = new PendingUploadRequest();
        
        // Get the pending upload response with blob reference
        PendingUploadResponse response = datasetsClient.pendingUpload(datasetName, datasetVersion, request);
        
        System.out.println("Pending upload initiated with ID: " + response.getPendingUploadId());
        System.out.println("Blob URI: " + response.getBlobReference().getBlobUri());

        // END:com.azure.ai.projects.DatasetsSample.pendingUploadSample
    }

    public static Path getPath(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = DatasetsSample.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }

        File file = new File(resource.toURI());
        return file.toPath();
    }
}
