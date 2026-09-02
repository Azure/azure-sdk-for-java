// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.SampleUtils;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

final class AgentTelemetrySampleUtils {
    private AgentTelemetrySampleUtils() {
    }

    static OpenTelemetrySdk configureConsoleTelemetry() {
        SdkTracerProvider provider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .build();
        return OpenTelemetrySdk.builder().setTracerProvider(provider).buildAndRegisterGlobal();
    }

    static OpenTelemetrySdk configureAzureMonitorTelemetry(String connectionString) {
        AutoConfiguredOpenTelemetrySdkBuilder builder = AutoConfiguredOpenTelemetrySdk.builder();
        AzureMonitorAutoConfigure.customize(builder, connectionString);
        return builder.setResultAsGlobal().build().getOpenTelemetrySdk();
    }

    static Response runAgent(AgentsClientBuilder builder, String model) {
        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        AgentVersionDetails agent = agentsClient.createAgentVersion("telemetry-sample-agent",
            new CreateAgentVersionInput(new PromptAgentDefinition(model)
                .setInstructions("Answer general questions concisely.")));
        try {
            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(agent)),
                ResponseCreateParams.builder().input("What is the capital of France?"));
            SampleUtils.printResponseText(response);
            return response;
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }
}
