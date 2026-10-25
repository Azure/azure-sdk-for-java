// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketUtils;
import com.azure.ai.agents.implementation.realtime.WebSocketSessionTransport;
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
import com.azure.core.util.BinaryData;
import com.openai.models.realtime.RealtimeConversationItemUserMessage.Content;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;

/**
 * A synchronous bidirectional realtime session connected to a Foundry voice agent.
 */
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaVoiceAgentWebSocketSessionClient implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(BetaVoiceAgentWebSocketSessionClient.class);
    private final WebSocketSessionTransport<RealtimeServerEvent> transport;

    private BetaVoiceAgentWebSocketSessionClient(WebSocketSessionTransport<RealtimeServerEvent> transport) {
        this.transport = transport;
    }

    static BetaVoiceAgentWebSocketSessionClient connect(VoiceAgentWebSocketClientConfiguration configuration,
        String agentName, VoiceAgentWebSocketConnectionOptions options) {
        return new BetaVoiceAgentWebSocketSessionClient(WebSocketSessionTransport.connect(configuration,
            VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, agentName, options), options,
            VoiceAgentWebSocketUtils.SUBPROTOCOL,
            token -> VoiceAgentWebSocketUtils.buildHeaders(configuration, options, token),
            VoiceAgentWebSocketUtils::deserializeEvent));
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
     * Determines whether the session is open.
     *
     * @return {@code true} when the session is open.
     */
    public boolean isOpen() {
        return transport.isOpen();
    }

    /**
     * Gets the peer close code.
     *
     * @return the close code, or {@code null}.
     */
    public Integer getCloseCode() {
        return transport.getCloseCode();
    }

    /**
     * Gets the peer close reason.
     *
     * @return the close reason, or {@code null}.
     */
    public String getCloseReason() {
        return transport.getCloseReason();
    }

    /**
     * Receives typed server events in wire order. The returned stream may be iterated once.
     *
     * @throws IllegalStateException if the event stream has already been claimed.
     * @return the server event stream.
     */
    public IterableStream<RealtimeServerEvent> receiveEvents() {
        return receiveEvents(null);
    }

    /**
     * Receives events with a timeout for each wait. A timeout leaves the session open and the iterator can be retried.
     * @param timeout positive per-event timeout, or null to wait indefinitely.
     * @return the single-consumer event stream.
     * @throws IllegalArgumentException if timeout is zero or negative.
     * @throws IllegalStateException if another receiver exists or a wait times out (with a TimeoutException cause).
     */
    public IterableStream<RealtimeServerEvent> receiveEvents(Duration timeout) {
        return transport.receiveEvents(timeout);
    }

    /**
     * Sends a typed realtime client event.
     *
     * @param event the event to send.
    * @throws IllegalArgumentException if the event cannot be serialized.
    * @throws IllegalStateException if the session is closed or cannot accept another event.
     */
    public void sendEvent(RealtimeClientEvent event) {
        Objects.requireNonNull(event, "'event' cannot be null.");
        try {
            transport.sendEvent(BinaryData.fromString(event.toJsonString()));
        } catch (IOException error) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Failed to serialize the realtime client event.", error));
        }
    }

    /**
     * Sends a JSON object, including event types and fields unknown to this SDK.
     * @param event the complete JSON event.
     * @throws IllegalArgumentException if the event is not a JSON object.
     * @throws IllegalStateException if the session cannot accept the event.
     */
    public void sendEvent(BinaryData event) {
        transport.sendEvent(event);
    }

    /**
     * Adds a conversation item.
     *
     * @param item the item to add.
     */
    public void createConversationItem(RealtimeConversationItem item) {
        createConversationItem(item, null);
    }

    /**
     * Adds a conversation item after another item.
     *
     * @param item the item to add.
     * @param previousItemId the preceding item identifier.
     */
    public void createConversationItem(RealtimeConversationItem item, String previousItemId) {
        Objects.requireNonNull(item, "'item' cannot be null.");
        sendEvent(new RealtimeConversationItemCreateEvent(item).setPreviousItemId(previousItemId));
    }

    /**
     * Adds a user text message.
     *
     * @param text the user message.
     */
    public void sendText(String text) {
        Objects.requireNonNull(text, "'text' cannot be null.");
        Content content = Content.builder().type(Content.Type.INPUT_TEXT).text(text).build();
        createConversationItem(new RealtimeConversationItemUserMessage(Collections.singletonList(content)));
    }

    /**
     * Appends audio to the input buffer.
     *
     * @param audio the audio bytes.
     */
    public void appendInputAudio(BinaryData audio) {
        Objects.requireNonNull(audio, "'audio' cannot be null.");
        sendEvent(new RealtimeInputAudioBufferAppendEvent(Base64.getEncoder().encodeToString(audio.toBytes())));
    }

    /** Clears the input audio buffer. */
    public void clearInputAudio() {
        sendEvent(new RealtimeInputAudioBufferClearEvent());
    }

    /** Commits the input audio buffer. */
    public void commitInputAudio() {
        sendEvent(new RealtimeInputAudioBufferCommitEvent());
    }

    /** Requests a response using the voice agent's configuration. */
    public void createResponse() {
        sendEvent(new RealtimeResponseCreateEvent());
    }

    /**
     * Requests a response with per-response options.
     *
     * @param responseOptions response options.
     */
    public void createResponse(VoiceAgentResponseCreateOptions responseOptions) {
        Objects.requireNonNull(responseOptions, "'responseOptions' cannot be null.");
        sendEvent(new RealtimeResponseCreateEvent().setResponse(responseOptions));
    }

    /** Cancels the active response. */
    public void cancelResponse() {
        sendEvent(new RealtimeResponseCancelEvent());
    }

    /**
     * Cancels a specific response.
     *
     * @param responseId the response identifier.
     */
    public void cancelResponse(String responseId) {
        Objects.requireNonNull(responseId, "'responseId' cannot be null.");
        sendEvent(new RealtimeResponseCancelEvent().setResponseId(responseId));
    }

    /**
     * Sends a function-call result and requests the next response.
     *
     * @param callId the function call identifier.
     * @param output the serialized function output.
     */
    public void sendFunctionCallOutput(String callId, String output) {
        createConversationItem(new RealtimeConversationItemFunctionCallOutput(callId, output));
        createResponse();
    }

    /** Closes the session. */
    @Override
    public void close() {
        transport.close();
    }

    /**
     * Closes the session with an application-selected WebSocket close frame.
     * @param code a valid WebSocket close code.
     * @param reason non-null reason of at most 123 UTF-8 bytes.
     * @throws IllegalArgumentException if the code or reason is invalid.
     */
    public void close(int code, String reason) {
        transport.close(code, reason);
    }
}
