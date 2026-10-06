// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.SampleUtils;
import com.azure.ai.agents.models.OpenApiFunctionDefinition;
import com.azure.ai.agents.models.OpenApiProjectConnectionAuthDetails;
import com.azure.ai.agents.models.OpenApiProjectConnectionSecurityScheme;
import com.azure.ai.agents.models.OpenApiTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.responses.ResponseCreateParams;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * This sample demonstrates (using the async client) how to create an agent with an OpenAPI tool
 * using project connection authentication.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>OPENAPI_PROJECT_CONNECTION_ID - A Custom Keys connection containing your TripAdvisor API key
 *       under the key name {@code key}.</li>
 * </ul>
 *
 * <p>This sample uses the TripAdvisor location-search specification bundled at
 * {@code src/samples/resources/assets/tripadvisor_openapi.json}. It declares API key authentication
 * in the {@code key} query parameter, matching the project connection.</p>
 */
public class OpenApiWithConnectionAsync {
    public static void main(String[] args) throws Exception {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String agentName = "openapi-connection-agent";
        String connectionId = Configuration.getGlobalConfiguration().get("OPENAPI_PROJECT_CONNECTION_ID");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        OpenAIClientAsync openAIAsyncClient = builder.buildAgentScopedOpenAIAsyncClient(agentName);

        Map<String, BinaryData> spec = OpenApiFunctionDefinition.readSpecFromFile(
            SampleUtils.getResourcePath("assets/tripadvisor_openapi.json"));

        OpenApiTool openApiTool = new OpenApiTool(
            new OpenApiFunctionDefinition(
                "tripadvisor",
                spec,
                new OpenApiProjectConnectionAuthDetails(
                    new OpenApiProjectConnectionSecurityScheme(connectionId)))
                .setDescription("TripAdvisor API to get travel information."));

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant.")
            .setTools(Collections.singletonList(openApiTool));

        Mono.usingWhen(
            agentsAsyncClient.createAgentVersion(agentName, agentDefinition),
            agent -> {
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                return Mono.fromFuture(() -> openAIAsyncClient.responses().create(
                    ResponseCreateParams.builder()
                        .input("Recommend me 5 top hotels in the United States")
                        .build()))
                    .timeout(Duration.ofSeconds(300))
                    .doOnNext(response -> System.out.println("Response: " + response.output()));
            },
            agent -> agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion())
                .doOnSuccess(unused -> System.out.println("Agent deleted")))
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .block();
    }
}
