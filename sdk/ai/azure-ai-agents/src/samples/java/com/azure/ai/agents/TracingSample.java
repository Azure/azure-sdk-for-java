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
 * Sample showing how experimental GenAI tracing is enabled for {@link AgentsClient} by configuring an OpenTelemetry
 * implementation through the client's {@link ClientOptions}. When an OpenTelemetry SDK is configured the client emits
 * GenAI semantic-convention spans and metrics automatically; there is no explicit opt-in call. Set the
 * {@code AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED} environment variable to also capture message/agent content.
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
        // Configure any OpenTelemetry SDK (exporters, sampling, etc.) and pass it through ClientOptions. The client
        // then emits GenAI spans (create_agent, chat / invoke_agent, create_conversation) and metrics automatically.
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
