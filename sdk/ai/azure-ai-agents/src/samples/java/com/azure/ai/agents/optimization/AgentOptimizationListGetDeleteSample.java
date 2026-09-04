// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.optimization;

import com.azure.ai.agents.BetaAgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.ai.agents.models.AgentOptimizationJob;
import com.azure.ai.agents.models.AgentOptimizationJobListItem;
import com.azure.ai.agents.models.ApiError;
import com.azure.ai.agents.models.PageOrder;

/**
 * Demonstrates listing, retrieving, and optionally deleting agent optimization jobs.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code OPTIMIZATION_JOB_ID} - Optional. The optimization job to retrieve; defaults to the first listed job.</li>
 *   <li>{@code DELETE_OPTIMIZATION_JOB} - Optional. Whether to delete the retrieved optimization job. Defaults to {@code false}.</li>
 * </ul>
 */
public class AgentOptimizationListGetDeleteSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String configuredJobId = configuration.get("OPTIMIZATION_JOB_ID");
        boolean deleteJob = Boolean.parseBoolean(configuration.get("DELETE_OPTIMIZATION_JOB", "false"));

        BetaAgentsClient client = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .beta()
            .buildBetaAgentsClient();
        String firstJobId = null;

        for (AgentOptimizationJobListItem item : client.listOptimizationJobs(10, PageOrder.DESC,
            null, null, null, null)) {
            System.out.printf("Job %s: %s%n", item.getId(), item.getStatus());
            printError(item.getError(), "  ");
            if (firstJobId == null) {
                firstJobId = item.getId();
            }
        }

        String jobId = configuredJobId == null ? firstJobId : configuredJobId;
        if (jobId == null) {
            System.out.println("No optimization jobs were found.");
            return;
        }

        AgentOptimizationJob job = client.getOptimizationJob(jobId);
        System.out.printf("Retrieved job %s: %s%n", job.getId(), job.getStatus());
        printError(job.getError(), "  ");
        if (job.getWarnings() != null) {
            job.getWarnings().forEach(warning -> System.out.println("  Warning: " + warning));
        }
        if (deleteJob) {
            client.deleteOptimizationJob(jobId);
            System.out.println("Deleted job: " + jobId);
        }
    }

    private static void printError(ApiError error, String indent) {
        if (error == null) {
            return;
        }

        System.out.printf("%sError: [%s] %s%n", indent, error.getCode(), error.getMessage());
        if (error.getType() != null) {
            System.out.println(indent + "Type: " + error.getType());
        }
        if (error.getParam() != null) {
            System.out.println(indent + "Parameter: " + error.getParam());
        }
        if (error.getDetails() != null) {
            error.getDetails().forEach(detail -> printError(detail, indent + "  "));
        }
        if (error.getAdditionalInfo() != null) {
            System.out.println(indent + "Additional info: " + error.getAdditionalInfo());
        }
        if (error.getDebugInfo() != null) {
            System.out.println(indent + "Debug info: " + error.getDebugInfo());
        }
    }
}
