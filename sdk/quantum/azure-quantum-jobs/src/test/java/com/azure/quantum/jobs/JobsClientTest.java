// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.quantum.jobs.models.BlobDetails;
import com.azure.quantum.jobs.models.JobDetails;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.FileSystems;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class JobsClientTest extends QuantumClientTestBase {

    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private JobsClient jobsClient;
    private StorageClient storageClient;

    private void initializeClients(HttpClient httpClient) {
        jobsClient = getClientBuilder(httpClient).buildJobsClient();
        storageClient = getClientBuilder(httpClient).buildStorageClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void jobLifecycleTest(HttpClient httpClient) {

        initializeClients(httpClient);

        //set up job details
        String containerName = "testcontainer";
        String containerUri = storageClient.sasUri(new BlobDetails().setContainerName(containerName)).getSasUri();
        BlobDetails d = new BlobDetails()
            .setContainerName(containerName)
            .setBlobName(String.format("input-%s.json", testResourceNamer.randomUuid()));
        String inputDataUri = storageClient.sasUri(d).getSasUri();
        String jobId = String.format("job-%s", testResourceNamer.randomUuid());
        String jobName = String.format("javaSdkTest-%s", testResourceNamer.randomUuid());
        String inputDataFormat = "microsoft.qio.v2";
        String outputDataFormat = "microsoft.qio-results.v2";
        String providerId = "microsoft";
        String target = "microsoft.paralleltempering-parameterfree.cpu";

        //if in record mode, setup the storage account
        if (!interceptorManager.isPlaybackMode()) {
            // create container if it doesn't exist
            BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .containerName(containerName)
                .endpoint(containerUri)
                .buildClient();
            if (!containerClient.exists()) {
                containerClient.create();
            }

            // upload input data to blob
            BlobClient blobClient = new BlobClientBuilder()
                .endpoint(inputDataUri)
                .buildClient();
            blobClient.uploadFromFile(FileSystems.getDefault().getPath("src/test/resources/problem.json").toString());
        }

        //test create job
        JobDetails createJobDetails = new JobDetails()
            .setContainerUri(containerUri)
            .setInputDataFormat(inputDataFormat)
            .setProviderId(providerId)
            .setTarget(target)
            .setId(jobId)
            .setName(jobName)
            .setOutputDataFormat(outputDataFormat);
        JobDetails jobDetails = jobsClient.create(jobId, createJobDetails);

        assertEquals(inputDataFormat, jobDetails.getInputDataFormat());
        assertEquals(outputDataFormat, jobDetails.getOutputDataFormat());
        assertEquals(providerId, jobDetails.getProviderId());
        assertEquals(target, jobDetails.getTarget());
        assertNotEquals(null, jobDetails.getId());
        assertNotEquals(null, jobDetails.getName());
        assertNotEquals(null, jobDetails.getInputDataUri());
        assertEquals(jobId, jobDetails.getId());
        assertEquals(jobName, jobDetails.getName());

        //test get job
        JobDetails gotJob = jobsClient.get(jobId);
        assertEquals(inputDataFormat, gotJob.getInputDataFormat());
        assertEquals(outputDataFormat, gotJob.getOutputDataFormat());
        assertEquals(providerId, gotJob.getProviderId());
        assertEquals(target, gotJob.getTarget());
        assertNotEquals(null, gotJob.getId());
        assertNotEquals(null, gotJob.getName());
        assertNotEquals(null, gotJob.getInputDataUri());
        assertEquals(jobId, gotJob.getId());
        assertEquals(jobName, gotJob.getName());

        //test list job
        PagedIterable<JobDetails> jobs = jobsClient.list();
        AtomicBoolean jobFound = new AtomicBoolean(false);
        jobs.forEach(job -> {
            if (job.getId().equals(jobId)) {
                jobFound.set(true);
                assertEquals(inputDataFormat, job.getInputDataFormat());
                assertEquals(outputDataFormat, job.getOutputDataFormat());
                assertEquals(providerId, job.getProviderId());
                assertEquals(target, job.getTarget());
                assertEquals(jobName, job.getName());
            }
        });
        assertTrue(jobFound.get());
    }
}
