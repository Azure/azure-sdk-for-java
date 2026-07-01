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
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

/**
 * Sample demonstrating GenAI tracing with a console span exporter.
 *
 * <p>Prerequisites:</p>
 * <ul>
 *   <li>Azure Core Tracing OpenTelemetry plugin (com.azure:azure-core-tracing-opentelemetry)</li>
 *   <li>OpenTelemetry SDK on the classpath (io.opentelemetry:opentelemetry-sdk)</li>
 *   <li>Console exporter (io.opentelemetry:opentelemetry-exporter-logging)</li>
 *   <li>Azure AI Agents endpoint and credentials</li>
 * </ul>
 *
 * <p>To run this sample, add the following dependencies to your project:</p>
 * <ul>
 *   <li>{@code com.azure:azure-ai-agents}</li>
 *   <li>{@code com.azure:azure-core-tracing-opentelemetry}</li>
 *   <li>{@code com.azure:azure-identity}</li>
 *   <li>{@code io.opentelemetry:opentelemetry-sdk}</li>
 *   <li>{@code io.opentelemetry:opentelemetry-exporter-logging}</li>
 * </ul>
 */
public class TracingConsoleSample {

    /**
     * Main method to run the console tracing sample.
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        // 1. Set up OpenTelemetry with console exporter
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .setResource(Resource.getDefault().toBuilder()
                .put(AttributeKey.stringKey("service.name"), "genai-tracing-sample").build())
            .build();
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();

        // 2. Enable GenAI tracing (experimental opt-in required)
        GenAiTracingConfiguration.enableGenAiTracing(
            new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        // 3. Create the agents client
        String endpoint = System.getenv("FOUNDRY_PROJECT_ENDPOINT");
        String model = System.getenv("FOUNDRY_MODEL_NAME");
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
            new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant that answers questions concisely.")
                .setTemperature(0.7));

        System.out.println("Agent created: " + agent.getName() + ":" + agent.getVersion());

        // 5. Generate a response — this produces an "invoke_agent" span
        AzureCreateResponseOptions azureOptions = new AzureCreateResponseOptions()
            .setAgentReference(new AgentReference(agentName));
        ResponseCreateParams.Builder params = ResponseCreateParams.builder()
            .input("What is the capital of France?");

        Response response = responsesClient.createAzureResponse(azureOptions, params);
        System.out.println("Response ID: " + response.id());

        // 6. Clean up
        agentsClient.deleteAgent(agentName);
        System.out.println("Agent deleted.");

        // 7. Disable tracing and shut down OpenTelemetry
        GenAiTracingConfiguration.disableGenAiTracing();
        tracerProvider.close();
        System.out.println("Tracing disabled. Check console output for spans.");
    }
}
