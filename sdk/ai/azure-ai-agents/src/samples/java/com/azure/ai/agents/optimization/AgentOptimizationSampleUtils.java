// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.optimization;

import com.azure.ai.agents.models.AgentOptimizationBaselineAgentConfiguration;
import com.azure.ai.agents.models.AgentOptimizationCandidateSearchConfiguration;
import com.azure.ai.agents.models.AgentOptimizationConfiguration;
import com.azure.ai.agents.models.AgentOptimizationEvaluationConfiguration;
import com.azure.ai.agents.models.AgentOptimizationEvaluatorReference;
import com.azure.ai.agents.models.AgentOptimizationFoundryAgentTargetConfiguration;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.ai.agents.models.AgentOptimizationModelConfiguration;
import com.azure.ai.agents.models.AgentOptimizationSpace;
import com.azure.ai.agents.models.AgentOptimizationTargetCompletionDatasetReferenceDataSource;
import com.azure.ai.agents.models.AgentOptimizationTargetCompletionEvaluationSet;
import com.azure.ai.agents.models.EvaluationModelConfiguration;
import com.azure.ai.agents.models.JobStatus;
import com.azure.core.util.Configuration;

import java.time.Duration;
import java.util.Collections;

final class AgentOptimizationSampleUtils {
    private AgentOptimizationSampleUtils() {
    }

    static AgentOptimizationJob createJob(Configuration configuration) {
        String evaluatorVersion = configuration.get("EVALUATOR_VERSION");
        AgentOptimizationEvaluatorReference evaluator
            = new AgentOptimizationEvaluatorReference(configuration.get("EVALUATOR_NAME"));
        if (evaluatorVersion != null) {
            evaluator.setVersion(evaluatorVersion);
        }

        AgentOptimizationTargetCompletionEvaluationSet trainingSet
            = new AgentOptimizationTargetCompletionEvaluationSet(
                new AgentOptimizationTargetCompletionDatasetReferenceDataSource(
                    configuration.get("DATASET_NAME"), configuration.get("DATASET_VERSION", "1")));
        AgentOptimizationEvaluationConfiguration evaluationConfiguration
            = new AgentOptimizationEvaluationConfiguration(trainingSet, Collections.singletonList(evaluator),
                new EvaluationModelConfiguration(configuration.get("EVAL_MODEL")));
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

    static Duration getPollInterval(Configuration configuration) {
        return Duration.ofSeconds(Integer.parseInt(configuration.get("POLL_INTERVAL_SECONDS", "10")));
    }

    static Duration getPollTimeout(Configuration configuration) {
        return Duration.ofMinutes(Integer.parseInt(configuration.get("POLL_TIMEOUT_MINUTES", "30")));
    }

    static boolean isTerminal(JobStatus status) {
        return JobStatus.SUCCEEDED.equals(status)
            || JobStatus.FAILED.equals(status)
            || JobStatus.CANCELLED.equals(status);
    }

    static void throwIfUnsuccessful(AgentOptimizationJob job) {
        if (JobStatus.FAILED.equals(job.getStatus())) {
            String message = job.getError() == null ? "No error details were returned." : job.getError().getMessage();
            throw new IllegalStateException("Optimization job " + job.getId() + " failed: " + message);
        }
        if (JobStatus.CANCELLED.equals(job.getStatus())) {
            throw new IllegalStateException("Optimization job " + job.getId() + " was cancelled.");
        }
        if (!JobStatus.SUCCEEDED.equals(job.getStatus())) {
            throw new IllegalStateException(
                "Optimization job " + job.getId() + " completed with unexpected status " + job.getStatus() + ".");
        }
    }
}
