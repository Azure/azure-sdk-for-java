// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.optimization;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Demonstrates how to asynchronously create an optimization job, capture its ID, and poll it with get requests.
 */
public class AgentOptimizationAdvancedPollingAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        AgentsAsyncClient agentsClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(configuration.get("FOUNDRY_PROJECT_ENDPOINT"))
            .buildAgentsAsyncClient();
        Duration pollInterval = AgentOptimizationSampleUtils.getPollInterval(configuration);
        Duration pollTimeout = AgentOptimizationSampleUtils.getPollTimeout(configuration);

        agentsClient.beginCreateOptimizationJob(AgentOptimizationSampleUtils.createJob(configuration))
            .next()
            .map(response -> response.getValue())
            .switchIfEmpty(Mono.error(
                new IllegalStateException("The create operation did not return an optimization job.")))
            .flatMap(createdJob -> pollToCompletion(agentsClient, createdJob, pollInterval, pollTimeout))
            .flatMap(job -> {
                if (job.getWarnings() != null) {
                    job.getWarnings().forEach(warning -> System.out.println("Warning: " + warning));
                }
                AgentOptimizationSampleUtils.throwIfUnsuccessful(job);
                if (job.getResult() != null && job.getResult().getCandidateSummary() != null) {
                    System.out.printf("Completed candidates: %d; best score: %s%n",
                        job.getResult().getCandidateSummary().getCompletedCandidateCount(),
                        job.getResult().getCandidateSummary().getBestScore());
                }
                return agentsClient.listOptimizationCandidates(job.getId())
                    .doOnNext(candidate -> System.out.printf("Candidate %s: status=%s, score=%s%n",
                        candidate.getCandidateId(), candidate.getStatus(),
                        candidate.getEvaluation() == null ? null : candidate.getEvaluation().getAverageScore()))
                    .then();
            })
            .block();
    }

    private static Mono<AgentOptimizationJob> pollToCompletion(AgentsAsyncClient client,
        AgentOptimizationJob createdJob, Duration pollInterval, Duration pollTimeout) {
        if (createdJob.getId() == null) {
            return Mono.error(new IllegalStateException("The create operation did not return an optimization job ID."));
        }
        System.out.printf("Created optimization job %s with status %s.%n",
            createdJob.getId(), createdJob.getStatus());

        return Flux.interval(Duration.ZERO, pollInterval)
            .concatMap(ignored -> client.getOptimizationJob(createdJob.getId()))
            .doOnNext(job -> System.out.printf("Job %s status: %s%n", job.getId(), job.getStatus()))
            .filter(job -> AgentOptimizationSampleUtils.isTerminal(job.getStatus()))
            .next()
            .timeout(pollTimeout);
    }
}
