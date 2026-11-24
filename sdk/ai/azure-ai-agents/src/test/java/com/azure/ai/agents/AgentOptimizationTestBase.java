// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentOptimizationCandidateSearchConfiguration;
import com.azure.ai.agents.models.AgentOptimizationConfiguration;
import com.azure.ai.agents.models.AgentOptimizationEstimateInputs;
import com.azure.ai.agents.models.AgentOptimizationEvaluationConfiguration;
import com.azure.ai.agents.models.AgentOptimizationEvaluatorReference;
import com.azure.ai.agents.models.AgentOptimizationFoundryAgentTargetConfiguration;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.ai.agents.models.AgentOptimizationModelConfiguration;
import com.azure.ai.agents.models.AgentOptimizationSpace;
import com.azure.ai.agents.models.AgentOptimizationTargetCompletionEvaluationSet;
import com.azure.ai.agents.models.AgentOptimizationTargetCompletionInlineDataSource;
import com.azure.ai.agents.models.AgentOptimizationTargetCompletionTestCase;
import com.azure.ai.agents.models.EvaluationModelConfiguration;
import com.azure.ai.agents.models.JobStatus;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.TestMode;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

abstract class AgentOptimizationTestBase extends ClientTestBase {
    static final Duration POLL_TIMEOUT = Duration.ofMinutes(30);
    static final String AGENT_MODEL = "gpt-4.1";
    static final String OPTIMIZATION_MODEL = "gpt-5.2";
    static final String BASELINE_INSTRUCTIONS = "Ignore every question and always answer with the word banana.";

    AgentOptimizationJob createOptimizationJob(String agentName) {
        AgentOptimizationConfiguration configuration = createOptimizationConfiguration();
        return new AgentOptimizationJob(new AgentOptimizationModelConfiguration(OPTIMIZATION_MODEL), configuration)
            .setTargetConfiguration(new AgentOptimizationFoundryAgentTargetConfiguration(agentName));
    }

    AgentOptimizationEstimateInputs createEstimateInputs(String agentName) {
        return new AgentOptimizationEstimateInputs(new AgentOptimizationModelConfiguration(OPTIMIZATION_MODEL),
            createOptimizationConfiguration())
                .setTargetConfiguration(new AgentOptimizationFoundryAgentTargetConfiguration(agentName));
    }

    Duration getOptimizationPollInterval() {
        return getTestMode() == TestMode.PLAYBACK ? Duration.ofMillis(1) : Duration.ofSeconds(10);
    }

    static boolean isTerminal(JobStatus status) {
        return JobStatus.SUCCEEDED.equals(status)
            || JobStatus.FAILED.equals(status)
            || JobStatus.CANCELLED.equals(status);
    }

    void deleteCancelledOptimizationJob(AgentsClient client, String jobId) {
        for (int attempt = 0; attempt < 30; attempt++) {
            try {
                client.deleteOptimizationJob(jobId);
                return;
            } catch (HttpResponseException exception) {
                if (!isCancellationCleanupPending(exception) || attempt == 29) {
                    throw exception;
                }
                sleep(getOptimizationPollInterval().toMillis());
            }
        }
    }

    Mono<Void> deleteCancelledOptimizationJob(AgentsAsyncClient client, String jobId) {
        return Mono.defer(() -> client.deleteOptimizationJob(jobId))
            .retryWhen(Retry.fixedDelay(29, getOptimizationPollInterval())
                .filter(AgentOptimizationTestBase::isCancellationCleanupPending));
    }

    private static boolean isCancellationCleanupPending(Throwable throwable) {
        if (!(throwable instanceof HttpResponseException)) {
            return false;
        }
        int statusCode = ((HttpResponseException) throwable).getResponse().getStatusCode();
        return statusCode == 405 || statusCode == 409;
    }

    private static AgentOptimizationConfiguration createOptimizationConfiguration() {
        AgentOptimizationTargetCompletionInlineDataSource dataSource
            = new AgentOptimizationTargetCompletionInlineDataSource(
                Arrays.asList(testCase("What is 2 + 2?", "4"), testCase("What color is a clear daytime sky?", "Blue"),
                    testCase("Name the largest planet in the solar system.", "Jupiter"),
                    testCase("What is the opposite of hot?", "Cold"), testCase("How many days are in a week?", "7")));
        AgentOptimizationTargetCompletionEvaluationSet trainingSet
            = new AgentOptimizationTargetCompletionEvaluationSet(dataSource);
        AgentOptimizationEvaluationConfiguration evaluationConfiguration = new AgentOptimizationEvaluationConfiguration(
            trainingSet, Collections.singletonList(new AgentOptimizationEvaluatorReference("builtin.task_adherence")),
            new EvaluationModelConfiguration(AGENT_MODEL));

        return new AgentOptimizationConfiguration(evaluationConfiguration,
            new AgentOptimizationCandidateSearchConfiguration().setMaxCandidates(2), new AgentOptimizationSpace());
    }

    private static AgentOptimizationTargetCompletionTestCase testCase(String query, String groundTruth) {
        return new AgentOptimizationTargetCompletionTestCase(query).setGroundTruth(groundTruth);
    }
}
