// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.DatasetVersion;
import com.azure.ai.projects.models.FileDatasetVersion;
import com.azure.ai.projects.models.PendingUploadRequest;
import com.azure.ai.projects.models.PendingUploadResponse;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class DatasetsAsyncSample {

    private static DatasetsAsyncClient datasetsAsyncClient
            = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildDatasetsAsyncClient();

    public static void main(String[] args) throws IOException, URISyntaxException {
        // Using block() to wait for the async operations to complete in the sample
        //createDatasetWithFile().block();
        listDatasets().blockLast();
        //listDatasetVersions().blockLast();
        //getDataset().block();
        //pendingUploadSample().block();
        //deleteDataset().block();
        //createOrUpdateDataset().block();
    }

    public static Mono<FileDatasetVersion> createDatasetWithFile() throws IOException, URISyntaxException {
        // BEGIN:com.azure.ai.projects.DatasetsAsyncSample.createDatasetWithFile

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersionString = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        Path filePath = getPath("product_info.md");

        return datasetsAsyncClient.createDatasetWithFile(datasetName, datasetVersionString, filePath)
            .doOnNext(createdDatasetVersion -> 
                System.out.println("Created dataset version: " + createdDatasetVersion.getId()));

        // END:com.azure.ai.projects.DatasetsAsyncSample.createDatasetWithFile
    }

    public static Flux<DatasetVersion> listDatasets() {
        // BEGIN:com.azure.ai.projects.DatasetsAsyncSample.listDatasets

        System.out.println("Listing all datasets (latest versions):");
        return datasetsAsyncClient.listLatest()
            .doOnNext(dataset -> {
                System.out.println("\nDataset name: " + dataset.getName());
                System.out.println("Dataset Id: " + dataset.getId());
                System.out.println("Dataset version: " + dataset.getVersion());
                System.out.println("Dataset type: " + dataset.getType());
                if (dataset.getDescription() != null) {
                    System.out.println("Description: " + dataset.getDescription());
                }
            });

        // END:com.azure.ai.projects.DatasetsAsyncSample.listDatasets
    }

    public static Flux<DatasetVersion> listDatasetVersions() {
        // BEGIN:com.azure.ai.projects.DatasetsAsyncSample.listDatasetVersions

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "test");

        System.out.println("Listing all versions of dataset: " + datasetName);
        return datasetsAsyncClient.listVersions(datasetName)
            .doOnNext(version -> {
                System.out.println("\nDataset name: " + version.getName());
                System.out.println("Dataset version: " + version.getVersion());
                System.out.println("Dataset type: " + version.getType());
                if (version.getDataUri() != null) {
                    System.out.println("Data URI: " + version.getDataUri());
                }
            });

        // END:com.azure.ai.projects.DatasetsAsyncSample.listDatasetVersions
    }

    public static Mono<DatasetVersion> getDataset() {
        // BEGIN:com.azure.ai.projects.DatasetsAsyncSample.getDataset

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "test");
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1");

        return datasetsAsyncClient.getDatasetVersion(datasetName, datasetVersion)
            .doOnNext(dataset -> {
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
            });

        // END:com.azure.ai.projects.DatasetsAsyncSample.getDataset
    }

    public static Mono<Void> deleteDataset() {
        // BEGIN:com.azure.ai.projects.DatasetsAsyncSample.deleteDataset

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        // Delete the specific version of the dataset
        return datasetsAsyncClient.deleteVersion(datasetName, datasetVersion)
            .doOnSuccess(unused -> 
                System.out.println("Deleted dataset: " + datasetName + ", version: " + datasetVersion));

        // END:com.azure.ai.projects.DatasetsAsyncSample.deleteDataset
    }

    public static Mono<DatasetVersion> createOrUpdateDataset() {
        // BEGIN:com.azure.ai.projects.DatasetsAsyncSample.createOrUpdateDataset

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");
        String dataUri = Configuration.getGlobalConfiguration().get("DATA_URI", "https://example.com/data.txt");

        // Create a new FileDatasetVersion with provided dataUri
        FileDatasetVersion fileDataset = new FileDatasetVersion()
            .setDataUri(dataUri)
            .setDescription("Sample dataset created via SDK");

        // Create or update the dataset
        return datasetsAsyncClient.createOrUpdateVersion(
            datasetName, 
            datasetVersion, 
            fileDataset
        ).doOnNext(createdDataset -> {
            FileDatasetVersion fileDatasetVersion = (FileDatasetVersion) createdDataset;
            System.out.println("Created/Updated dataset:");
            System.out.println("Name: " + fileDatasetVersion.getName());
            System.out.println("Version: " + fileDatasetVersion.getVersion());
            System.out.println("Data URI: " + fileDatasetVersion.getDataUri());
        });

        // END:com.azure.ai.projects.DatasetsAsyncSample.createOrUpdateDataset
    }

    public static Mono<PendingUploadResponse> pendingUploadSample() {
        // BEGIN:com.azure.ai.projects.DatasetsAsyncSample.pendingUploadSample

        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

        // Create a pending upload request for the dataset
        PendingUploadRequest request = new PendingUploadRequest();
        
        // Get the pending upload response with blob reference
        return datasetsAsyncClient.pendingUpload(datasetName, datasetVersion, request)
            .doOnNext(response -> {
                System.out.println("Pending upload initiated with ID: " + response.getPendingUploadId());
                System.out.println("Blob URI: " + response.getBlobReference().getBlobUri());
            });

        // END:com.azure.ai.projects.DatasetsAsyncSample.pendingUploadSample
    }

    public static Path getPath(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = DatasetsAsyncSample.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }

        File file = new File(resource.toURI());
        return file.toPath();
    }
}
