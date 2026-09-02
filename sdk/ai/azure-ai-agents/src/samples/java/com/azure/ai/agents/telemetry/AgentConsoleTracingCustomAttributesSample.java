// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;

/**
 * Demonstrates adding application-specific attributes to an agent trace.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class AgentConsoleTracingCustomAttributesSample {
    @SuppressWarnings("try")
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");
        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        try (OpenTelemetrySdk telemetry = AgentTelemetrySampleUtils.configureConsoleTelemetry()) {
            Span span = telemetry.getTracer(AgentConsoleTracingCustomAttributesSample.class.getName())
                .spanBuilder("run-agent")
                .setAttribute("sample.scenario", "custom-attributes")
                .setAttribute("sample.agent.type", "prompt")
                .startSpan();
            try (Scope ignored = span.makeCurrent()) {
                AgentTelemetrySampleUtils.runAgent(builder, model);
                span.setAttribute("sample.completed", true);
            } catch (RuntimeException error) {
                span.recordException(error);
                throw error;
            } finally {
                span.end();
            }
        }
    }
}
