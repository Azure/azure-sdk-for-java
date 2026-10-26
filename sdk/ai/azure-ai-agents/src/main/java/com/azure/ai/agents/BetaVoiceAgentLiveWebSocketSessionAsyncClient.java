// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.OpenAIJsonHelper;
import com.azure.ai.agents.implementation.realtime.WebSocketSessionAsyncTransport;
import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.BinaryData;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.ServerEvent;
import java.util.Objects;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * An asynchronous bidirectional native GPT Live session.
 * <p>
 * Live events can be sent and received using {@link ClientEvent} and {@link ServerEvent}, or as complete JSON objects.
 * Raw JSON preserves unknown fields, nested {@code response.event} payloads and Foundry {@code conversation_id} values.
 * Typed and raw receivers share one event subscriber. Close sessions when they are no longer needed.
 */
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaVoiceAgentLiveWebSocketSessionAsyncClient implements AsyncCloseable, AutoCloseable {
    private final WebSocketSessionAsyncTransport<ServerEvent> transport;

    BetaVoiceAgentLiveWebSocketSessionAsyncClient(WebSocketSessionAsyncTransport<ServerEvent> transport) {
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
     * Service {@code error} events are delivered as JSON; transport and malformed JSON errors terminate the stream.
     * @return the single-subscriber stream of complete JSON events.
     */
    public Flux<BinaryData> receiveRawEvents() {
        return transport.receiveRawEvents();
    }

    /**
     * Receives typed native Live server events. Typed and raw receivers share the single-subscriber limit.
    * Use {@link #receiveRawEvents()} when the original JSON representation is needed.
     * @return the typed server event stream.
     */
    public Flux<ServerEvent> receiveEvents() {
        return transport.receiveEvents();
    }

    /**
     * Sends a typed native Live command. Foundry supplies {@code session.start}; clients must not send it.
     * @param event the command to send.
     * @return completion after the frame is written.
     */
    public Mono<Void> sendEvent(ClientEvent event) {
        Objects.requireNonNull(event, "'event' cannot be null.");
        return Mono.defer(() -> sendEvent(OpenAIJsonHelper.toBinaryData(event)));
    }

    /**
     * Sends a native Live JSON event. Foundry supplies {@code session.start}; clients must not send it.
     * The service validates native command semantics, including delegation restrictions.
     * @param event the complete JSON object.
     * @return completion after the frame is written, or an error for invalid JSON or a closed session.
     */
    public Mono<Void> sendEvent(BinaryData event) {
        return transport.sendEvent(event);
    }

    /**
     * Closes the WebSocket normally.
     * @return completion when the session is closed.
     */
    @Override
    public Mono<Void> closeAsync() {
        return transport.closeAsync();
    }

    /**
     * Closes the WebSocket with the specified close frame. The first asynchronous close request wins.
     * @param code a valid WebSocket close code.
     * @param reason a non-null reason of at most 123 UTF-8 bytes.
     * @return completion when the session is closed.
     * @throws IllegalArgumentException if the code or reason is invalid.
     */
    public Mono<Void> closeAsync(int code, String reason) {
        return transport.closeAsync(code, reason);
    }

    /** Closes the session, blocking until closure or the configured close timeout. */
    @Override
    public void close() {
        transport.close();
    }
}
