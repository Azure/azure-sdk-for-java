// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.ai.agents.models.RealtimeClientEvent;
import com.azure.ai.agents.models.RealtimeConversationItem;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.VoiceAgentResponseCreateParams;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/**
 * A synchronous bidirectional realtime session connected to a Foundry voice agent.
 */
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class VoiceAgentWebSocketSessionClient implements AutoCloseable {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);
    private final VoiceAgentWebSocketSessionAsyncClient asyncClient;

    VoiceAgentWebSocketSessionClient(VoiceAgentWebSocketSessionAsyncClient asyncClient) {
        this.asyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
    }

    /**
     * Gets the WebSocket endpoint used by this session.
     *
     * @return the WebSocket endpoint.
     */
    public URI getEndpoint() {
        return asyncClient.getEndpoint();
    }

    /**
     * Determines whether the session is open.
     *
     * @return {@code true} when the session is open.
     */
    public boolean isOpen() {
        return asyncClient.isOpen();
    }

    /**
     * Gets the peer close code.
     *
     * @return the close code, or {@code null}.
     */
    public Integer getCloseCode() {
        return asyncClient.getCloseCode();
    }

    /**
     * Gets the peer close reason.
     *
     * @return the close reason, or {@code null}.
     */
    public String getCloseReason() {
        return asyncClient.getCloseReason();
    }

    /**
     * Receives typed server events in wire order. The returned stream may be iterated once.
     *
     * @return the server event stream.
     */
    public IterableStream<RealtimeServerEvent> receiveEvents() {
        return new IterableStream<>(asyncClient.receiveEvents());
    }

    /**
     * Sends a typed realtime client event.
     *
     * @param event the event to send.
     */
    public void sendEvent(RealtimeClientEvent event) {
        asyncClient.sendEvent(event).block(OPERATION_TIMEOUT);
    }

    /**
     * Adds a conversation item.
     *
     * @param item the item to add.
     */
    public void createConversationItem(RealtimeConversationItem item) {
        asyncClient.createConversationItem(item).block(OPERATION_TIMEOUT);
    }

    /**
     * Adds a conversation item after another item.
     *
     * @param item the item to add.
     * @param previousItemId the preceding item identifier.
     */
    public void createConversationItem(RealtimeConversationItem item, String previousItemId) {
        asyncClient.createConversationItem(item, previousItemId).block(OPERATION_TIMEOUT);
    }

    /**
     * Adds a user text message.
     *
     * @param text the user message.
     */
    public void sendText(String text) {
        asyncClient.sendText(text).block(OPERATION_TIMEOUT);
    }

    /**
     * Appends audio to the input buffer.
     *
     * @param audio the audio bytes.
     */
    public void appendInputAudio(BinaryData audio) {
        asyncClient.appendInputAudio(audio).block(OPERATION_TIMEOUT);
    }

    /**
     * Clears the input audio buffer.
     */
    public void clearInputAudio() {
        asyncClient.clearInputAudio().block(OPERATION_TIMEOUT);
    }

    /**
     * Commits the input audio buffer.
     */
    public void commitInputAudio() {
        asyncClient.commitInputAudio().block(OPERATION_TIMEOUT);
    }

    /**
     * Requests a response using the voice agent's configuration.
     */
    public void createResponse() {
        asyncClient.createResponse().block(OPERATION_TIMEOUT);
    }

    /**
     * Requests a response with per-response options.
     *
     * @param responseOptions response options.
     */
    public void createResponse(VoiceAgentResponseCreateParams responseOptions) {
        asyncClient.createResponse(responseOptions).block(OPERATION_TIMEOUT);
    }

    /**
     * Cancels the active response.
     */
    public void cancelResponse() {
        asyncClient.cancelResponse().block(OPERATION_TIMEOUT);
    }

    /**
     * Cancels a specific response.
     *
     * @param responseId the response identifier.
     */
    public void cancelResponse(String responseId) {
        asyncClient.cancelResponse(responseId).block(OPERATION_TIMEOUT);
    }

    /**
     * Sends a function-call result and requests the next response.
     *
     * @param callId the function call identifier.
     * @param output the serialized function output.
     */
    public void sendFunctionCallOutput(String callId, String output) {
        asyncClient.sendFunctionCallOutput(callId, output).block(OPERATION_TIMEOUT);
    }

    /**
     * Closes the session.
     */
    @Override
    public void close() {
        asyncClient.close();
    }
}
