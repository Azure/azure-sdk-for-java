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
 * Sample demonstrating GenAI tracing with Azure Monitor (Application Insights) exporter.
 *
 * <p>Prerequisites:</p>
 * <ul>
 *   <li>Azure Monitor OpenTelemetry exporter on the classpath</li>
 *   <li>{@code APPLICATIONINSIGHTS_CONNECTION_STRING} environment variable set</li>
 *   <li>Azure AI Agents endpoint and credentials</li>
 * </ul>
 *
 * <p>To run this sample, add the following dependencies to your project:</p>
 * <pre>{@code
 * <dependency>
 *   <groupId>com.azure</groupId>
 *   <artifactId>azure-monitor-opentelemetry-exporter</artifactId>
 *   <version>1.0.0-beta.28</version>
 * </dependency>
 * }</pre>
 */
public class TracingAzureMonitorSample {

    /**
     * Main method to run the Azure Monitor tracing sample.
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        // 1. Set up Azure Monitor OpenTelemetry exporter
        // (Typically done via auto-instrumentation agent or manual SDK setup)
        //
        // String connectionString = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");
        // AzureMonitorExporterOptions exporterOptions = new AzureMonitorExporterOptions()
        //     .connectionString(connectionString);
        //
        // SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
        //     .addSpanProcessor(BatchSpanProcessor.builder(
        //         AzureMonitorTraceExporter.create(exporterOptions)).build())
        //     .setResource(Resource.getDefault().toBuilder()
        //         .put(ResourceAttributes.SERVICE_NAME, "my-agent-app").build())
        //     .build();
        // OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();

        // 2. Enable GenAI tracing with content recording for full prompt/response capture
        GenAiTracingConfiguration.enableGenAiTracing(
            new GenAiTracingOptions().setContentRecording(true));

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

        // 4. Create an agent
        String agentName = "AzureMonitorTracingSample";
        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName,
            new PromptAgentDefinition("gpt-4.1")
                .setInstructions("You are a travel assistant. Help users plan trips."));

        System.out.println("Agent created: " + agent.getName());

        // 5. Generate a response
        AzureCreateResponseOptions azureOptions = new AzureCreateResponseOptions()
            .setAgentReference(new AgentReference(agentName));
        ResponseCreateParams.Builder params = ResponseCreateParams.builder()
            .model(agentName)
            .input("Plan a 3-day trip to Tokyo.");

        Response response = responsesClient.createAzureResponse(azureOptions, params);
        System.out.println("Response generated. Check Azure Monitor for traces.");

        // 6. Clean up
        agentsClient.deleteAgent(agentName);

        // 7. Disable tracing
        GenAiTracingConfiguration.disableGenAiTracing();
        System.out.println("Done. Traces will appear in Application Insights within a few minutes.");
    }
}
