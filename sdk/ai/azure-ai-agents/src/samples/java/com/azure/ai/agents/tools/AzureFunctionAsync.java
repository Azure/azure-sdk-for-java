// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureFunctionBinding;
import com.azure.ai.agents.models.AzureFunctionDefinition;
import com.azure.ai.agents.models.AzureFunctionDefinitionDetails;
import com.azure.ai.agents.models.AzureFunctionStorageQueue;
import com.azure.ai.agents.models.AzureFunctionTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ToolChoiceOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates (using the async client) how to create an agent with an Azure Function tool
 * that calls an Azure Function via Storage Queue input/output bindings.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>STORAGE_INPUT_QUEUE_NAME - The Azure Storage Queue name for input.</li>
 *   <li>STORAGE_OUTPUT_QUEUE_NAME - The Azure Storage Queue name for output.</li>
 *   <li>STORAGE_QUEUE_SERVICE_ENDPOINT - The Azure Storage Queue service endpoint.</li>
 * </ul>
 */
public class AzureFunctionAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String inputQueueName = Configuration.getGlobalConfiguration().get("STORAGE_INPUT_QUEUE_NAME");
        String outputQueueName = Configuration.getGlobalConfiguration().get("STORAGE_OUTPUT_QUEUE_NAME");
        String queueServiceEndpoint = Configuration.getGlobalConfiguration().get("STORAGE_QUEUE_SERVICE_ENDPOINT");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        Map<String, Object> locationProp = new LinkedHashMap<String, Object>();
        locationProp.put("type", "string");
        locationProp.put("description", "location to determine weather for");

        Map<String, Object> props = new LinkedHashMap<String, Object>();
        props.put("location", locationProp);

        Map<String, BinaryData> parameters = new HashMap<String, BinaryData>();
        parameters.put("type", BinaryData.fromObject("object"));
        parameters.put("properties", BinaryData.fromObject(props));

        AzureFunctionTool azureFunctionTool = new AzureFunctionTool(
            new AzureFunctionDefinition(
                new AzureFunctionDefinitionDetails("queue_trigger", parameters)
                    .setDescription("Get weather for a given location"),
                new AzureFunctionBinding(
                    new AzureFunctionStorageQueue(queueServiceEndpoint, inputQueueName)),
                new AzureFunctionBinding(
                    new AzureFunctionStorageQueue(queueServiceEndpoint, outputQueueName))
            )
        );

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant.")
            .setTools(Collections.singletonList(azureFunctionTool));

        agentsAsyncClient.createAgentVersion("azure-function-agent", agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                return responsesAsyncClient.createAzureResponse(
                    new AzureCreateResponseOptions().setAgentReference(agentReference),
                    ResponseCreateParams.builder()
                        .toolChoice(ToolChoiceOptions.REQUIRED)
                        .input("What is the weather in Seattle?"));
            })
            .doOnNext(response -> {
                System.out.println("Response: " + response.output());
            })
            .then(Mono.defer(() -> {
                AgentVersionDetails agent = agentRef.get();
                if (agent != null) {
                    return agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion())
                        .doOnSuccess(v -> System.out.println("Agent deleted"));
                }
                return Mono.empty();
            }))
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .timeout(Duration.ofSeconds(300))
            .block();
    }
}
