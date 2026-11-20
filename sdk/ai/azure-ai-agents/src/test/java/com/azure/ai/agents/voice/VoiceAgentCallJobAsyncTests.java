// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.agents.models.TelephonyCallJobStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.exception.ResourceNotFoundException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Asynchronous call-job recordings using cancelled, future-scheduled jobs and no immediate calls.
 */
public class VoiceAgentCallJobAsyncTests extends VoiceAgentTelephonyTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteTelephonyCallJob(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (ScheduledCallJobResource resource = createScheduledCallJob(httpClient, serviceVersion)) {
            StepVerifier.create(resource.asyncClient.deleteTelephonyCallJob(resource.agentName, resource.jobId)
                .doOnSuccess(ignored -> resource.deleted = true)).verifyComplete();
            StepVerifier.create(resource.asyncClient.getTelephonyCallJob(resource.agentName, resource.jobId))
                .expectError(ResourceNotFoundException.class)
                .verify(TIMEOUT);
            StepVerifier.create(resource.asyncClient.listTelephonyCallJobs(resource.agentName).count())
                .expectNext(0L)
                .verifyComplete();
            StepVerifier
                .create(
                    resource.asyncClient.deleteTelephonyCallJobWithResponse(resource.agentName, resource.jobId, null))
                .assertNext(response -> assertEquals(204, response.getStatusCode()))
                .verifyComplete();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listTelephonyCallJobs(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (ScheduledCallJobResource resource = createScheduledCallJob(httpClient, serviceVersion)) {
            StepVerifier
                .create(
                    resource.asyncClient
                        .listTelephonyCallJobs(resource.agentName, 1, PageOrder.ASC, null, null,
                            TelephonyCallJobStatus.CANCELLED, null)
                        .collectList())
                .assertNext(jobs -> {
                    assertEquals(1, jobs.size());
                    assertEquals(resource.jobId, jobs.get(0).getId());
                    assertEquals(resource.agentName, jobs.get(0).getAgentName());
                    assertNull(jobs.get(0).getCampaignName());
                    assertEquals(TelephonyCallJobStatus.CANCELLED, jobs.get(0).getStatus());
                    assertEquals(0, jobs.get(0).getAttemptCount());
                })
                .verifyComplete();
            StepVerifier
                .create(resource.asyncClient
                    .listTelephonyCallJobs(resource.agentName, 1, PageOrder.ASC, resource.jobId, null,
                        TelephonyCallJobStatus.CANCELLED, null)
                    .count())
                .expectNext(0L)
                .verifyComplete();
        }
    }
}
