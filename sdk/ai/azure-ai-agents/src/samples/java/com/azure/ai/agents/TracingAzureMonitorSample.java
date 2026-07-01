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
import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;

/**
 * Sample demonstrating GenAI tracing with Azure Monitor (Application Insights) exporter.
 *
 * <p>Prerequisites:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} environment variable set</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} environment variable set</li>
 *   <li>{@code APPLICATIONINSIGHTS_CONNECTION_STRING} environment variable set</li>
 *   <li>Logged in via {@code az login}</li>
 * </ul>
 *
 * <p>To run this sample, add the following dependencies to your project:</p>
 * <ul>
 *   <li>{@code com.azure:azure-ai-agents}</li>
 *   <li>{@code com.azure:azure-monitor-opentelemetry-autoconfigure}</li>
 *   <li>{@code com.azure:azure-identity}</li>
 *   <li>{@code com.azure:azure-core-tracing-opentelemetry}</li>
 * </ul>
 */
public class TracingAzureMonitorSample {

    /**
     * Main method to run the Azure Monitor tracing sample.
     *
     * @param args unused.
     */
    @SuppressWarnings("try")
    public static void main(String[] args) {
        String endpoint = System.getenv("FOUNDRY_PROJECT_ENDPOINT");
        String model = System.getenv("FOUNDRY_MODEL_NAME");
        String appInsightsConnectionString = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");

        // 1. Set up Azure Monitor OpenTelemetry exporter FIRST (before any Azure clients)
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
        AzureMonitorAutoConfigure.customize(sdkBuilder, appInsightsConnectionString);
        sdkBuilder.setResultAsGlobal();
        OpenTelemetrySdk openTelemetry = sdkBuilder.build().getOpenTelemetrySdk();

        // 2. Enable GenAI tracing with content recording
        GenAiTracingConfiguration.enableGenAiTracing(
            new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        // 3. Start a parent span to group all operations together
        Tracer tracer = openTelemetry.getTracer("azure-monitor-tracing-sample");
        Span parentSpan = tracer.spanBuilder("travel-assistant-workflow").startSpan();
        try (Scope scope = parentSpan.makeCurrent()) {

            // 4. Create the agents client
            AgentsClient agentsClient = new AgentsClientBuilder()
                .endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAgentsClient();
            ResponsesClient responsesClient = new AgentsClientBuilder()
                .endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildResponsesClient();

            // 5. Create an agent
            String agentName = "AzureMonitorTracingSample";
            AgentVersionDetails agent = agentsClient.createAgentVersion(agentName,
                new PromptAgentDefinition(model)
                    .setInstructions("You are a travel assistant. Help users plan trips."));
            System.out.println("Agent created: " + agent.getName());

            // 6. Generate a response
            AzureCreateResponseOptions azureOptions = new AzureCreateResponseOptions()
                .setAgentReference(new AgentReference(agentName));
            ResponseCreateParams.Builder params = ResponseCreateParams.builder()
                .input("Plan a 3-day trip to Tokyo.");

            Response response = responsesClient.createAzureResponse(azureOptions, params);
            System.out.println("Response ID: " + response.id());

            // 7. Clean up
            agentsClient.deleteAgent(agentName);
        } finally {
            parentSpan.end();
        }

        GenAiTracingConfiguration.disableGenAiTracing();

        // Flush all pending spans to Azure Monitor before exiting
        openTelemetry.close();
        System.out.println("Done. Traces will appear in Application Insights within a few minutes.");
    }
}
