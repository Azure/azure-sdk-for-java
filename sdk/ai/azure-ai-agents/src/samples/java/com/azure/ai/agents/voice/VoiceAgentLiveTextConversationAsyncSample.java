// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaAgentEndpointConversationsAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentWebSocketAsyncClient;
import com.azure.ai.agents.VoiceAgentWebSocketSessionAsyncClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates an asynchronous, typed, multi-turn realtime conversation with a persisted voice agent.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - Optional. The voice agent name. Defaults to
 *   {@code sample-live-text-conversation-agent-async-java}.</li>
 * </ul>
 */
public class VoiceAgentLiveTextConversationAsyncSample {
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(45);

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME",
            "sample-live-text-conversation-agent-async-java");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true);
        AgentsAsyncClient agents = builder.buildAgentsAsyncClient();
        BetaVoiceAgentWebSocketAsyncClient realtime = builder.buildBetaVoiceAgentWebSocketAsyncClient();
        BetaAgentEndpointConversationsAsyncClient conversations
            = builder.buildBetaAgentEndpointConversationsAsyncClient();

        Map<String, String> request = new LinkedHashMap<>();
        request.put("kind", "voice");
        request.put("name", agentName);

        AtomicReference<String> conversationId = new AtomicReference<>();
        VoiceAgentRealtimeSampleUtils.SpeakerPlayer player = new VoiceAgentRealtimeSampleUtils.SpeakerPlayer();
        Scanner scanner = new Scanner(System.in);

        agents.generateAgent(BinaryData.fromObject(request))
            .flatMap(generated -> {
                VoiceAgentDefinition definition
                    = (VoiceAgentDefinition) generated.getVersions().getLatest().getDefinition();
                return agents.createAgentVersion(agentName,
                    new CreateAgentVersionInput(definition.setStore(true)));
            })
            .then(Mono.usingWhen(realtime.connect(agentName),
                session -> runConversation(session, scanner, conversationId, player),
                VoiceAgentWebSocketSessionAsyncClient::closeAsync,
                (session, error) -> session.closeAsync(),
                VoiceAgentWebSocketSessionAsyncClient::closeAsync))
            .then(Mono.defer(() -> conversationId.get() == null
                ? Mono.fromRunnable(() -> System.out.println("No persisted conversation ID was returned."))
                : VoiceAgentRealtimeSampleUtils.readConversation(conversations, agentName, conversationId.get())))
            .then(agents.deleteAgent(agentName))
            .doOnSuccess(ignored -> System.out.println("Deleted voice agent: " + agentName))
            .onErrorResume(error -> agents.deleteAgent(agentName)
                .onErrorResume(cleanupError -> Mono.empty())
                .then(Mono.error(error)))
            .doFinally(signal -> {
                scanner.close();
                player.close();
            })
            .block();
    }

    private static Mono<Void> runConversation(VoiceAgentWebSocketSessionAsyncClient session, Scanner scanner,
        AtomicReference<String> conversationId, VoiceAgentRealtimeSampleUtils.SpeakerPlayer player) {
        AtomicReference<Sinks.One<Void>> responseCompleted = new AtomicReference<>();
        Disposable receiver = session.receiveEvents().subscribe(event -> {
            if (VoiceAgentRealtimeSampleUtils.handleResponseEvent(event, conversationId, player)) {
                Sinks.One<Void> completion = responseCompleted.getAndSet(null);
                if (completion != null) {
                    completion.tryEmitEmpty();
                }
            }
        }, error -> {
            Sinks.One<Void> completion = responseCompleted.getAndSet(null);
            if (completion != null) {
                completion.tryEmitError(error);
            }
        });

        System.out.println("Type a message and press Enter. Blank line (or 'exit') ends the session.");
        return prompt(session, scanner, responseCompleted)
            .doFinally(signal -> {
                receiver.dispose();
                System.out.printf("(received %.2fs of reply audio%s)%n", player.getSecondsReceived(),
                    player.isEnabled() ? " and played it" : "");
            });
    }

    private static Mono<Void> prompt(VoiceAgentWebSocketSessionAsyncClient session, Scanner scanner,
        AtomicReference<Sinks.One<Void>> responseCompleted) {
        return Mono.fromCallable(() -> {
            System.out.print("You:  ");
            return scanner.nextLine().trim();
        }).subscribeOn(Schedulers.boundedElastic()).flatMap(text -> {
            String normalized = text.toLowerCase(Locale.ROOT);
            if (text.isEmpty() || "exit".equals(normalized) || "quit".equals(normalized)) {
                return Mono.empty();
            }

            Sinks.One<Void> completion = Sinks.one();
            responseCompleted.set(completion);
            return session.sendText(text)
                .then(session.createResponse())
                .then(completion.asMono().timeout(RESPONSE_TIMEOUT))
                .onErrorResume(java.util.concurrent.TimeoutException.class, error -> {
                    System.out.println("Timed out waiting for the agent's reply; cancelling the active response.");
                    return session.cancelResponse()
                        .then(completion.asMono().timeout(Duration.ofSeconds(10)))
                        .onErrorResume(cancelError -> Mono.empty());
                })
                .then(Mono.defer(() -> prompt(session, scanner, responseCompleted)));
        });
    }
}
