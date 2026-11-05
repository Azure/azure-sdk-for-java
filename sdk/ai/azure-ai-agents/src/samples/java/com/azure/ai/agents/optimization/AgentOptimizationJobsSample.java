// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.optimization;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.ai.agents.models.JobStatus;
import com.azure.ai.agents.models.PageOrder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Demonstrates how to list, filter, get, and optionally delete optimization jobs.
 */
public class AgentOptimizationJobsSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        AgentsClient agentsClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(configuration.get("FOUNDRY_PROJECT_ENDPOINT"))
            .buildAgentsClient();

        System.out.println("Recent optimization jobs:");
        for (AgentOptimizationJob job
            : agentsClient.listOptimizationJobs(10, PageOrder.DESC, null, null, null, null)) {
            System.out.printf("  %s: status=%s%n", job.getId(), job.getStatus());
        }

        String agentName = configuration.get("FOUNDRY_AGENT_NAME");
        if (agentName != null) {
            System.out.printf("Recent optimization jobs for agent %s:%n", agentName);
            for (AgentOptimizationJob job
                : agentsClient.listOptimizationJobs(10, PageOrder.DESC, null, null, null, agentName)) {
                System.out.printf("  %s: status=%s%n", job.getId(), job.getStatus());
            }
        }

        System.out.println("Recent succeeded optimization jobs:");
        for (AgentOptimizationJob job
            : agentsClient.listOptimizationJobs(5, PageOrder.DESC, null, null, JobStatus.SUCCEEDED, null)) {
            System.out.println("  " + job.getId());
        }

        String jobId = configuration.get("JOB_ID");
        if (jobId == null) {
            return;
        }

        AgentOptimizationJob job = agentsClient.getOptimizationJob(jobId);
        System.out.printf("Optimization job %s: status=%s%n", job.getId(), job.getStatus());
        if (job.getResult() != null && job.getResult().getCandidateSummary() != null) {
            System.out.printf("Baseline candidate: %s; best candidate: %s; completed candidates: %d%n",
                job.getResult().getCandidateSummary().getBaselineId(),
                job.getResult().getCandidateSummary().getBestId(),
                job.getResult().getCandidateSummary().getCompletedCandidateCount());
        }
        if (job.getWarnings() != null) {
            job.getWarnings().forEach(warning -> System.out.println("Warning: " + warning));
        }

        if (Boolean.parseBoolean(configuration.get("DELETE_JOB", "false"))) {
            agentsClient.deleteOptimizationJob(jobId);
            System.out.println("Deleted optimization job " + jobId + ".");
        }
    }
}
