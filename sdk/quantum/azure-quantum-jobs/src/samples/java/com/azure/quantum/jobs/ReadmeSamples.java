// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.rest.PagedIterable;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.quantum.jobs.models.BlobDetails;
import com.azure.quantum.jobs.models.JobDetails;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import java.nio.file.FileSystems;
import java.util.UUID;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    private JobsClient jobsClient = new QuantumClientBuilder().buildJobsClient();
    private StorageClient storageClient = new QuantumClientBuilder().buildStorageClient();
    private String containerName = "containerName";
    private String containerUri = "containerUri";
    private String jobId = "jobId";

    /**
     * Code snippet for creating JobsClient and StorageClient
     */
    public void getClients() {
        // BEGIN: readme-sample-getClients
        JobsClient jobsClient = new QuantumClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .host("{endpoint}")
            .subscriptionId("{subscriptionId}")
            .resourceGroupName("{resourceGroup}")
            .workspaceName("{workspaceName}")
            .buildJobsClient();

        StorageClient storageClient = new QuantumClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .host("{endpoint}")
            .subscriptionId("{subscriptionId}")
            .resourceGroupName("{resourceGroup}")
            .workspaceName("{workspaceName}")
            .buildStorageClient();
        // END: readme-sample-getClients
    }

    /**
     * Code snippet to create a storage container to store data for jobs.
     */
    public void getContainerSasUri() {
        // BEGIN: readme-sample-getContainerSasUri
        // Get container URI with SAS key
        String containerName = "{storageContainerName}";

        // Create container if it doesn't already exist
        BlobContainerClient containerClient = new BlobContainerClientBuilder()
            .containerName(containerName)
            .endpoint(containerUri)
            .buildClient();
        if (!containerClient.exists()) {
            containerClient.create();
        }

        // Get connection string to the container
        String containerUri = storageClient.sasUri(
            new BlobDetails().setContainerName(containerName)
        ).getSasUri();
        // END: readme-sample-getContainerSasUri
    }

    /**
     * Code snippet to upload data to be used for jobs.
     */
    public void uploadInputData() {
        // BEGIN: readme-sample-uploadInputData
        // Get input data blob Uri with SAS key
        String blobName = "{blobName}";
        BlobDetails blobDetails = new BlobDetails()
            .setContainerName(containerName)
            .setBlobName(blobName);
        String inputDataUri = storageClient.sasUri(blobDetails).getSasUri();

        // Upload input data to blob
        BlobClient blobClient = new BlobClientBuilder()
            .endpoint(inputDataUri)
            .buildClient();
        String problemFilePath = FileSystems.getDefault().getPath("src/samples/resources/problem.json").toString();
        blobClient.uploadFromFile(problemFilePath);
        // END: readme-sample-uploadInputData
    }

    /**
     * Code snippet to create a job.
     */
    public void createTheJob() {
        // BEGIN: readme-sample-createTheJob
        String jobId = String.format("job-%s", UUID.randomUUID());
        JobDetails createJobDetails = new JobDetails()
            .setContainerUri(containerUri)
            .setId(jobId)
            .setInputDataFormat("microsoft.qio.v2")
            .setOutputDataFormat("microsoft.qio-results.v2")
            .setProviderId("microsoft")
            .setTarget("microsoft.paralleltempering-parameterfree.cpu")
            .setName("{jobName}");
        JobDetails jobDetails = jobsClient.create(jobId, createJobDetails);
        // END: readme-sample-createTheJob
    }

    /**
     * Code snippet to get a job.
     */
    public void getJob() {
        // BEGIN: readme-sample-getJob
        // Get the job that we've just created based on its jobId
        JobDetails myJob = jobsClient.get(jobId);
        // END: readme-sample-getJob
    }

    /**
     * Code snippet to list all jobs in a workspace.
     */
    public void listJobs() {
        // BEGIN: readme-sample-listJobs
        PagedIterable<JobDetails> jobs = jobsClient.list();
        jobs.forEach(job -> System.out.println(job.getName()));
        // END: readme-sample-listJobs
    }

}
