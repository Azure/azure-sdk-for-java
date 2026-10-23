// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.agents;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sample demonstrating how to create and inspect draft agent versions using the asynchronous
 * {@link AgentsAsyncClient}.
 *
 * <p>Draft agent versions are a preview feature. They are not promoted to the agent's latest released version and
 * are excluded from version listings unless drafts are explicitly included.</p>
 *
 * <p>Before running the sample, set the {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME} environment
 * variables.</p>
 */
public class AgentDraftAsyncSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsAsyncClient agentsAsyncClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true)
            .buildAgentsAsyncClient();

        String agentName = "java-draft-agent-" + UUID.randomUUID();
        AtomicBoolean agentCreated = new AtomicBoolean();

        Mono<Void> workflow = agentsAsyncClient.createAgentVersion(agentName,
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("You are a prompt agent that gives helpful answers."))
                    .setDescription("Released agent version created by the draft sample."))
            .doOnNext(releaseVersion -> {
                agentCreated.set(true);
                System.out.printf("Agent created: name: %s, version: %s%n",
                    releaseVersion.getName(), releaseVersion.getVersion());
            })
            .then(agentsAsyncClient.getAgent(agentName))
            .doOnNext(AgentDraftAsyncSample::printLatestVersion)
            .then(agentsAsyncClient.createAgentVersion(agentName,
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("You are a prompt agent that is still being tested."))
                    .setDescription("Draft agent version created by the draft sample.")
                    .setDraft(true)))
            .doOnNext(draftVersion -> {
                System.out.printf("Agent draft created: name: %s, version: %s, is draft: %s%n",
                    draftVersion.getName(), draftVersion.getVersion(), isDraft(draftVersion));
            })
            .then(agentsAsyncClient.getAgent(agentName))
            .doOnNext(agent -> System.out.printf(
                "The latest released version of agent \"%s\" is still %s.%n",
                agent.getName(), agent.getVersions().getLatest().getVersion()))
            .then(agentsAsyncClient.listAgentVersions(agentName)
                .doOnNext(version -> printVersion("Released", version))
                .then())
            .then(agentsAsyncClient.listAgentVersions(agentName, null, null, null, null, true)
                .doOnNext(version -> printVersion("All", version))
                .then());

        workflow
            .onErrorResume(error -> cleanup(agentsAsyncClient, agentName, agentCreated).then(Mono.error(error)))
            .then(Mono.defer(() -> cleanup(agentsAsyncClient, agentName, agentCreated)))
            .block();
    }

    private static Mono<Void> cleanup(AgentsAsyncClient agentsAsyncClient, String agentName,
        AtomicBoolean agentCreated) {
        if (!agentCreated.get()) {
            return Mono.empty();
        }
        return agentsAsyncClient.deleteAgent(agentName)
            .doOnSuccess(unused -> System.out.printf("Agent deleted (name: %s)%n", agentName));
    }

    private static void printLatestVersion(AgentDetails agent) {
        System.out.printf("The latest released version of agent \"%s\" is %s.%n",
            agent.getName(), agent.getVersions().getLatest().getVersion());
    }

    private static void printVersion(String collection, AgentVersionDetails version) {
        System.out.printf("%s version: %s (is draft: %s)%n", collection, version.getVersion(), isDraft(version));
    }

    private static boolean isDraft(AgentVersionDetails version) {
        return Boolean.TRUE.equals(version.isDraft());
    }
}
