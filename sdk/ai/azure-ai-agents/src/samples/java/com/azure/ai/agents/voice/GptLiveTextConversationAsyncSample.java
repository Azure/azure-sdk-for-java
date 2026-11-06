// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.VoiceAgentLiveDefinition;
import com.azure.ai.agents.models.VoiceAgentLiveResponsesDelegation;
import com.openai.models.live.ResponsesDelegationConfig;
import com.openai.models.live.SessionConfig;
import com.openai.core.JsonValue;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.ResponseCreateEvent;
import com.openai.models.live.ResponseItemCreateEvent;
import com.openai.models.live.SessionCloseEvent;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputText;
import com.azure.core.util.Configuration;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.live.ServerEvent;
import com.openai.models.responses.ResponseStreamEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;


/**
 * Runs a nonblocking, multi-turn GPT Live text conversation using Responses delegation.
 * Pass each prompt as a command-line argument, or run without arguments for one example question.
 * Prints backend text without console input, audio devices, or silent PCM.
 * See {@link GptLiveInteractiveTextConversationAsyncSample} for the advanced console-and-speaker scenario.
 * Agent creation/deletion run outside the asynchronous session on the main thread.
 * The uniquely named agent is deleted unless FOUNDRY_KEEP_GPT_LIVE_AGENT=true.
 * The standalone main method shuts down shared Reactor schedulers on exit; do not invoke it inside a shared application.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_MODEL} - Optional. The GPT Live deployment name. Defaults to {@code gpt-live-1}.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_BACKEND_MODEL} - Optional. The Responses backend deployment name in the Live
 *   resource. Defaults to {@code gpt-5.6-terra}.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_STORE} - Optional. Set to {@code true} to persist conversation content and audio
 *   and request a Foundry conversation ID. Defaults to {@code false}. Also keep the agent for later REST reads.</li>
 *   <li>{@code FOUNDRY_KEEP_GPT_LIVE_AGENT} - Optional. Set to {@code true} to keep the agent after the sample.
 *   Defaults to {@code false}.</li>
 * </ul>
 */
public class GptLiveTextConversationAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_GPT_LIVE_MODEL");
        String backendModel = configuration.get("FOUNDRY_GPT_LIVE_BACKEND_MODEL");
        boolean keepAgent = Boolean.parseBoolean(configuration.get("FOUNDRY_KEEP_GPT_LIVE_AGENT", "false"));
        boolean store = Boolean.parseBoolean(configuration.get("FOUNDRY_GPT_LIVE_STORE", "false"));

        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_PROJECT_ENDPOINT before running this sample.");
        }
        if (model == null) {
            model = "gpt-live-1";
        } else if (model.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_GPT_LIVE_MODEL before running this sample.");
        }
        if (backendModel == null) {
            backendModel = "gpt-5.6-terra";
        } else if (backendModel.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_GPT_LIVE_BACKEND_MODEL before running this sample.");
        }

        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .allowPreview(true);
        VoiceAgentLiveDefinition definition = new VoiceAgentLiveDefinition(model)
            .setInstructions("You are a helpful voice assistant. Delegate requests to your Responses backend.")
            .setAudio(SessionConfig.Audio.builder()
                .audioPcmFormat(com.openai.models.live.AudioFormat.AudioPcm.Rate._24000).build())
            .setDelegation(new VoiceAgentLiveResponsesDelegation(ResponsesDelegationConfig.builder()
                .model(backendModel).instructions("Answer the user briefly and helpfully.").build()));
        String agentName = "gpt-live-text-async-" + UUID.randomUUID();
        AgentsClient agents = builder.buildAgentsClient();
        agents.createAgentVersion(agentName, new CreateAgentVersionInput(definition));
        System.out.println("Created GPT Live agent: " + agentName);
        try {
            Mono.usingWhen(builder.beta().buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession(agentName, new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(store)),
                session -> runConversation(session,
                    Arrays.asList(args.length == 0 ? new String[] { "What can you help me with?" } : args)),
                BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync,
                (session, error) -> session.closeAsync(),
                BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync).block();
        } finally {
            try {
                if (!keepAgent) {
                    agents.deleteAgent(agentName);
                }
            } finally {
                Schedulers.shutdownNow();
            }
        }
    }

    static Mono<Void> runConversation(BetaVoiceAgentsLiveWebSocketSessionAsyncClient session, List<String> prompts) {
        return Mono.defer(() -> {
            Sinks.Empty<Void> ready = Sinks.empty();
            Sinks.Empty<Void> closed = Sinks.empty();
            AtomicBoolean started = new AtomicBoolean();
            AtomicBoolean closing = new AtomicBoolean();
            AtomicBoolean finalized = new AtomicBoolean();
            AtomicBoolean remoteHangup = new AtomicBoolean();
            AtomicReference<Sinks.Empty<Void>> responseCompleted = new AtomicReference<>();
            Mono<Void> receive = session.receiveEvents()
                .publishOn(Schedulers.boundedElastic(), 1)
                .takeUntil(ServerEvent::isSessionClosed)
                .doOnNext(event -> {
                    GptLiveSampleUtils.checkError(event);
                    if (event.isSessionStarted()) {
                        if (!started.compareAndSet(false, true)) {
                            throw new IllegalStateException("Duplicate session.started event.");
                        }
                        JsonValue conversationId = event.asSessionStarted()._additionalProperties().get("conversation_id");
                        System.out.println(conversationId != null && conversationId.asString().isPresent()
                            ? "Live session started; conversation_id=" + conversationId.asString().get()
                            : "Live session started; conversation_id not provided (persistence may be disabled).");
                        ready.tryEmitEmpty();
                    } else if (event.isResponseEvent()) {
                        ResponseStreamEvent response = JsonValue.from(event.asResponseEvent().event()._additionalProperties())
                            .convert(ResponseStreamEvent.class);
                        if (response.isOutputTextDelta()) {
                            System.out.print(response.asOutputTextDelta().delta());
                        }
                        if (response.isCompleted()) {
                            Sinks.Empty<Void> completion = responseCompleted.getAndSet(null);
                            if (!started.get() || completion == null || closing.get()) {
                                throw new IllegalStateException("Unexpected GPT Live response lifecycle event.");
                            }
                            System.out.println();
                            completion.tryEmitEmpty();
                        }
                    } else if (event.isSessionClosed()) {
                        String reason = event.asSessionClosed().reason().toString();
                        remoteHangup.set(started.get() && "remote_hangup".equals(reason));
                        if (!remoteHangup.get() && !(closing.get() && "close_requested".equals(reason))) {
                            throw new IllegalStateException("GPT Live closed before all turns completed. Event: "
                                + event + "; transport close code=" + session.getCloseCode()
                                + ", reason=" + session.getCloseReason());
                        }
                        finalized.set(true);
                        System.out.println("Live session closed (" + reason + ").");
                        closed.tryEmitEmpty();
                    }
                })
                .then(Mono.defer(() -> {
                    if (!finalized.get()) {
                        return Mono.error(new IllegalStateException("GPT Live disconnected without session.closed."));
                    }
                    return remoteHangup.get() ? Mono.empty() : Mono.<Void>never();
                }));
            Mono<Void> conversation = ready.asMono().timeout(Duration.ofSeconds(60))
                .thenMany(Flux.fromIterable(prompts).concatMap(prompt -> Mono.defer(() -> {
                    Sinks.Empty<Void> completion = Sinks.empty();
                    responseCompleted.set(completion);
                    System.out.println("You: " + prompt);
                    System.out.print("Assistant: ");
                    return session.sendEvent(ClientEvent.ofResponseItemCreate(ResponseItemCreateEvent.builder()
                        .item(EasyInputMessage.builder().role(EasyInputMessage.Role.USER)
                            .type(EasyInputMessage.Type.MESSAGE)
                            .contentOfResponseInputMessageContentList(Collections.singletonList(
                                ResponseInputContent.ofInputText(ResponseInputText.builder().text(prompt).build())))
                            .build()).build()))
                        .then(session.sendEvent(ClientEvent.ofResponseCreate(ResponseCreateEvent.builder().build())))
                        .then(completion.asMono()).timeout(Duration.ofSeconds(60));
                }), 1))
                .then(Mono.defer(() -> {
                    closing.set(true);
                    return session.sendEvent(ClientEvent.ofSessionClose(SessionCloseEvent.builder().build()))
                        .then(closed.asMono()).timeout(Duration.ofSeconds(60));
                }));
            return Mono.firstWithSignal(receive, conversation);
        });
    }

}
