// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.optimization;

import com.azure.ai.agents.BetaAgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

/**
 * Demonstrates cancelling an in-progress agent optimization job.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_AGENT_NAME} - The registered agent to optimize.</li>
 *   <li>{@code DATASET_NAME} - The registered training dataset.</li>
 *   <li>{@code DATASET_VERSION} - Optional. The training dataset version. Defaults to {@code 1}.</li>
 *   <li>{@code EVALUATOR_NAME} - Optional. The registered evaluator name. Defaults to {@code task_adherence}.</li>
 *   <li>{@code MAX_CANDIDATES} - Optional. The maximum number of optimization candidates. Defaults to {@code 2}.</li>
 *   <li>{@code EVAL_MODEL} - Optional. The model deployment used to evaluate candidates. Defaults to {@code gpt-4.1-mini}.</li>
 *   <li>{@code OPTIMIZATION_MODEL} - Optional. The model deployment used to generate candidates. Defaults to {@code gpt-5.1}.</li>
 * </ul>
 */
public class AgentOptimizationCancelSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("FOUNDRY_AGENT_NAME");
        String datasetName = configuration.get("DATASET_NAME");
        String datasetVersion = configuration.get("DATASET_VERSION", "1");
        String evaluatorName = configuration.get("EVALUATOR_NAME", "task_adherence");
        int maxCandidates = Integer.parseInt(configuration.get("MAX_CANDIDATES", "2"));
        String evalModel = configuration.get("EVAL_MODEL", "gpt-4.1-mini");
        String optimizationModel = configuration.get("OPTIMIZATION_MODEL", "gpt-5.1");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        BetaAgentsClient client = builder.beta().buildBetaAgentsClient();
        SyncPoller<AgentOptimizationJob, ?> poller = client.beginCreateOptimizationJob(
            AgentOptimizationSampleUtils.createJob(agentName, datasetName, datasetVersion, evaluatorName,
                maxCandidates, evalModel, optimizationModel));
        PollResponse<AgentOptimizationJob> initial = poller.poll();
        AgentOptimizationJob job = initial.getValue();
        if (job == null || job.getId() == null) {
            throw new IllegalStateException("The service did not return an optimization job ID.");
        }

        try {
            AgentOptimizationJob cancelled = client.cancelOptimizationJob(job.getId());
            System.out.printf("Cancellation requested for %s; status is %s%n",
                cancelled.getId(), cancelled.getStatus());
        } finally {
            client.deleteOptimizationJob(job.getId());
        }
    }
}
