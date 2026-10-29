// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentOptimizationCandidate;
import com.azure.ai.agents.models.AgentOptimizationCandidateExpand;
import com.azure.ai.agents.models.AgentOptimizationCandidateStatus;
import com.azure.ai.agents.models.AgentOptimizationEstimateResult;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.ai.agents.models.JobStatus;
import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentOptimizationTests extends AgentOptimizationTestBase {
    private static final String SUCCESS_AGENT_NAME = "java-optimization-sync-success";
    private static final String CANCEL_AGENT_NAME = "java-optimization-sync-cancel";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void successfulOptimizationLifecycle(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient client = getAgentsSyncClient(httpClient, serviceVersion);
        String jobId = null;
        try {
            client.createAgentVersion(SUCCESS_AGENT_NAME,
                new PromptAgentDefinition(AGENT_MODEL).setInstructions(BASELINE_INSTRUCTIONS));

            AgentOptimizationEstimateResult estimate
                = client.estimateOptimizationJob(createEstimateInputs(SUCCESS_AGENT_NAME));
            assertNotNull(estimate);

            SyncPoller<AgentOptimizationJob, ?> poller
                = client.beginCreateOptimizationJob(createOptimizationJob(SUCCESS_AGENT_NAME));
            poller.setPollInterval(getOptimizationPollInterval());
            PollResponse<AgentOptimizationJob> completed = poller.waitForCompletion(POLL_TIMEOUT);
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, completed.getStatus());
            assertNotNull(completed.getValue());
            assertEquals(JobStatus.SUCCEEDED, completed.getValue().getStatus());
            jobId = completed.getValue().getId();
            assertNotNull(jobId);
            String completedJobId = jobId;

            AgentOptimizationJob retrieved = client.getOptimizationJob(jobId);
            assertEquals(jobId, retrieved.getId());
            assertTrue(
                client.listOptimizationJobs(10, PageOrder.DESC, null, null, JobStatus.SUCCEEDED, SUCCESS_AGENT_NAME)
                    .stream()
                    .anyMatch(job -> completedJobId.equals(job.getId())));

            AgentOptimizationCandidate candidate = null;
            boolean foundCandidate = false;
            for (AgentOptimizationCandidate item : client.listOptimizationCandidates(jobId,
                Collections.singletonList(AgentOptimizationCandidateExpand.MUTATIONS), 10, PageOrder.ASC, null, null)) {
                foundCandidate = true;
                if (AgentOptimizationCandidateStatus.COMPLETED.equals(item.getStatus()) && item.getOutput() != null) {
                    candidate = item;
                    break;
                }
            }
            assertTrue(foundCandidate);

            if (candidate != null) {
                AgentOptimizationCandidate retrievedCandidate
                    = client.getOptimizationCandidate(jobId, candidate.getCandidateId());
                assertEquals(candidate.getCandidateId(), retrievedCandidate.getCandidateId());
                AgentOptimizationCandidate promoted
                    = client.promoteOptimizationCandidate(jobId, candidate.getCandidateId());
                assertNotNull(promoted.getPromotion());
            }

            client.deleteOptimizationJob(jobId);
            jobId = null;
        } finally {
            try {
                if (jobId != null) {
                    client.deleteOptimizationJob(jobId);
                }
            } finally {
                client.deleteAgent(SUCCESS_AGENT_NAME);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void cancelledOptimizationLifecycle(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient client = getAgentsSyncClient(httpClient, serviceVersion);
        String jobId = null;
        try {
            client.createAgentVersion(CANCEL_AGENT_NAME,
                new PromptAgentDefinition(AGENT_MODEL).setInstructions(BASELINE_INSTRUCTIONS));

            SyncPoller<AgentOptimizationJob, ?> poller
                = client.beginCreateOptimizationJob(createOptimizationJob(CANCEL_AGENT_NAME));
            poller.setPollInterval(getOptimizationPollInterval());
            AgentOptimizationJob job = poller.poll().getValue();
            assertNotNull(job);
            jobId = job.getId();
            assertNotNull(jobId);

            job = client.cancelOptimizationJob(jobId);
            while (!isTerminal(job.getStatus())) {
                sleep(getOptimizationPollInterval().toMillis());
                job = client.getOptimizationJob(jobId);
            }
            assertEquals(JobStatus.CANCELLED, job.getStatus());

            deleteCancelledOptimizationJob(client, jobId);
            jobId = null;
        } finally {
            try {
                if (jobId != null) {
                    client.deleteOptimizationJob(jobId);
                }
            } finally {
                client.deleteAgent(CANCEL_AGENT_NAME);
            }
        }
    }
}
