// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentDefinition;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.ClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;

/**
 * Sample showing how to configure OpenTelemetry tracing for {@link AgentsClient} through the client's
 * {@link ClientOptions}. Set {@code AZURE_EXPERIMENTAL_ENABLE_GENAI_TRACING=true} to enable experimental GenAI spans.
 * Message and agent content capture is disabled by default; set
 * {@code OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT=true} to enable it.
 * Review your privacy requirements before enabling content capture.
 */
public final class TracingSample {

    /**
     * Runs the tracing sample.
     *
     * @param args ignored.
     */
    public static void main(String[] args) {
        String endpoint = System.getenv("FOUNDRY_PROJECT_ENDPOINT");

        // BEGIN: com.azure.ai.agents.tracing
        // Set AZURE_EXPERIMENTAL_ENABLE_GENAI_TRACING=true before running this sample.
        // Configure OpenTelemetry exporters and sampling, then pass the SDK through ClientOptions.
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().build();
        AgentsClient agentsClient = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .clientOptions(new ClientOptions()
                .setTracingOptions(new OpenTelemetryTracingOptions().setOpenTelemetry(openTelemetry)))
            .buildAgentsClient();

        AgentDefinition definition
            = new PromptAgentDefinition("gpt-4o").setInstructions("You are a helpful assistant.");
        AgentVersionDetails agent = agentsClient.createAgentVersion("my-agent", definition);
        System.out.printf("Created agent %s (traced).%n", agent.getName());
        // END: com.azure.ai.agents.tracing
    }

    private TracingSample() {
    }
}
