// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.agents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample demonstrating how to create and inspect draft agent versions using the synchronous {@link AgentsClient}.
 *
 * <p>Draft agent versions are a preview feature. They are not promoted to the agent's latest released version and
 * are excluded from version listings unless drafts are explicitly included.</p>
 *
 * <p>Before running the sample, set the {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME} environment
 * variables.</p>
 */
public class AgentDraftSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClient agentsClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true)
            .buildAgentsClient();

        String agentName = "myAgentWithDraft";

        try {
            try {
                agentsClient.deleteAgent(agentName);
            } catch (ResourceNotFoundException ignored) {
                // The sample agent does not already exist.
            }

            // BEGIN:com.azure.ai.agents.agents.AgentDraftSample.createReleaseVersion
            AgentVersionDetails releaseVersion = agentsClient.createAgentVersion(agentName,
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("You are a prompt agent that gives helpful answers."))
                    .setDescription("Released agent version created by the draft sample."));
            System.out.printf("Agent created: name: %s, version: %s%n",
                releaseVersion.getName(), releaseVersion.getVersion());

            printLatestVersion(agentsClient.getAgent(agentName));
            // END:com.azure.ai.agents.agents.AgentDraftSample.createReleaseVersion

            // BEGIN:com.azure.ai.agents.agents.AgentDraftSample.createDraftVersion
            AgentVersionDetails draftVersion = agentsClient.createAgentVersion(agentName,
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("You are a prompt agent that is still being tested."))
                    .setDescription("Draft agent version created by the draft sample.")
                    .setDraft(true));
            System.out.printf("Agent draft created: name: %s, version: %s, is draft: %s%n",
                draftVersion.getName(), draftVersion.getVersion(), isDraft(draftVersion));

            AgentDetails agent = agentsClient.getAgent(agentName);
            System.out.printf("The latest released version of agent \"%s\" is still %s.%n",
                agent.getName(), agent.getVersions().getLatest().getVersion());
            // END:com.azure.ai.agents.agents.AgentDraftSample.createDraftVersion

            System.out.printf("Released versions for agent %s:%n", agentName);
            for (AgentVersionDetails version : agentsClient.listAgentVersions(agentName)) {
                System.out.printf("    %s (is draft: %s)%n", version.getVersion(), isDraft(version));
            }

            System.out.printf("All versions for agent %s:%n", agentName);
            for (AgentVersionDetails version
                : agentsClient.listAgentVersions(agentName, null, null, null, null, true)) {
                System.out.printf("    %s (is draft: %s)%n", version.getVersion(), isDraft(version));
            }
        } finally {
            agentsClient.deleteAgent(agentName);
            System.out.printf("Agent deleted (name: %s)%n", agentName);
        }
    }

    private static void printLatestVersion(AgentDetails agent) {
        System.out.printf("The latest released version of agent \"%s\" is %s.%n",
            agent.getName(), agent.getVersions().getLatest().getVersion());
    }

    private static boolean isDraft(AgentVersionDetails version) {
        return Boolean.TRUE.equals(version.isDraft());
    }
}
