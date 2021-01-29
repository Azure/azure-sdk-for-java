// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.quantum.jobs.models.JobDetails;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

public class JobsClientTest extends QuantumClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private JobsClient client;

    private void initializeClient(HttpClient httpClient) {
        client = getClientBuilder(httpClient).buildJobsClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void listTest(HttpClient httpClient) {
        initializeClient(httpClient);

        PagedIterable<JobDetails> jobs = client.list();
        AtomicInteger total = new AtomicInteger();

        //check constant values on all jobs
        jobs.forEach(job -> {
            assertEquals(null, job.getCancellationTime());
            assertEquals("microsoft.qio.v2", job.getInputDataFormat());
            assertEquals("microsoft.qio-results.v2", job.getOutputDataFormat());
            assertEquals("microsoft", job.getProviderId());
            assertEquals("Sanitized", job.getContainerUri());
            assertEquals("Sanitized", job.getOutputDataUri());
            assertEquals("Sanitized", job.getInputDataUri());
            total.getAndIncrement();
        });
        assertEquals(203, total.intValue());
    }
}
