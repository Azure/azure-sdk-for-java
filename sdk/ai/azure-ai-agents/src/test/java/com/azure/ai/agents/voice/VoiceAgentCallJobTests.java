// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.agents.models.TelephonyCallJob;
import com.azure.ai.agents.models.TelephonyCallJobStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.exception.ResourceNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Recorded direct call-job operations. Jobs are scheduled one day ahead and cancelled before each operation.
 */
public class VoiceAgentCallJobTests extends VoiceAgentTelephonyTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteTelephonyCallJob(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (ScheduledCallJobResource resource = createScheduledCallJob(httpClient, serviceVersion)) {
            resource.client.deleteTelephonyCallJob(resource.agentName, resource.jobId);
            resource.deleted = true;
            assertThrows(ResourceNotFoundException.class,
                () -> resource.client.getTelephonyCallJob(resource.agentName, resource.jobId));
            assertEquals(0L, resource.client.listTelephonyCallJobs(resource.agentName).stream().count());
            assertEquals(204,
                resource.client.deleteTelephonyCallJobWithResponse(resource.agentName, resource.jobId, null)
                    .getStatusCode());
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listTelephonyCallJobs(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (ScheduledCallJobResource resource = createScheduledCallJob(httpClient, serviceVersion)) {
            List<TelephonyCallJob> jobs
                = resource.client
                    .listTelephonyCallJobs(resource.agentName, 1, PageOrder.ASC, null, null,
                        TelephonyCallJobStatus.CANCELLED, null)
                    .stream()
                    .collect(Collectors.toList());
            assertEquals(1, jobs.size());
            TelephonyCallJob job = jobs.get(0);
            assertEquals(resource.jobId, job.getId());
            assertEquals(resource.agentName, job.getAgentName());
            assertNull(job.getCampaignName());
            assertEquals(TelephonyCallJobStatus.CANCELLED, job.getStatus());
            assertEquals(0, job.getAttemptCount());
            assertEquals(0L,
                resource.client
                    .listTelephonyCallJobs(resource.agentName, 1, PageOrder.ASC, job.getId(), null,
                        TelephonyCallJobStatus.CANCELLED, null)
                    .stream()
                    .count());
        }
    }
}
