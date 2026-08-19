// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FabricIqPreviewTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates how to create an agent with the FabricIQ preview tool using async clients.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>FOUNDRY_AGENT_NAME - Optional. The agent name; defaults to fabric-iq-agent.</li>
 *   <li>FABRIC_IQ_PROJECT_CONNECTION_ID - The fully qualified Fabric IQ project connection resource ID.</li>
 *   <li>FABRIC_IQ_USER_INPUT - Optional. The natural-language question to send to the agent.</li>
 * </ul>
 */
public class FabricIQAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String agentName = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_NAME", "fabric-iq-agent");
        String fabricIqConnectionId = Configuration.getGlobalConfiguration().get("FABRIC_IQ_PROJECT_CONNECTION_ID");
        String userInput = Configuration.getGlobalConfiguration().get("FABRIC_IQ_USER_INPUT",
            "Use FabricIQ to summarize the available enterprise context.");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();
        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        FabricIqPreviewTool fabricIqTool = new FabricIqPreviewTool(fabricIqConnectionId)
            .setServerLabel("fabriciq-tool")
            .setRequireApproval("never");

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("Use the available Fabric IQ tools to answer questions and perform tasks.")
            .setTools(Collections.singletonList(fabricIqTool));

        Mono<Void> workflow = agentsAsyncClient.createAgentVersion(agentName, agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                return responsesAsyncClient.createAzureResponse(
                    new AzureCreateResponseOptions().setAgentReference(agentReference),
                    ResponseCreateParams.builder()
                        .input(userInput));
            })
            .doOnNext(FabricIQAsync::printResponse)
            .then()
            .timeout(Duration.ofSeconds(300))
            .onErrorResume(error -> cleanup(agentsAsyncClient, agentRef).then(Mono.<Void>error(error)))
            .then(Mono.defer(() -> cleanup(agentsAsyncClient, agentRef)));

        workflow.block();
    }

    private static Mono<Void> cleanup(AgentsAsyncClient agentsAsyncClient,
        AtomicReference<AgentVersionDetails> agentRef) {
        AgentVersionDetails agent = agentRef.get();
        if (agent == null) {
            return Mono.empty();
        }
        return agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion())
            .doOnSuccess(v -> System.out.println("Agent deleted"));
    }

    private static void printResponse(Response response) {
        for (ResponseOutputItem outputItem : response.output()) {
            if (outputItem.message().isPresent()) {
                ResponseOutputMessage message = outputItem.message().get();
                message.content().forEach(content -> content.outputText().ifPresent(outputText -> {
                    System.out.println("Agent response: " + outputText.text());
                    outputText.annotations()
                        .forEach(annotation -> System.out.println("Annotation: " + annotation));
                }));
            }
        }
    }
}
