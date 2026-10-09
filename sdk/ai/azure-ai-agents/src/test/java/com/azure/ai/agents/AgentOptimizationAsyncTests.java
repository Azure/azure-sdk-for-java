// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentOptimizationCandidate;
import com.azure.ai.agents.models.AgentOptimizationCandidateExpand;
import com.azure.ai.agents.models.AgentOptimizationCandidateStatus;
import com.azure.ai.agents.models.JobStatus;
import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentOptimizationAsyncTests extends AgentOptimizationTestBase {
    private static final String SUCCESS_AGENT_NAME = "java-optimization-async-success";
    private static final String CANCEL_AGENT_NAME = "java-optimization-async-cancel";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void successfulOptimizationLifecycle(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient client = getAgentsAsyncClient(httpClient, serviceVersion);
        AtomicReference<String> jobId = new AtomicReference<>();
        try {
            Mono<Void> lifecycle = client
                .createAgentVersion(SUCCESS_AGENT_NAME,
                    new PromptAgentDefinition(AGENT_MODEL).setInstructions(BASELINE_INSTRUCTIONS))
                .then(client.estimateOptimizationJob(createEstimateInputs(SUCCESS_AGENT_NAME)))
                .doOnNext(estimate -> assertNotNull(estimate))
                .thenMany(client.beginCreateOptimizationJob(createOptimizationJob(SUCCESS_AGENT_NAME))
                    .setPollInterval(getOptimizationPollInterval()))
                .last()
                .flatMap(completed -> {
                    assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, completed.getStatus());
                    assertNotNull(completed.getValue());
                    assertEquals(JobStatus.SUCCEEDED, completed.getValue().getStatus());
                    jobId.set(completed.getValue().getId());
                    assertNotNull(jobId.get());
                    return client.getOptimizationJob(jobId.get());
                })
                .flatMap(retrieved -> {
                    assertEquals(jobId.get(), retrieved.getId());
                    return client
                        .listOptimizationJobs(10, PageOrder.DESC, null, null, JobStatus.SUCCEEDED, SUCCESS_AGENT_NAME)
                        .filter(job -> jobId.get().equals(job.getId()))
                        .hasElements();
                })
                .flatMap(found -> {
                    assertTrue(found);
                    return client
                        .listOptimizationCandidates(jobId.get(),
                            Collections.singletonList(AgentOptimizationCandidateExpand.MUTATIONS), 10, PageOrder.ASC,
                            null, null)
                        .filter(candidate -> AgentOptimizationCandidateStatus.COMPLETED.equals(candidate.getStatus()))
                        .reduce((first, second) -> second.getOutput() == null ? first : second)
                        .switchIfEmpty(
                            Mono.error(new AssertionError("The completed job did not produce a completed candidate.")));
                })
                .flatMap(candidate -> verifyAndMaybePromoteCandidate(client, jobId.get(), candidate))
                .then(Mono.defer(() -> client.deleteOptimizationJob(jobId.get())))
                .doOnSuccess(ignored -> jobId.set(null));

            StepVerifier.create(lifecycle).verifyComplete();
        } finally {
            try {
                if (jobId.get() != null) {
                    client.deleteOptimizationJob(jobId.get()).block();
                }
            } finally {
                client.deleteAgent(SUCCESS_AGENT_NAME).block();
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void cancelledOptimizationLifecycle(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient client = getAgentsAsyncClient(httpClient, serviceVersion);
        AtomicReference<String> jobId = new AtomicReference<>();
        try {
            Mono<Void> lifecycle = client
                .createAgentVersion(CANCEL_AGENT_NAME,
                    new PromptAgentDefinition(AGENT_MODEL).setInstructions(BASELINE_INSTRUCTIONS))
                .thenMany(client.beginCreateOptimizationJob(createOptimizationJob(CANCEL_AGENT_NAME))
                    .setPollInterval(getOptimizationPollInterval()))
                .next()
                .map(response -> response.getValue())
                .doOnNext(job -> {
                    assertNotNull(job);
                    jobId.set(job.getId());
                    assertNotNull(jobId.get());
                })
                .flatMap(job -> client.cancelOptimizationJob(jobId.get()))
                .expand(job -> isTerminal(job.getStatus())
                    ? Mono.empty()
                    : Mono.delay(getOptimizationPollInterval()).then(client.getOptimizationJob(jobId.get())))
                .filter(job -> isTerminal(job.getStatus()))
                .next()
                .doOnNext(job -> assertEquals(JobStatus.CANCELLED, job.getStatus()))
                .then(Mono.defer(() -> deleteCancelledOptimizationJob(client, jobId.get())))
                .doOnSuccess(ignored -> jobId.set(null));

            StepVerifier.create(lifecycle).verifyComplete();
        } finally {
            try {
                if (jobId.get() != null) {
                    client.deleteOptimizationJob(jobId.get()).block();
                }
            } finally {
                client.deleteAgent(CANCEL_AGENT_NAME).block();
            }
        }
    }

    private static Mono<AgentOptimizationCandidate> verifyAndMaybePromoteCandidate(AgentsAsyncClient client,
        String jobId, AgentOptimizationCandidate candidate) {
        return client.getOptimizationCandidate(jobId, candidate.getCandidateId())
            .doOnNext(retrieved -> assertEquals(candidate.getCandidateId(), retrieved.getCandidateId()))
            .flatMap(retrieved -> candidate.getOutput() == null
                ? Mono.just(retrieved)
                : client.promoteOptimizationCandidate(jobId, candidate.getCandidateId())
                    .doOnNext(promoted -> assertNotNull(promoted.getPromotion())));
    }
}
