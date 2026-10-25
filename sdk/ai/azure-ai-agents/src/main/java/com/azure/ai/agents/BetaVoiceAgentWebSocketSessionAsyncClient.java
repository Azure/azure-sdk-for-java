// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketUtils;
import com.azure.ai.agents.implementation.realtime.WebSocketSessionAsyncTransport;
import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.ai.agents.models.RealtimeClientEvent;
import com.azure.ai.agents.models.RealtimeConversationItemCreateEvent;
import com.azure.ai.agents.models.RealtimeInputAudioBufferAppendEvent;
import com.azure.ai.agents.models.RealtimeInputAudioBufferClearEvent;
import com.azure.ai.agents.models.RealtimeInputAudioBufferCommitEvent;
import com.azure.ai.agents.models.RealtimeResponseCancelEvent;
import com.azure.ai.agents.models.RealtimeResponseCreateEvent;
import com.azure.ai.agents.models.RealtimeConversationItem;
import com.azure.ai.agents.models.RealtimeConversationItemFunctionCallOutput;
import com.azure.ai.agents.models.RealtimeConversationItemUserMessage;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.VoiceAgentResponseCreateOptions;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.BinaryData;
import com.openai.models.realtime.RealtimeConversationItemUserMessage.Content;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * An asynchronous bidirectional realtime session connected to a Foundry voice agent.
 *
 * <p>Instances are created by {@link BetaVoiceAgentWebSocketAsyncClient#openWebSocketSession(String)}. A session supports one
 * subscriber to {@link #receiveEvents()}. Close the session when it is no longer needed.</p>
 */
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaVoiceAgentWebSocketSessionAsyncClient implements AsyncCloseable, AutoCloseable {
    private final WebSocketSessionAsyncTransport<RealtimeServerEvent> transport;

    BetaVoiceAgentWebSocketSessionAsyncClient(VoiceAgentWebSocketClientConfiguration configuration, String agentName,
        VoiceAgentWebSocketConnectionOptions options) {
        this(configuration, agentName, options, HttpClient.create());
    }

    BetaVoiceAgentWebSocketSessionAsyncClient(VoiceAgentWebSocketClientConfiguration configuration, String agentName,
        VoiceAgentWebSocketConnectionOptions options, HttpClient httpClient) {
        VoiceAgentWebSocketConnectionOptions sessionOptions
            = options == null ? new VoiceAgentWebSocketConnectionOptions() : options;
        transport = new WebSocketSessionAsyncTransport<>(configuration,
            VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, agentName, sessionOptions), sessionOptions,
            httpClient, VoiceAgentWebSocketUtils.SUBPROTOCOL,
            token -> VoiceAgentWebSocketUtils.buildHeaders(configuration, sessionOptions, token),
            VoiceAgentWebSocketUtils::deserializeEvent);
    }

    Mono<Void> connect() {
        return transport.connect();
    }

    /**
     * Gets the WebSocket endpoint used by this session.
     *
     * @return the WebSocket endpoint.
     */
    public String getEndpoint() {
        return transport.getEndpoint();
    }

    /**
     * Determines whether the WebSocket session is open.
     *
     * @return {@code true} when the session is open.
     */
    public boolean isOpen() {
        return transport.isOpen();
    }

    /**
     * Gets the peer close code after the session closes.
     *
     * @return the close code, or {@code null} when no close frame has been received.
     */
    public Integer getCloseCode() {
        return transport.getCloseCode();
    }

    /**
     * Gets the peer close reason after the session closes.
     *
     * @return the close reason, or {@code null} when no close frame has been received.
     */
    public String getCloseReason() {
        return transport.getCloseReason();
    }

    /**
     * Receives typed server events in wire order. Only one subscriber is supported per session.
     *
     * @return the server event stream.
     */
    public Flux<RealtimeServerEvent> receiveEvents() {
        return transport.receiveEvents();
    }

    /**
     * Sends a typed realtime client event.
     *
     * @param event the event to send.
     * @return a completion signal emitted after the frame is written.
     */
    public Mono<Void> sendEvent(RealtimeClientEvent event) {
        Objects.requireNonNull(event, "'event' cannot be null.");
        return Mono.defer(() -> {
            try {
                return transport.sendEvent(BinaryData.fromString(event.toJsonString()));
            } catch (IOException error) {
                return Mono
                    .error(new IllegalArgumentException("Failed to serialize the realtime client event.", error));
            }
        });
    }

    /**
     * Sends a JSON object, including event types and fields unknown to this SDK.
     * @param event the complete JSON event.
     * @return completion after the frame is written, or an error for invalid JSON or a closed session.
     */
    public Mono<Void> sendEvent(BinaryData event) {
        return transport.sendEvent(event);
    }

    /**
     * Adds an item to the session conversation.
     *
     * @param item the item to add.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> createConversationItem(RealtimeConversationItem item) {
        return createConversationItem(item, null);
    }

    /**
     * Adds an item after a specific conversation item.
     *
     * @param item the item to add.
     * @param previousItemId the preceding item identifier, or {@code null} to append.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> createConversationItem(RealtimeConversationItem item, String previousItemId) {
        Objects.requireNonNull(item, "'item' cannot be null.");
        return sendEvent(new RealtimeConversationItemCreateEvent(item).setPreviousItemId(previousItemId));
    }

    /**
     * Adds a user text message to the session conversation.
     *
     * @param text the user message.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> sendText(String text) {
        Objects.requireNonNull(text, "'text' cannot be null.");
        Content content = Content.builder().type(Content.Type.INPUT_TEXT).text(text).build();
        return createConversationItem(new RealtimeConversationItemUserMessage(Collections.singletonList(content)));
    }

    /**
     * Appends PCM or encoded audio bytes to the input audio buffer.
     *
     * @param audio the bytes in the input format configured by the voice agent.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> appendInputAudio(BinaryData audio) {
        Objects.requireNonNull(audio, "'audio' cannot be null.");
        String encoded = Base64.getEncoder().encodeToString(audio.toBytes());
        return sendEvent(new RealtimeInputAudioBufferAppendEvent(encoded));
    }

    /**
     * Clears the input audio buffer.
     *
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> clearInputAudio() {
        return sendEvent(new RealtimeInputAudioBufferClearEvent());
    }

    /**
     * Commits the input audio buffer.
     *
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> commitInputAudio() {
        return sendEvent(new RealtimeInputAudioBufferCommitEvent());
    }

    /**
     * Requests a response using the persisted voice-agent configuration.
     *
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> createResponse() {
        return sendEvent(new RealtimeResponseCreateEvent());
    }

    /**
     * Requests a response with per-response options.
     *
     * @param responseOptions the response options.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> createResponse(VoiceAgentResponseCreateOptions responseOptions) {
        Objects.requireNonNull(responseOptions, "'responseOptions' cannot be null.");
        return sendEvent(new RealtimeResponseCreateEvent().setResponse(responseOptions));
    }

    /**
     * Cancels the response currently writing to the default conversation.
     *
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> cancelResponse() {
        return sendEvent(new RealtimeResponseCancelEvent());
    }

    /**
     * Cancels a specific response.
     *
     * @param responseId the response identifier.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> cancelResponse(String responseId) {
        Objects.requireNonNull(responseId, "'responseId' cannot be null.");
        return sendEvent(new RealtimeResponseCancelEvent().setResponseId(responseId));
    }

    /**
     * Sends a function-call result and requests the next response.
     *
     * @param callId the function call identifier.
     * @param output the serialized function result.
     * @return a completion signal emitted after both events are written.
     */
    public Mono<Void> sendFunctionCallOutput(String callId, String output) {
        RealtimeConversationItemFunctionCallOutput item
            = new RealtimeConversationItemFunctionCallOutput(callId, output);
        return createConversationItem(item).then(createResponse());
    }

    /**
     * Closes the WebSocket session.
     *
     * @return a completion signal for closing the session.
     */
    @Override
    public Mono<Void> closeAsync() {
        return transport.closeAsync();
    }

    /**
     * Closes the session with an application-selected WebSocket close frame.
     * The first requested close frame wins when close is called more than once.
     * @param code a valid WebSocket close code.
     * @param reason non-null reason of at most 123 UTF-8 bytes.
     * @return a completion signal, or an error if the code or reason is invalid.
     */
    public Mono<Void> closeAsync(int code, String reason) {
        return transport.closeAsync(code, reason);
    }

    /**
     * Closes the WebSocket session synchronously.
     */
    @Override
    public void close() {
        transport.close();
    }
}
