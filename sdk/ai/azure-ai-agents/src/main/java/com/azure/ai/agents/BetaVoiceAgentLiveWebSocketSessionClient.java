// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.OpenAIJsonHelper;
import com.azure.ai.agents.implementation.realtime.WebSocketSessionTransport;
import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.ServerEvent;
import java.time.Duration;
import java.util.Objects;

/**
 * A synchronous bidirectional native GPT Live session.
 * <p>
 * Events can be sent and received using {@link ClientEvent} and {@link ServerEvent}, or as complete JSON objects.
 * Raw JSON preserves unknown fields, nested {@code response.event} payloads and Foundry {@code conversation_id} values.
 * Typed and raw receivers share one event iterator. Close sessions when they are no longer needed.
 */
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaVoiceAgentLiveWebSocketSessionClient implements AutoCloseable {
    private final WebSocketSessionTransport<ServerEvent> transport;

    BetaVoiceAgentLiveWebSocketSessionClient(WebSocketSessionTransport<ServerEvent> transport) {
        this.transport = transport;
    }

    /**
     * Gets the WebSocket endpoint. It may contain structured inputs; do not log sensitive query values.
     * @return the endpoint.
     */
    public String getEndpoint() {
        return transport.getEndpoint();
    }

    /**
     * Determines whether the transport is open, not whether {@code session.started} has been received.
     * @return whether the WebSocket is open.
     */
    public boolean isOpen() {
        return transport.isOpen();
    }

    /**
     * Gets the peer close code.
     * @return the code, or null if no close frame has been received.
     */
    public Integer getCloseCode() {
        return transport.getCloseCode();
    }

    /**
     * Gets the peer close reason.
     * @return the reason, or null if no close frame has been received.
     */
    public String getCloseReason() {
        return transport.getCloseReason();
    }

    /**
     * Receives native Live server events in wire order, including unknown events.
     * Service {@code error} events are delivered as JSON; transport and malformed JSON errors terminate iteration.
     * @return the single-consumer stream of complete JSON events.
     * @throws IllegalStateException if the receive stream was already claimed.
     */
    public IterableStream<BinaryData> receiveRawEvents() {
        return receiveRawEvents(null);
    }

    /**
     * Receives typed native Live server events. Typed and raw receivers share the single-consumer limit.
     * @return the typed server event stream.
     */
    public IterableStream<ServerEvent> receiveEvents() {
        return receiveEvents(null);
    }

    /**
     * Receives typed native Live events with a timeout per wait. Timeouts leave the session open.
    * Use {@link #receiveRawEvents(Duration)} when the original JSON representation is needed.
     * @param timeout a positive timeout, or null to wait indefinitely.
     * @return the single-consumer typed event stream.
     * @throws IllegalArgumentException if the timeout is zero or negative.
     * @throws IllegalStateException if the receive stream was already claimed, or a wait times out.
     */
    public IterableStream<ServerEvent> receiveEvents(Duration timeout) {
        return transport.receiveEvents(timeout);
    }

    /**
     * Sends a typed native Live command. Foundry supplies {@code session.start}; clients must not send it.
     * @param event the command to send.
     * @throws IllegalStateException if the session cannot accept the event.
     */
    public void sendEvent(ClientEvent event) {
        Objects.requireNonNull(event, "'event' cannot be null.");
        sendEvent(OpenAIJsonHelper.toBinaryData(event));
    }

    /**
     * Receives native Live events with a timeout per wait. Timeouts leave the session open so iteration can resume.
     * @param timeout a positive timeout, or null to wait indefinitely.
     * @return the single-consumer event stream.
     * @throws IllegalArgumentException if the timeout is zero or negative.
     * @throws IllegalStateException if the receive stream was already claimed, or a wait times out.
     */
    public IterableStream<BinaryData> receiveRawEvents(Duration timeout) {
        return transport.receiveRawEvents(timeout);
    }

    /**
     * Sends a native Live JSON event. Foundry supplies {@code session.start}; clients must not send it.
     * The service validates native command semantics, including delegation restrictions.
     * @param event the complete JSON object.
     * @throws IllegalArgumentException if the event is not a JSON object.
     * @throws IllegalStateException if the session cannot accept the event.
     */
    public void sendEvent(BinaryData event) {
        transport.sendEvent(event);
    }

    /** Closes the WebSocket normally. */
    @Override
    public void close() {
        transport.close();
    }

    /**
     * Closes the WebSocket with the specified close frame.
     * @param code a valid WebSocket close code.
     * @param reason a non-null reason of at most 123 UTF-8 bytes.
     * @throws IllegalArgumentException if the code or reason is invalid.
     */
    public void close(int code, String reason) {
        transport.close(code, reason);
    }
}
