// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.PromptAgentDefinition;
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
 * Sample demonstrating GenAI tracing exported to the console.
 *
 * <p>Unlike the original experimental design, there is no {@code enableGenAiTracing()} / {@code disableGenAiTracing()}
 * opt-in: tracing activates automatically once an OpenTelemetry implementation is configured (here registered
 * globally). To also capture message/agent content, set the
 * {@code AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED} environment variable to {@code true} (it is off by
 * default).</p>
 *
 * <p>To run this sample, add these dependencies to your project:</p>
 * <ul>
 *   <li>{@code com.azure:azure-ai-agents}</li>
 *   <li>{@code com.azure:azure-core-tracing-opentelemetry}</li>
 *   <li>{@code com.azure:azure-identity}</li>
 *   <li>{@code io.opentelemetry:opentelemetry-sdk}</li>
 *   <li>{@code io.opentelemetry:opentelemetry-exporter-logging}</li>
 * </ul>
 */
public final class TracingConsoleSample {

    /**
     * Runs the console tracing sample.
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        // 1. Configure OpenTelemetry with a console span exporter and register it globally.
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .setResource(Resource.getDefault()
                .toBuilder()
                .put(AttributeKey.stringKey("service.name"), "genai-tracing-sample")
                .build())
            .build();
        OpenTelemetrySdk openTelemetry
            = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();

        // 2. Build the clients. No tracing opt-in call is required; the configured OpenTelemetry is picked up.
        String endpoint = System.getenv("FOUNDRY_PROJECT_ENDPOINT");
        String model = System.getenv("FOUNDRY_MODEL_NAME");
        AgentsClientBuilder builder
            = new AgentsClientBuilder().endpoint(endpoint).credential(new DefaultAzureCredentialBuilder().build());
        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // 3. Create an agent - produces a "create_agent" span.
        String agentName = "TracingSampleAgent";
        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName,
            new PromptAgentDefinition(model).setInstructions("You are a helpful assistant that answers concisely.")
                .setTemperature(0.7));
        System.out.println("Agent created: " + agent.getName() + ":" + agent.getVersion());

        // 4. Generate a response - produces an "invoke_agent" span.
        AzureCreateResponseOptions azureOptions
            = new AzureCreateResponseOptions().setAgentReference(new AgentReference(agentName));
        ResponseCreateParams.Builder params = ResponseCreateParams.builder().input("What is the capital of France?");
        Response response = responsesClient.createAzureResponse(azureOptions, params);
        System.out.println("Response ID: " + response.id());

        // 5. Clean up and flush the spans to the console.
        agentsClient.deleteAgent(agentName);
        tracerProvider.close();
        System.out.println("Done. Check the console output for spans.");
    }

    private TracingConsoleSample() {
    }
}
