// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.PromptAgentDefinition;
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
 * Sample demonstrating GenAI tracing exported to Azure Monitor (Application Insights).
 *
 * <p>As with the console sample, there is no {@code enableGenAiTracing()} opt-in: tracing activates automatically
 * once OpenTelemetry is configured (here via Azure Monitor autoconfigure, registered globally). Set the
 * {@code AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED} environment variable to {@code true} to also capture
 * message/agent content.</p>
 *
 * <p>Prerequisites:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME} environment variables set</li>
 *   <li>{@code APPLICATIONINSIGHTS_CONNECTION_STRING} environment variable set</li>
 *   <li>Logged in via {@code az login}</li>
 * </ul>
 *
 * <p>To run this sample, add these dependencies to your project:</p>
 * <ul>
 *   <li>{@code com.azure:azure-ai-agents}</li>
 *   <li>{@code com.azure:azure-monitor-opentelemetry-autoconfigure}</li>
 *   <li>{@code com.azure:azure-identity}</li>
 *   <li>{@code com.azure:azure-core-tracing-opentelemetry}</li>
 * </ul>
 */
public final class TracingAzureMonitorSample {

    /**
     * Runs the Azure Monitor tracing sample.
     *
     * @param args unused.
     */
    @SuppressWarnings("try")
    public static void main(String[] args) {
        String endpoint = System.getenv("FOUNDRY_PROJECT_ENDPOINT");
        String model = System.getenv("FOUNDRY_MODEL_NAME");
        String appInsightsConnectionString = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");

        // 1. Configure the Azure Monitor OpenTelemetry exporter and register it globally.
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
        AzureMonitorAutoConfigure.customize(sdkBuilder, appInsightsConnectionString);
        sdkBuilder.setResultAsGlobal();
        OpenTelemetrySdk openTelemetry = sdkBuilder.build().getOpenTelemetrySdk();

        // 2. Optionally group the operations under a parent span.
        Tracer tracer = openTelemetry.getTracer("azure-monitor-tracing-sample");
        Span parentSpan = tracer.spanBuilder("travel-assistant-workflow").startSpan();
        try (Scope scope = parentSpan.makeCurrent()) {
            // 3. Build the clients. The configured OpenTelemetry is picked up automatically.
            AgentsClientBuilder builder
                = new AgentsClientBuilder().endpoint(endpoint).credential(new DefaultAzureCredentialBuilder().build());
            AgentsClient agentsClient = builder.buildAgentsClient();
            ResponsesClient responsesClient = builder.buildResponsesClient();

            // 4. Create an agent - produces a "create_agent" span nested under the parent span.
            String agentName = "AzureMonitorTracingSample";
            AgentVersionDetails agent = agentsClient.createAgentVersion(agentName,
                new PromptAgentDefinition(model).setInstructions("You are a travel assistant. Help users plan trips."));
            System.out.println("Agent created: " + agent.getName());

            // 5. Generate a response - produces an "invoke_agent" span.
            AzureCreateResponseOptions azureOptions
                = new AzureCreateResponseOptions().setAgentReference(new AgentReference(agentName));
            ResponseCreateParams.Builder params = ResponseCreateParams.builder().input("Plan a 3-day trip to Tokyo.");
            Response response = responsesClient.createAzureResponse(azureOptions, params);
            System.out.println("Response ID: " + response.id());

            agentsClient.deleteAgent(agentName);
        } finally {
            parentSpan.end();
        }

        // 6. Flush all pending spans to Azure Monitor before exiting.
        openTelemetry.close();
        System.out.println("Done. Traces will appear in Application Insights within a few minutes.");
    }

    private TracingAzureMonitorSample() {
    }
}
