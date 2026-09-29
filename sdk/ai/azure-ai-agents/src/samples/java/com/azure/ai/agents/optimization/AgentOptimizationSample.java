// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.optimization;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.models.AgentOptimizationBaselineAgentConfiguration;
import com.azure.ai.agents.models.AgentOptimizationCandidate;
import com.azure.ai.agents.models.AgentOptimizationCandidateSearchConfiguration;
import com.azure.ai.agents.models.AgentOptimizationConfiguration;
import com.azure.ai.agents.models.AgentOptimizationEvaluationConfiguration;
import com.azure.ai.agents.models.AgentOptimizationEvaluatorReference;
import com.azure.ai.agents.models.AgentOptimizationFoundryAgentTargetConfiguration;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.ai.agents.models.AgentOptimizationJobResult;
import com.azure.ai.agents.models.AgentOptimizationModelConfiguration;
import com.azure.ai.agents.models.AgentOptimizationResultCandidateSummary;
import com.azure.ai.agents.models.AgentOptimizationSpace;
import com.azure.ai.agents.models.AgentOptimizationTargetCompletionDatasetReferenceDataSource;
import com.azure.ai.agents.models.AgentOptimizationTargetCompletionEvaluationSet;
import com.azure.ai.agents.models.EvaluationModelConfiguration;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.Collections;

/**
 * This sample demonstrates how to create and monitor an agent optimization job with the synchronous client.
 *
 * <p>Agent optimization is currently a preview feature. Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code FOUNDRY_AGENT_NAME} - the registered agent to optimize.</li>
 *   <li>{@code DATASET_NAME} - the registered training dataset.</li>
 *   <li>{@code DATASET_VERSION} - the training dataset version (defaults to {@code 1}).</li>
 *   <li>{@code EVALUATOR_NAME} - the registered evaluator (defaults to {@code task_adherence}).</li>
 *   <li>{@code EVAL_MODEL} - the model deployment used to score responses.</li>
 *   <li>{@code OPTIMIZATION_MODEL} - the model deployment used to generate candidates.</li>
 * </ul>
 *
 * <p>For a hosted agent, also set {@code FOUNDRY_AGENT_SYSTEM_PROMPT} to include the baseline system prompt in the
 * optimization request. The prompt is optional for agents whose baseline configuration is resolved by the service.</p>
 */
public class AgentOptimizationSample {
    private static final Duration POLL_TIMEOUT = Duration.ofMinutes(30);

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");

        AgentsClient agentsClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildAgentsClient();

        AgentOptimizationJob job = createOptimizationJob(configuration);
        SyncPoller<AgentOptimizationJob, AgentOptimizationJobResult> poller
            = agentsClient.beginCreateOptimizationJob(job);
        poller.setPollInterval(Duration.ofSeconds(
            Integer.parseInt(configuration.get("POLL_INTERVAL_SECONDS", "10"))));

        String jobId = null;
        try {
            PollResponse<AgentOptimizationJob> initialResponse = poller.poll();
            AgentOptimizationJob createdJob = initialResponse.getValue();
            if (createdJob == null || createdJob.getId() == null) {
                throw new IllegalStateException("The optimization service did not return a job ID.");
            }

            jobId = createdJob.getId();
            System.out.printf("Optimization job started (id: %s, status: %s)%n",
                jobId, initialResponse.getStatus());

            poller.waitForCompletion(POLL_TIMEOUT);
            printResult(poller.getFinalResult(), agentsClient, jobId);
        } finally {
            deleteJob(agentsClient, jobId);
        }
    }

    private static AgentOptimizationJob createOptimizationJob(Configuration configuration) {
        String evaluatorVersion = configuration.get("EVALUATOR_VERSION");
        AgentOptimizationEvaluatorReference evaluator = new AgentOptimizationEvaluatorReference(
            configuration.get("EVALUATOR_NAME", "task_adherence"));
        if (evaluatorVersion != null) {
            evaluator.setVersion(evaluatorVersion);
        }

        AgentOptimizationTargetCompletionEvaluationSet trainingSet
            = new AgentOptimizationTargetCompletionEvaluationSet(
                new AgentOptimizationTargetCompletionDatasetReferenceDataSource(
                    configuration.get("DATASET_NAME"), configuration.get("DATASET_VERSION", "1")));
        AgentOptimizationEvaluationConfiguration evaluationConfiguration
            = new AgentOptimizationEvaluationConfiguration(trainingSet, Collections.singletonList(evaluator),
                new EvaluationModelConfiguration(configuration.get("EVAL_MODEL", "gpt-4.1-mini")));
        AgentOptimizationConfiguration optimizationConfiguration = new AgentOptimizationConfiguration(
            evaluationConfiguration,
            new AgentOptimizationCandidateSearchConfiguration()
                .setMaxCandidates(Integer.parseInt(configuration.get("MAX_CANDIDATES", "2"))),
            new AgentOptimizationSpace());

        String systemPrompt = configuration.get("FOUNDRY_AGENT_SYSTEM_PROMPT");
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            optimizationConfiguration.setBaselineAgentConfiguration(
                new AgentOptimizationBaselineAgentConfiguration().setSystemPrompt(systemPrompt));
        }

        return new AgentOptimizationJob(
            new AgentOptimizationModelConfiguration(configuration.get("OPTIMIZATION_MODEL", "gpt-5.1")),
            optimizationConfiguration)
            .setTargetConfiguration(
                new AgentOptimizationFoundryAgentTargetConfiguration(configuration.get("FOUNDRY_AGENT_NAME")));
    }

    static void printResult(AgentOptimizationJobResult result, AgentsClient agentsClient, String jobId) {
        if (result == null) {
            System.out.println("The optimization job did not return a result.");
            return;
        }

        AgentOptimizationResultCandidateSummary summary = result.getCandidateSummary();
        if (summary != null) {
            System.out.printf("Baseline candidate: %s (score: %s)%n",
                summary.getBaselineId(), summary.getBaselineScore());
            System.out.printf("Best candidate: %s (score: %s)%n", summary.getBestId(), summary.getBestScore());
        }
        for (AgentOptimizationCandidate candidate : agentsClient.listOptimizationCandidates(jobId)) {
            System.out.printf("  %s (id: %s, score: %s, tokens: %s)%n",
                candidate.getName(), candidate.getCandidateId(),
                candidate.getEvaluation() == null ? null : candidate.getEvaluation().getAverageScore(),
                candidate.getEvaluation() == null ? null : candidate.getEvaluation().getAverageTokens());
        }
    }

    private static void deleteJob(AgentsClient agentsClient, String jobId) {
        if (jobId == null) {
            return;
        }

        try {
            agentsClient.deleteOptimizationJob(jobId);
            System.out.printf("Optimization job deleted (id: %s)%n", jobId);
        } catch (RuntimeException cleanupError) {
            System.err.printf("Failed to delete optimization job %s: %s%n", jobId, cleanupError.getMessage());
        }
    }
}
