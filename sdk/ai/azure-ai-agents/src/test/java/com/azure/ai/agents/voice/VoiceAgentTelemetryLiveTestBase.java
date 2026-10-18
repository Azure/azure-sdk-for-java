// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.RealtimeSessionCreatedEvent;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.ai.agents.models.VoiceAgentSessionResponseConfiguration;
import com.azure.ai.agents.models.VoiceModelType;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class VoiceAgentTelemetryLiveTestBase {
    private static final AttributeKey<String> GEN_AI_OPERATION_NAME = AttributeKey.stringKey("gen_ai.operation.name");
    private static final AttributeKey<String> GEN_AI_AGENT_NAME = AttributeKey.stringKey("gen_ai.agent.name");
    private static final AttributeKey<String> GEN_AI_REQUEST_MODEL = AttributeKey.stringKey("gen_ai.request.model");
    private static final AttributeKey<String> GEN_AI_VOICE_SESSION_ID
        = AttributeKey.stringKey("gen_ai.voice.session_id");
    private static final AttributeKey<String> GEN_AI_VOICE_EVENT_TYPE
        = AttributeKey.stringKey("gen_ai.voice.event_type");

    protected static TelemetryTestContext createTestContext() {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_VOICE_MODEL_NAME");
        assertNotNull(endpoint, "FOUNDRY_PROJECT_ENDPOINT is required for live testing.");
        assertNotNull(model, "FOUNDRY_VOICE_MODEL_NAME is required for live testing.");

        GlobalOpenTelemetry.resetForTest();
        InMemorySpanExporter exporter = InMemorySpanExporter.create();
        SdkTracerProvider tracerProvider
            = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
        OpenTelemetrySdk openTelemetry
            = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();

        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .allowPreview(true)
            .credential(new DefaultAzureCredentialBuilder().build());
        AgentsClient agents = builder.buildAgentsClient();
        String agentName = "test-telemetry-" + UUID.randomUUID();
        TelemetryTestContext context = new TelemetryTestContext(builder, agents, agentName, exporter, openTelemetry);
        try {
            VoiceAgentDefinition definition = new VoiceAgentDefinition().setModelType(VoiceModelType.MANAGED)
                .setModel(model)
                .setInstructions("You are a concise test assistant.");
            agents.createAgentVersion(agentName, new CreateAgentVersionInput(definition));
            context.agentCreated = true;
            return context;
        } catch (Throwable throwable) {
            context.close();
            throw throwable;
        }
    }

    protected static final class TelemetryTestContext implements AutoCloseable {
        private final AgentsClientBuilder builder;
        private final AgentsClient agents;
        private final String agentName;
        private final InMemorySpanExporter exporter;
        private final OpenTelemetrySdk openTelemetry;
        private boolean agentCreated;

        private TelemetryTestContext(AgentsClientBuilder builder, AgentsClient agents, String agentName,
            InMemorySpanExporter exporter, OpenTelemetrySdk openTelemetry) {
            this.builder = builder;
            this.agents = agents;
            this.agentName = agentName;
            this.exporter = exporter;
            this.openTelemetry = openTelemetry;
        }

        AgentsClientBuilder getBuilder() {
            return builder;
        }

        String getAgentName() {
            return agentName;
        }

        void assertTelemetry(RealtimeSessionCreatedEvent sessionCreated) {
            VoiceAgentSessionResponseConfiguration sessionConfiguration
                = sessionCreated.getSessionAsVoiceAgentSessionResponseConfiguration();
            assertNotNull(sessionConfiguration);
            String resolvedModel = sessionConfiguration.getModel();
            assertNotNull(resolvedModel);

            List<SpanData> spans = exporter.getFinishedSpanItems();
            SpanData connect = spans.stream()
                .filter(span -> "connect".equals(span.getAttributes().get(GEN_AI_OPERATION_NAME)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected an exported connect span."));
            assertEquals(agentName, connect.getAttributes().get(GEN_AI_AGENT_NAME));
            assertEquals(resolvedModel, connect.getAttributes().get(GEN_AI_REQUEST_MODEL));
            assertNotNull(connect.getAttributes().get(GEN_AI_VOICE_SESSION_ID));
            assertTrue(spans.stream()
                .anyMatch(span -> "session.created".equals(span.getAttributes().get(GEN_AI_VOICE_EVENT_TYPE))));
            assertTrue(
                spans.stream().anyMatch(span -> "close".equals(span.getAttributes().get(GEN_AI_OPERATION_NAME))));
        }

        @Override
        public void close() {
            try {
                if (agentCreated) {
                    agents.deleteAgent(agentName);
                }
            } finally {
                openTelemetry.close();
                GlobalOpenTelemetry.resetForTest();
            }
        }
    }
}
