// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.telemetry.GenAiTracingConfiguration;
import com.azure.ai.agents.telemetry.GenAiTracingOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

/**
 * Sample demonstrating GenAI tracing with a console span exporter.
 *
 * <p>Prerequisites:</p>
 * <ul>
 *   <li>OpenTelemetry SDK on the classpath (io.opentelemetry:opentelemetry-sdk)</li>
 *   <li>Console exporter (io.opentelemetry:opentelemetry-exporter-logging)</li>
 *   <li>Azure AI Agents endpoint and credentials</li>
 * </ul>
 *
 * <p>To run this sample, add the following dependencies to your project:</p>
 * <pre>{@code
 * <dependency>
 *   <groupId>io.opentelemetry</groupId>
 *   <artifactId>opentelemetry-sdk</artifactId>
 *   <version>1.40.0</version>
 * </dependency>
 * <dependency>
 *   <groupId>io.opentelemetry</groupId>
 *   <artifactId>opentelemetry-exporter-logging</artifactId>
 *   <version>1.40.0</version>
 * </dependency>
 * }</pre>
 */
public class TracingConsoleSample {

    /**
     * Main method to run the console tracing sample.
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        // 1. Set up OpenTelemetry with console exporter
        // (In a real app, configure the TracerProvider before enabling GenAI tracing)
        //
        // SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
        //     .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
        //     .setResource(Resource.getDefault().toBuilder()
        //         .put(ResourceAttributes.SERVICE_NAME, "genai-tracing-sample").build())
        //     .build();
        // OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();

        // 2. Enable GenAI tracing (experimental)
        GenAiTracingConfiguration.enableGenAiTracing(
            new GenAiTracingOptions().setContentRecording(false));

        // 3. Create the agents client
        String endpoint = System.getenv("AZURE_AI_AGENTS_ENDPOINT");
        AgentsClient agentsClient = new AgentsClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAgentsClient();
        ResponsesClient responsesClient = new AgentsClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildResponsesClient();

        // 4. Create an agent — this produces a "create_agent" span
        String agentName = "TracingSampleAgent";
        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName,
            new PromptAgentDefinition("gpt-4.1")
                .setInstructions("You are a helpful assistant that answers questions concisely.")
                .setTemperature(0.7));

        System.out.println("Agent created: " + agent.getName() + ":" + agent.getVersion());

        // 5. Generate a response — this produces an "invoke_agent" span
        AzureCreateResponseOptions azureOptions = new AzureCreateResponseOptions()
            .setAgentReference(new AgentReference(agentName));
        ResponseCreateParams.Builder params = ResponseCreateParams.builder()
            .model(agentName)
            .input("What is the capital of France?");

        Response response = responsesClient.createAzureResponse(azureOptions, params);
        System.out.println("Response ID: " + response.id());

        // 6. Clean up
        agentsClient.deleteAgent(agentName);
        System.out.println("Agent deleted.");

        // 7. Disable tracing
        GenAiTracingConfiguration.disableGenAiTracing();
        System.out.println("Tracing disabled. Check console output for spans.");
    }
}
