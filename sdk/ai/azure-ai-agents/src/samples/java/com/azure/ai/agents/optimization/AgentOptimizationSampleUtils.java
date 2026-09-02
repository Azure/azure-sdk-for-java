// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.optimization;

import com.azure.ai.agents.models.AgentOptimizationEvaluatorRef;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.ai.agents.models.AgentOptimizationJobInputs;
import com.azure.ai.agents.models.AgentOptimizationOptions;
import com.azure.ai.agents.models.AgentOptimizationReferenceDatasetInput;
import com.azure.ai.agents.models.JobStatus;
import com.azure.ai.agents.models.OptimizedAgentIdentifier;

import java.util.Collections;

final class AgentOptimizationSampleUtils {
    private AgentOptimizationSampleUtils() {
    }

    static AgentOptimizationJob createJob(String agentName, String datasetName, String datasetVersion,
        String evaluatorName, int maxCandidates, String evalModel, String optimizationModel) {
        AgentOptimizationEvaluatorRef evaluator = new AgentOptimizationEvaluatorRef(evaluatorName);
        AgentOptimizationReferenceDatasetInput dataset = new AgentOptimizationReferenceDatasetInput(datasetName)
            .setVersion(datasetVersion);
        AgentOptimizationOptions options = new AgentOptimizationOptions()
            .setMaxCandidates(maxCandidates)
            .setEvalModel(evalModel)
            .setOptimizationModel(optimizationModel);
        AgentOptimizationJobInputs inputs = new AgentOptimizationJobInputs(
            new OptimizedAgentIdentifier(agentName), dataset, Collections.singletonList(evaluator))
                .setOptions(options);
        return new AgentOptimizationJob().setInputs(inputs);
    }

    static boolean isTerminal(JobStatus status) {
        return JobStatus.SUCCEEDED.equals(status)
            || JobStatus.FAILED.equals(status)
            || JobStatus.CANCELLED.equals(status);
    }
}
