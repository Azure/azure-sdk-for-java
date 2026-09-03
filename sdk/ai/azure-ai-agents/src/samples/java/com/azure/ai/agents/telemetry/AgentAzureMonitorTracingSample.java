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
 * Demonstrates exporting agent traces to Azure Monitor.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 *   <li>{@code APPLICATIONINSIGHTS_CONNECTION_STRING} - The Azure Monitor Application Insights connection string.</li>
 * </ul>
 */
public class AgentAzureMonitorTracingSample {
    @SuppressWarnings("try")
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");
        String connectionString = configuration.get("APPLICATIONINSIGHTS_CONNECTION_STRING");
        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        try (OpenTelemetrySdk telemetry
            = AgentTelemetrySampleUtils.configureAzureMonitorTelemetry(connectionString)) {
            Span span = telemetry.getTracer(AgentAzureMonitorTracingSample.class.getName())
                .spanBuilder("run-agent").startSpan();
            try (Scope ignored = span.makeCurrent()) {
                AgentTelemetrySampleUtils.runAgent(builder, model);
            } catch (RuntimeException error) {
                span.recordException(error);
                throw error;
            } finally {
                span.end();
            }
        }
    }
}
