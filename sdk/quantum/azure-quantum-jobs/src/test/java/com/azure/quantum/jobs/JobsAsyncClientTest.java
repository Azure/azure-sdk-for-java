// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.quantum.jobs.models.JobDetails;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JobsAsyncClientTest extends QuantumClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private JobsAsyncClient client;

    private void initializeClient(HttpClient httpClient) {
        client = getClientBuilder(httpClient).buildJobsAsyncClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void listAsyncTest(HttpClient httpClient) {
        initializeClient(httpClient);

        List<JobDetails> jobs = client.list().collectList().block();

        assertEquals(203, jobs.size());

        //check specifics on the first job
        JobDetails firstJob = jobs.get(0);
        assertEquals("microsoft.paralleltempering-parameterfree.cpu", firstJob.getTarget());
        assertEquals("1be7199c-5d16-11eb-8f86-3e22fb0c562e", firstJob.getId());
        assertEquals("first-demo", firstJob.getName());

        //check constant values on all jobs
        for (JobDetails job : jobs) {
            assertEquals(null, job.getCancellationTime());
            assertEquals("microsoft.qio.v2", job.getInputDataFormat());
            assertEquals("microsoft.qio-results.v2", job.getOutputDataFormat());
            assertEquals("microsoft", job.getProviderId());
            assertEquals("Sanitized", job.getContainerUri());
            assertEquals("Sanitized", job.getInputDataUri());
            assertEquals("Sanitized", job.getOutputDataUri());
        }
    }
}
