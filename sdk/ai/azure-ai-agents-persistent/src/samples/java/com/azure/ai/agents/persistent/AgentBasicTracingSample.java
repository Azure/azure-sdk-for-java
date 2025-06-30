// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.PersistentAgentsAdministrationClientTracer;
import com.azure.ai.agents.persistent.models.CodeInterpreterToolDefinition;
import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import java.util.Arrays;

import static com.azure.ai.agents.persistent.SampleUtils.configureOpenTelemetryEndpointTracing;
import static com.azure.ai.agents.persistent.SampleUtils.printRunMessages;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletion;

public final class AgentBasicTracingSample {

    @SuppressWarnings("try")
    public static void main(String[] args) {
        final OpenTelemetrySdk telemetrySdk = configureOpenTelemetryEndpointTracing();
        final Tracer tracer = telemetrySdk.getTracer(PersistentAgentsAdministrationClientTracer.class.getName());

        final Span span = tracer.spanBuilder("AgentBasicTracingSample.main").startSpan();
        try (AutoCloseable scope = span.makeCurrent()) {
            PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
                .credential(new DefaultAzureCredentialBuilder().build());
            PersistentAgentsClient agentsClient = clientBuilder.buildClient();
            PersistentAgentsAdministrationClient administrationClient = agentsClient.getPersistentAgentsAdministrationClient();
            ThreadsClient threadsClient = agentsClient.getThreadsClient();
            MessagesClient messagesClient = agentsClient.getMessagesClient();
            RunsClient runsClient = agentsClient.getRunsClient();

            String agentName = "basic_example";
            CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
                .setName(agentName)
                .setInstructions("You are a helpful agent")
                .setTools(Arrays.asList(new CodeInterpreterToolDefinition()));
            PersistentAgent agent = administrationClient.createAgent(createAgentOptions);

            PersistentAgentThread thread = threadsClient.createThread();
            ThreadMessage createdMessage = messagesClient.createMessage(
                thread.getId(),
                MessageRole.USER,
                "I need to solve the equation `3x + 11 = 14`. Can you help me?");

            try {
                //run agent
                CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
                    .setAdditionalInstructions("");
                ThreadRun threadRun = runsClient.createRun(createRunOptions);

                waitForRunCompletion(thread.getId(), threadRun, runsClient);
                printRunMessages(messagesClient, thread.getId());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                //cleanup
                threadsClient.deleteThread(thread.getId());
                administrationClient.deleteAgent(agent.getId());
            }
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
    }
}
