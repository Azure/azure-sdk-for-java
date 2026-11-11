// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.VoiceAgentLiveClientDelegation;
import com.azure.ai.agents.models.VoiceAgentLiveDefinition;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.live.SessionConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Creates a temporary GPT Live agent version, updates a live session, appends instructions, and closes the session.
 * Silent PCM frames advance the Live timeline while the sample waits for the instruction acknowledgement; no audio
 * devices or user input are needed. The sample deletes the agent version it creates.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_MODEL} - The GPT Live deployment name.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - Optional prefix for the temporary agent name.</li>
 * </ul>
 */
public class GptLiveConnectionAsyncSample {
    private static final Duration EVENT_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration AUDIO_FRAME_INTERVAL = Duration.ofMillis(50);
    private static final String APPENDED_INSTRUCTIONS = "Keep responses brief. Do not invent external facts.";

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_PROJECT_ENDPOINT");
        String model = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_GPT_LIVE_MODEL");
        String prefix = configuration.get("FOUNDRY_VOICE_AGENT_NAME", "sample-live-connection");
        String agentName = prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .allowPreview(true);
        AgentsAsyncClient agents = builder.buildAgentsAsyncClient();
        VoiceAgentLiveDefinition definition = new VoiceAgentLiveDefinition(model)
            .setInstructions("You are a concise, friendly voice assistant.")
            .setAudio(SessionConfig.Audio.builder()
                .audioPcmFormat(com.openai.models.live.AudioFormat.AudioPcm.Rate._24000)
                .build())
            .setDelegation(new VoiceAgentLiveClientDelegation())
            .setStore(false);
        VoiceAgentLiveWebSocketConnectionOptions options = new VoiceAgentLiveWebSocketConnectionOptions()
            .setStoreEnabled(false);
        VoiceAgentLiveWebSocketConnectionOptions sessionOptions = options;

        Mono.usingWhen(agents.createAgentVersion(agentName, new CreateAgentVersionInput(definition)),
            created -> {
                sessionOptions.setAgentVersionOverride(created.getVersion());
                return Mono.usingWhen(builder.beta().buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                    .openWebSocketSession(agentName, sessionOptions), GptLiveConnectionAsyncSample::runConnection,
                    BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync,
                    (session, error) -> session.closeAsync(), BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync);
            },
            created -> agents.deleteAgentVersion(agentName, created.getVersion()),
            (created, error) -> agents.deleteAgentVersion(agentName, created.getVersion()),
            created -> agents.deleteAgentVersion(agentName, created.getVersion()))
            .block();
    }

    static Mono<Void> runConnection(BetaVoiceAgentsLiveWebSocketSessionAsyncClient session) {
        Sinks.Empty<Void> started = Sinks.empty();
        Sinks.Empty<Void> updated = Sinks.empty();
        Sinks.Empty<Void> instructionsAppended = Sinks.empty();
        Sinks.Empty<Void> closed = Sinks.empty();
        AtomicBoolean closing = new AtomicBoolean();
        AtomicBoolean sessionClosed = new AtomicBoolean();

        Mono<Void> receive = session.receiveRawEvents()
            .map(GptLiveFunctionToolSample::object)
            .doOnNext(event -> {
                String type = (String) event.get("type");
                if ("error".equals(type)) {
                    throw new IllegalStateException("GPT Live service error: " + event);
                }
                if ("session.started".equals(type)) {
                    Map<?, ?> sessionInfo = GptLiveFunctionToolSample.object(event.get("session"));
                    System.out.println("Live session started: " + sessionInfo.get("id"));
                    started.tryEmitEmpty();
                } else if ("session.updated".equals(type)) {
                    updated.tryEmitEmpty();
                } else if ("session.instructions.appended".equals(type)) {
                    instructionsAppended.tryEmitEmpty();
                } else if ("session.closed".equals(type)) {
                    String reason = (String) event.get("reason");
                    if (!closing.get() || !"close_requested".equals(reason)) {
                        throw new IllegalStateException("GPT Live closed unexpectedly: " + event);
                    }
                    sessionClosed.set(true);
                    closed.tryEmitEmpty();
                }
            })
            .takeUntil(event -> "session.closed".equals(event.get("type")))
            .then(Mono.defer(() -> sessionClosed.get() ? Mono.<Void>never()
                : Mono.error(new IllegalStateException("GPT Live disconnected without session.closed."))));

        Mono<Void> workflow = started.asMono().timeout(EVENT_TIMEOUT)
            .then(session.sendEvent(sessionUpdateEvent()))
            .then(updated.asMono().timeout(EVENT_TIMEOUT))
            .then(session.sendEvent(instructionsAppendEvent()))
            .then(waitForInstructionAppend(session, instructionsAppended))
            .then(Mono.fromRunnable(() -> System.out.println("Confirmed session.updated and "
                + "session.instructions.appended.")))
            .then(Mono.defer(() -> {
                closing.set(true);
                return session.sendEvent(GptLiveSampleUtils.closeSession())
                    .then(closed.asMono().timeout(EVENT_TIMEOUT));
            }));

        return Mono.firstWithSignal(receive, workflow);
    }

    private static BinaryData sessionUpdateEvent() {
        Map<String, Object> delegation = new LinkedHashMap<>();
        delegation.put("type", "client");
        Map<String, Object> session = new LinkedHashMap<>();
        session.put("type", "realtime");
        session.put("delegation", delegation);
        return GptLiveSampleUtils.event("session.update", "session", session);
    }

    private static BinaryData instructionsAppendEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "session.instructions.append");
        event.put("content", APPENDED_INSTRUCTIONS);
        event.put("delegation_id", null);
        return BinaryData.fromObject(event);
    }

    private static Mono<Void> waitForInstructionAppend(BetaVoiceAgentsLiveWebSocketSessionAsyncClient session,
        Sinks.Empty<Void> instructionsAppended) {
        byte[] silence = new byte[2400];
        return session.sendEvent(GptLiveSampleUtils.audio(silence))
            .thenMany(Flux.interval(AUDIO_FRAME_INTERVAL)
                .concatMap(ignored -> session.sendEvent(GptLiveSampleUtils.audio(silence)))
                .takeUntilOther(instructionsAppended.asMono().timeout(EVENT_TIMEOUT)))
            .then();
    }
}
