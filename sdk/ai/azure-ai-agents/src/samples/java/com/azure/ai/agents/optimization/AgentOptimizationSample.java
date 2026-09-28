// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.optimization;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaAgentsClient;
import com.azure.ai.agents.models.AgentOptimizationCandidate;
import com.azure.ai.agents.models.AgentOptimizationEvaluatorReference;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.ai.agents.models.AgentOptimizationJobInputs;
import com.azure.ai.agents.models.AgentOptimizationJobResult;
import com.azure.ai.agents.models.AgentOptimizationOptions;
import com.azure.ai.agents.models.AgentOptimizationReferenceDatasetInput;
import com.azure.ai.agents.models.OptimizedAgentIdentifier;
import com.azure.core.util.Configuration;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This sample demonstrates how to create and monitor an agent optimization job with the synchronous beta client.
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

        BetaAgentsClient betaAgentsClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .beta()
            .buildBetaAgentsClient();

        AgentOptimizationJob job = createOptimizationJob(configuration);
        SyncPoller<AgentOptimizationJob, AgentOptimizationJobResult> poller
            = betaAgentsClient.beginCreateOptimizationJob(job);
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
            printResult(poller.getFinalResult());
        } finally {
            deleteJob(betaAgentsClient, jobId);
        }
    }

    private static AgentOptimizationJob createOptimizationJob(Configuration configuration) {
        String evaluatorVersion = configuration.get("EVALUATOR_VERSION");
        AgentOptimizationEvaluatorReference evaluator = new AgentOptimizationEvaluatorReference(
            configuration.get("EVALUATOR_NAME", "task_adherence"));
        if (evaluatorVersion != null) {
            evaluator.setVersion(evaluatorVersion);
        }

        AgentOptimizationReferenceDatasetInput trainDataset = new AgentOptimizationReferenceDatasetInput(
            configuration.get("DATASET_NAME"));
        trainDataset.setVersion(configuration.get("DATASET_VERSION", "1"));

        AgentOptimizationOptions options = new AgentOptimizationOptions()
            .setMaxCandidates(Integer.parseInt(configuration.get("MAX_CANDIDATES", "2")))
            .setEvalModel(configuration.get("EVAL_MODEL", "gpt-4.1-mini"))
            .setOptimizationModel(configuration.get("OPTIMIZATION_MODEL", "gpt-5.1"));

        String systemPrompt = configuration.get("FOUNDRY_AGENT_SYSTEM_PROMPT");
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, BinaryData> optimizationConfig = new HashMap<>();
            optimizationConfig.put("system_prompt", BinaryData.fromObject(systemPrompt));
            options.setOptimizationConfig(optimizationConfig);
        }

        AgentOptimizationJobInputs inputs = new AgentOptimizationJobInputs(
            new OptimizedAgentIdentifier(configuration.get("FOUNDRY_AGENT_NAME")),
            trainDataset,
            Collections.singletonList(evaluator));
        inputs.setOptions(options);
        return new AgentOptimizationJob().setInputs(inputs);
    }

    static void printResult(AgentOptimizationJobResult result) {
        if (result == null) {
            System.out.println("The optimization job did not return a result.");
            return;
        }

        System.out.printf("Baseline candidate: %s%n", result.getBaseline());
        System.out.printf("Best candidate: %s%n", result.getBest());
        List<AgentOptimizationCandidate> candidates = result.getCandidates();
        if (candidates != null) {
            for (AgentOptimizationCandidate candidate : candidates) {
                System.out.printf("  %s (id: %s, score: %.4f, tokens: %.0f)%n",
                    candidate.getName(), candidate.getCandidateId(), candidate.getAverageScore(),
                    candidate.getAverageTokens());
            }
        }
    }

    private static void deleteJob(BetaAgentsClient betaAgentsClient, String jobId) {
        if (jobId == null) {
            return;
        }

        try {
            betaAgentsClient.deleteOptimizationJob(jobId);
            System.out.printf("Optimization job deleted (id: %s)%n", jobId);
        } catch (RuntimeException cleanupError) {
            System.err.printf("Failed to delete optimization job %s: %s%n", jobId, cleanupError.getMessage());
        }
    }
}
