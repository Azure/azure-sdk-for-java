// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketUtils;
import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import java.time.Duration;
import java.util.Iterator;

/**
 * A synchronous bidirectional native GPT Live session.
 * <p>
 * Events are complete JSON objects, not Realtime protocol models. Unknown fields, nested {@code response.event}
 * payloads and Foundry {@code conversation_id} values are preserved. Only one event iterator is supported.
 * Close sessions when they are no longer needed.
 */
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaVoiceAgentLiveWebSocketSessionClient implements AutoCloseable {
    private final BetaVoiceAgentWebSocketSessionClient transport;

    BetaVoiceAgentLiveWebSocketSessionClient(BetaVoiceAgentWebSocketSessionClient transport) {
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
    public IterableStream<BinaryData> receiveEvents() {
        return receiveEvents(null);
    }

    /**
     * Receives native Live events with a timeout per wait. Timeouts leave the session open so iteration can resume.
     * @param timeout a positive timeout, or null to wait indefinitely.
     * @return the single-consumer event stream.
     * @throws IllegalArgumentException if the timeout is zero or negative.
     * @throws IllegalStateException if the receive stream was already claimed, or a wait times out.
     */
    public IterableStream<BinaryData> receiveEvents(Duration timeout) {
        IterableStream<RealtimeServerEvent> events = transport.receiveEvents(timeout);
        return IterableStream.of(() -> new Iterator<BinaryData>() {
            private final Iterator<RealtimeServerEvent> iterator = events.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public BinaryData next() {
                return VoiceAgentWebSocketUtils.liveEventPayload(iterator.next());
            }
        });
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
