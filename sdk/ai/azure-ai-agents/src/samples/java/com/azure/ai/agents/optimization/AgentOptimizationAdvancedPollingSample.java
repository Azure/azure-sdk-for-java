// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.optimization;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentOptimizationCandidate;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.time.Instant;

/**
 * Demonstrates how to create an optimization job, capture its ID, and poll it with get requests.
 */
public class AgentOptimizationAdvancedPollingSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        AgentsClient agentsClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(configuration.get("FOUNDRY_PROJECT_ENDPOINT"))
            .buildAgentsClient();
        Duration pollInterval = AgentOptimizationSampleUtils.getPollInterval(configuration);
        Duration pollTimeout = AgentOptimizationSampleUtils.getPollTimeout(configuration);

        SyncPoller<AgentOptimizationJob, ?> poller
            = agentsClient.beginCreateOptimizationJob(AgentOptimizationSampleUtils.createJob(configuration));
        PollResponse<AgentOptimizationJob> createResponse = poller.poll();
        AgentOptimizationJob job = createResponse.getValue();
        if (job == null || job.getId() == null) {
            throw new IllegalStateException("The create operation did not return an optimization job ID.");
        }

        String jobId = job.getId();
        System.out.printf("Created optimization job %s with status %s.%n", jobId, job.getStatus());
        Instant deadline = Instant.now().plus(pollTimeout);
        while (!AgentOptimizationSampleUtils.isTerminal(job.getStatus())) {
            if (Instant.now().isAfter(deadline)) {
                throw new IllegalStateException("Timed out waiting for optimization job " + jobId + ".");
            }
            sleep(pollInterval);
            job = agentsClient.getOptimizationJob(jobId);
            System.out.printf("Job %s status: %s%n", jobId, job.getStatus());
        }

        if (job.getWarnings() != null) {
            job.getWarnings().forEach(warning -> System.out.println("Warning: " + warning));
        }
        AgentOptimizationSampleUtils.throwIfUnsuccessful(job);

        if (job.getResult() != null && job.getResult().getCandidateSummary() != null) {
            System.out.printf("Completed candidates: %d; best score: %s%n",
                job.getResult().getCandidateSummary().getCompletedCandidateCount(),
                job.getResult().getCandidateSummary().getBestScore());
        }
        for (AgentOptimizationCandidate candidate : agentsClient.listOptimizationCandidates(jobId)) {
            System.out.printf("Candidate %s: status=%s, score=%s%n", candidate.getCandidateId(),
                candidate.getStatus(),
                candidate.getEvaluation() == null ? null : candidate.getEvaluation().getAverageScore());
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to poll the optimization job.", exception);
        }
    }
}
