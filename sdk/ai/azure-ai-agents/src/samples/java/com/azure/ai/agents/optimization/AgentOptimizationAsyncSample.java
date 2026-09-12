// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.optimization;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaAgentsAsyncClient;
import com.azure.ai.agents.models.AgentOptimizationEvaluatorReference;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.ai.agents.models.AgentOptimizationJobInputs;
import com.azure.ai.agents.models.AgentOptimizationJobResult;
import com.azure.ai.agents.models.AgentOptimizationOptions;
import com.azure.ai.agents.models.AgentOptimizationReferenceDatasetInput;
import com.azure.ai.agents.models.OptimizedAgentIdentifier;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.identity.DefaultAzureCredentialBuilder;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates how to create and monitor an agent optimization job with the asynchronous beta client.
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
public class AgentOptimizationAsyncSample {
    private static final Duration POLL_TIMEOUT = Duration.ofMinutes(30);

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");

        BetaAgentsAsyncClient betaAgentsAsyncClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .beta()
            .buildBetaAgentsAsyncClient();

        AtomicReference<String> jobId = new AtomicReference<>();
        PollerFlux<AgentOptimizationJob, AgentOptimizationJobResult> poller
            = betaAgentsAsyncClient.beginCreateOptimizationJob(createOptimizationJob(configuration))
                .setPollInterval(Duration.ofSeconds(
                    Integer.parseInt(configuration.get("POLL_INTERVAL_SECONDS", "10"))));

        poller
            .take(POLL_TIMEOUT)
            .doOnNext(response -> recordProgress(response, jobId))
            .last()
            .flatMap(AgentOptimizationAsyncSample::getResult)
            .doOnNext(AgentOptimizationSample::printResult)
            .then(Mono.defer(() -> cleanupAsync(betaAgentsAsyncClient, jobId)))
            .onErrorResume(error -> Mono.defer(() -> cleanupAsync(betaAgentsAsyncClient, jobId))
                .then(Mono.<Void>error(error)))
            .block();
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

    private static void recordProgress(AsyncPollResponse<AgentOptimizationJob, AgentOptimizationJobResult> response,
        AtomicReference<String> jobId) {
        AgentOptimizationJob job = response.getValue();
        if (job != null && job.getId() != null) {
            jobId.set(job.getId());
            if (job.getProgress() != null) {
                System.out.printf("Job %s: %d candidates completed, best score %.4f%n",
                    job.getId(), job.getProgress().getCandidatesCompleted(), job.getProgress().getBestScore());
            }
        }
    }

    private static Mono<AgentOptimizationJobResult> getResult(
        AsyncPollResponse<AgentOptimizationJob, AgentOptimizationJobResult> response) {
        if (response.getStatus() != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            return Mono.error(new IllegalStateException(
                "Optimization job completed with status: " + response.getStatus()));
        }
        return response.getFinalResult()
            .switchIfEmpty(Mono.error(new IllegalStateException("The optimization job did not return a result.")));
    }

    private static Mono<Void> cleanupAsync(BetaAgentsAsyncClient betaAgentsAsyncClient,
        AtomicReference<String> jobId) {
        String id = jobId.get();
        if (id == null) {
            return Mono.empty();
        }

        return betaAgentsAsyncClient.deleteOptimizationJob(id)
            .doOnSuccess(unused -> System.out.printf("Optimization job deleted (id: %s)%n", id))
            .onErrorResume(cleanupError -> {
                System.err.printf("Failed to delete optimization job %s: %s%n", id, cleanupError.getMessage());
                return Mono.empty();
            });
    }
}
