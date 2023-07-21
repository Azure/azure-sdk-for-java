// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.quantum.jobs.models.BlobDetails;
import com.azure.quantum.jobs.models.JobDetails;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.FileSystems;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobsAsyncClientTest extends QuantumClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private JobsAsyncClient asyncJobsClient;

    private JobsClient jobsClient;

    private StorageClient storageClient;

    private void initializeClient(HttpClient httpClient) {
        asyncJobsClient = getClientBuilder(httpClient).buildJobsAsyncClient();
        jobsClient = getClientBuilder(httpClient).buildJobsClient();
        storageClient = getClientBuilder(httpClient).buildStorageClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void listAsyncTest(HttpClient httpClient) {
        initializeClient(httpClient);

        createJobs();

        StepVerifier.create(asyncJobsClient.list().collect(Collectors.toList()))
            .assertNext(jobs -> {
                assertTrue(jobs.size() >= 203);
                for (JobDetails job : jobs) {
                    assertEquals("ionq.simulator", job.getTarget());
                    assertTrue(job.getName().startsWith("javaSdkTest"));
                    assertEquals(null, job.getCancellationTime());
                    assertEquals("microsoft.qio.v2", job.getInputDataFormat());
                    assertEquals("microsoft.qio-results.v2", job.getOutputDataFormat());
                    assertEquals("ionq", job.getProviderId());
                }
            }).verifyComplete();
    }

    private void createJobs() {

        Integer jobs = jobsClient.list().stream().collect(Collectors.toList()).size();

        if (jobs < 203) {
            for (int i = 0; i < 203 - jobs; i++) {
                JobDetails jobDetails = getJobDetail();
                jobsClient.create(jobDetails.getId(), jobDetails);
            }
        }
    }

    private JobDetails getJobDetail() {
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
        String providerId = "ionq";
        String target = "ionq.simulator";

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

        return new JobDetails()
            .setContainerUri(containerUri)
            .setInputDataFormat(inputDataFormat)
            .setProviderId(providerId)
            .setTarget(target)
            .setId(jobId)
            .setName(jobName)
            .setOutputDataFormat(outputDataFormat);
    }
}
