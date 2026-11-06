// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.VoiceAgentLiveDefinition;
import com.azure.ai.agents.models.VoiceAgentLiveResponsesDelegation;
import com.azure.core.util.Configuration;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.live.ClientEvent;
import com.openai.core.JsonValue;
import com.openai.models.live.ResponseCreateEvent;
import com.openai.models.live.ResponseItemCreateEvent;
import com.openai.models.live.SessionCloseEvent;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputText;
import com.openai.models.live.ServerEvent;
import com.openai.models.live.ResponsesDelegationConfig;
import com.openai.models.live.SessionConfig;
import com.openai.models.responses.ResponseStreamEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import reactor.core.scheduler.Schedulers;


/**
 * Runs a synchronous, multi-turn GPT Live text conversation using Responses delegation.
 * Pass each prompt as a command-line argument, or run without arguments for one example question.
 * Prints backend text without console input, audio devices, background conversation workers, or silent PCM.
 * See {@link GptLiveInteractiveTextConversationSample} for the advanced console-and-speaker scenario.
 * A uniquely named agent is deleted on exit unless FOUNDRY_KEEP_GPT_LIVE_AGENT=true.
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
public class GptLiveTextConversationSample {
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
        String agentName = "gpt-live-text-" + UUID.randomUUID();
        AgentsClient agents = builder.buildAgentsClient();
        agents.createAgentVersion(agentName, new CreateAgentVersionInput(definition));
        System.out.println("Created GPT Live agent: " + agentName);
        try (BetaVoiceAgentsLiveWebSocketSessionClient session = builder.beta()
                .buildBetaVoiceAgentsLiveWebSocketClient()
                .openWebSocketSession(agentName, new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(store))) {
            runConversation(session, Arrays.asList(args.length == 0 ? new String[] { "What can you help me with?" } : args));
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

    static void runConversation(BetaVoiceAgentsLiveWebSocketSessionClient session, List<String> prompts) {
        runConversation(session.receiveEvents(Duration.ofSeconds(60)), session::sendEvent,
            () -> "transport close code=" + session.getCloseCode() + ", reason=" + session.getCloseReason(), prompts);
    }

    static void runConversation(Iterable<ServerEvent> events, Consumer<ClientEvent> sendEvent,
        Supplier<String> closeDetails, List<String> prompts) {
        Iterator<String> pending = prompts.iterator();
        boolean started = false;
        boolean awaitingResponse = false;
        boolean closing = false;
        for (ServerEvent event : events) {
            GptLiveSampleUtils.checkError(event);
            boolean nextTurn = false;
            if (event.isSessionStarted()) {
                if (started) {
                    throw new IllegalStateException("Duplicate session.started event.");
                }
                started = true;
                JsonValue conversationId = event.asSessionStarted()._additionalProperties().get("conversation_id");
                System.out.println(conversationId != null && conversationId.asString().isPresent()
                    ? "Live session started; conversation_id=" + conversationId.asString().get()
                    : "Live session started; conversation_id not provided (persistence may be disabled).");
                nextTurn = true;
            } else if (event.isResponseEvent()) {
                ResponseStreamEvent response = JsonValue.from(event.asResponseEvent().event()._additionalProperties())
                    .convert(ResponseStreamEvent.class);
                if (response.isOutputTextDelta()) {
                    System.out.print(response.asOutputTextDelta().delta());
                }
                if (response.isCompleted()) {
                    if (!started || !awaitingResponse || closing) {
                        throw new IllegalStateException("Unexpected GPT Live response lifecycle event.");
                    }
                    awaitingResponse = false;
                    System.out.println();
                    nextTurn = true;
                }
            } else if (event.isSessionClosed()) {
                String reason = event.asSessionClosed().reason().toString();
                if (!(started && "remote_hangup".equals(reason))
                    && !(closing && "close_requested".equals(reason))) {
                    throw new IllegalStateException("GPT Live closed before all turns completed. Event: " + event
                        + "; " + closeDetails.get());
                }
                System.out.println("Live session closed (" + reason + ").");
                return;
            }
            if (nextTurn) {
                if (pending.hasNext()) {
                    String prompt = pending.next();
                    System.out.println("You: " + prompt);
                    System.out.print("Assistant: ");
                    awaitingResponse = true;
                    sendEvent.accept(ClientEvent.ofResponseItemCreate(ResponseItemCreateEvent.builder()
                        .item(EasyInputMessage.builder().role(EasyInputMessage.Role.USER)
                            .type(EasyInputMessage.Type.MESSAGE)
                            .contentOfResponseInputMessageContentList(Collections.singletonList(
                                ResponseInputContent.ofInputText(ResponseInputText.builder().text(prompt).build())))
                            .build()).build()));
                    sendEvent.accept(ClientEvent.ofResponseCreate(ResponseCreateEvent.builder().build()));
                } else {
                    closing = true;
                    sendEvent.accept(ClientEvent.ofSessionClose(SessionCloseEvent.builder().build()));
                }
            }
        }
        throw new IllegalStateException("GPT Live disconnected without session.closed.");
    }

}
