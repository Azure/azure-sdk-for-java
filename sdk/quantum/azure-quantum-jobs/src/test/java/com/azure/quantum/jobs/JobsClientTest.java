// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.quantum.jobs.models.BlobDetails;
import com.azure.quantum.jobs.models.JobDetails;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@LiveOnly
public class JobsClientTest extends QuantumClientTestBase {
    // LiveOnly because "SAS URL and tokens cannot be stored in recordings."
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
        String qirFilePath = FileSystems.getDefault().getPath("src/samples/java/com/azure/quantum/jobs/BellState.bc").toString();
        BlobDetails d = new BlobDetails()
            .setContainerName(containerName)
            .setBlobName(String.format("input-%s.bc", testResourceNamer.randomUuid()));
        BlobHttpHeaders blobHttpHeaders = new BlobHttpHeaders()
            .setContentType("qir.v1");
        String inputDataUri = storageClient.sasUri(d).getSasUri();
        String jobId = String.format("%s", testResourceNamer.randomUuid());
        String jobName = String.format("javaSdkTest-%s", jobId);
        String inputDataFormat = "qir.v1";
        String outputDataFormat = "microsoft.quantum-results.v1";
        String providerId = "quantinuum";
        String target = "quantinuum.sim.h1-1e";

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
            blobClient.uploadFromFile(qirFilePath, null, blobHttpHeaders, null, null, null, null);
        }

        //test create job
        Map<String, Object> inputParams = new HashMap<String, Object>();
        inputParams.put("entryPoint", "ENTRYPOINT__BellState");
        inputParams.put("arguments", new ArrayList<String>());
        inputParams.put("targetCapability", "AdaptiveExecution");
        JobDetails createJobDetails = new JobDetails()
            .setContainerUri(containerUri)
            .setId(jobId)
            .setInputDataFormat(inputDataFormat)
            .setOutputDataFormat(outputDataFormat)
            .setProviderId(providerId)
            .setTarget(target)
            .setName(jobName)
            .setInputParams(inputParams);
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
