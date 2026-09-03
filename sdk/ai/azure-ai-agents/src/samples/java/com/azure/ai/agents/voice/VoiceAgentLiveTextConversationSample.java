// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaAgentEndpointConversationsClient;
import com.azure.ai.agents.BetaVoiceAgentWebSocketClient;
import com.azure.ai.agents.VoiceAgentWebSocketSessionClient;
import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates a synchronous, typed, multi-turn realtime conversation with a persisted voice agent.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - Optional. The voice agent name. Defaults to
 *   {@code sample-live-text-conversation-agent-java}.</li>
 * </ul>
 */
public class VoiceAgentLiveTextConversationSample {
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(45);

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME",
            "sample-live-text-conversation-agent-java");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true);
        AgentsClient agents = builder.buildAgentsClient();
        BetaVoiceAgentWebSocketClient realtime = builder.buildBetaVoiceAgentWebSocketClient();
        BetaAgentEndpointConversationsClient conversations = builder.buildBetaAgentEndpointConversationsClient();

        Map<String, String> request = new LinkedHashMap<>();
        request.put("kind", "voice");
        request.put("name", agentName);
        AgentDetails generated = agents.generateAgent(BinaryData.fromObject(request));

        try {
            AgentVersionDetails latest = generated.getVersions().getLatest();
            VoiceAgentDefinition definition = (VoiceAgentDefinition) latest.getDefinition();
            agents.createAgentVersion(agentName, new CreateAgentVersionInput(definition.setStore(true)));

            AtomicReference<String> conversationId = new AtomicReference<>();
            try (VoiceAgentRealtimeSampleUtils.SpeakerPlayer player
                    = new VoiceAgentRealtimeSampleUtils.SpeakerPlayer();
                VoiceAgentWebSocketSessionClient session = realtime.connect(agentName);
                Scanner scanner = new Scanner(System.in)) {
                AtomicReference<CompletableFuture<Void>> responseCompleted = new AtomicReference<>();
                ExecutorService receiver = Executors.newSingleThreadExecutor();
                receiver.submit(() -> {
                    try {
                        for (RealtimeServerEvent event : session.receiveEvents()) {
                            if (VoiceAgentRealtimeSampleUtils.handleResponseEvent(event, conversationId, player)) {
                                CompletableFuture<Void> completion = responseCompleted.getAndSet(null);
                                if (completion != null) {
                                    completion.complete(null);
                                }
                            }
                        }
                    } catch (RuntimeException error) {
                        CompletableFuture<Void> completion = responseCompleted.getAndSet(null);
                        if (completion != null) {
                            completion.completeExceptionally(error);
                        }
                    }
                });

                try {
                    System.out.println("Type a message and press Enter. Blank line (or 'exit') ends the session.");
                    while (true) {
                        System.out.print("You:  ");
                        String prompt = scanner.nextLine().trim();
                        String normalized = prompt.toLowerCase(Locale.ROOT);
                        if (prompt.isEmpty() || "exit".equals(normalized) || "quit".equals(normalized)) {
                            break;
                        }

                        CompletableFuture<Void> completion = new CompletableFuture<>();
                        responseCompleted.set(completion);
                        session.sendText(prompt);
                        session.createResponse();
                        if (!awaitResponse(session, completion)) {
                            break;
                        }
                    }
                } finally {
                    receiver.shutdownNow();
                }
                System.out.printf("(received %.2fs of reply audio%s)%n", player.getSecondsReceived(),
                    player.isEnabled() ? " and played it" : "");
            }

            if (conversationId.get() != null) {
                VoiceAgentRealtimeSampleUtils.readConversation(conversations, agentName, conversationId.get());
            } else {
                System.out.println("No persisted conversation ID was returned.");
            }
        } finally {
            agents.deleteAgent(agentName);
            System.out.println("Deleted voice agent: " + agentName);
        }
    }

    private static boolean awaitResponse(VoiceAgentWebSocketSessionClient session,
        CompletableFuture<Void> completion) {
        try {
            completion.get(RESPONSE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        } catch (TimeoutException error) {
            System.out.println("Timed out waiting for the agent's reply; cancelling the active response.");
            session.cancelResponse();
            try {
                completion.get(10, TimeUnit.SECONDS);
                return true;
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException | TimeoutException ignored) {
                return false;
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException error) {
            throw new IllegalStateException("The realtime receive loop failed.", error.getCause());
        }
    }
}
