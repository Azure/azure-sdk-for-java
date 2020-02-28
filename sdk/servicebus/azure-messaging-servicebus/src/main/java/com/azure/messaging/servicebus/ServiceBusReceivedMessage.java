// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.Context;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

/***
 * This class represent the received Message.
 */
public final class ServiceBusReceivedMessage extends ServiceBusMessage {

    private UUID lockToken;
    private long sequenceNumber;

    ServiceBusReceivedMessage(byte[] body) {
        super(body);
    }

    ServiceBusReceivedMessage(byte[] body, String sessionId) {
        super(body, sessionId);
    }

    /**
     * Creates an event containing the {@code body}.
     *
     * @param body The data to set for this event.
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    ServiceBusReceivedMessage(ByteBuffer body) {
        super(Objects.requireNonNull(body, "'body' cannot be null.").array());
    }
    ServiceBusReceivedMessage(ByteBuffer body, String sessionId) {
        this(Objects.requireNonNull(body, "'body' cannot be null.").array());

    }

    /**
     * Creates an event by encoding the {@code body} using UTF-8 charset.
     *
     * @param body The string that will be UTF-8 encoded to create an event.
     * @throws NullPointerException if {@code body} is {@code null}.
     */
    ServiceBusReceivedMessage(String body) {
        this(Objects.requireNonNull(body, "'body' cannot be null.").getBytes(UTF_8));
    }
    ServiceBusReceivedMessage(String body, String sessionId) {
        super(body, sessionId);

    }

    /**
     * Creates an event with the given {@code body}, system properties and context.
     *
     * @param body The data to set for this event.
     * @param systemProperties System properties set by message broker for this event.
     * @param context A specified key-value pair of type {@link Context}.
     * @throws NullPointerException if {@code body}, {@code systemProperties}, or {@code context} is {@code null}.
     */
    ServiceBusReceivedMessage(byte[] body, SystemProperties systemProperties, Context context) {
        super(body, systemProperties, context);
    }

    ServiceBusReceivedMessage(byte[] body, Map<String, Object> systemProperties, Context context, String sessionId) {
        super(body, systemProperties, context, sessionId);
    }

    /**
     *
     * @return The delivery count.
     */
    public long getDeliveryCount() {
        return 0;
    }

    /**
     *
     * @return The lockToken for this message.
     */
    public UUID getLockToken() {
        return this.lockToken;
    }

    /**
     * Gets the instant, in UTC, of when the event was enqueued in the Event Hub partition. This is only present on a
     * <b>received</b> {@link ServiceBusMessage}.
     *
     * @return The instant, in UTC, this was enqueued in the Event Hub partition.
     *  {@code null} if the {@link ServiceBusMessage} was not received from Event Hubs service.
     */
    public Instant getEnqueuedTime() {
        return null;
    }


    /**
     * Gets the sequence number assigned to the event when it was enqueued in the associated Event Hub partition. This
     * is unique for every message received in the Event Hub partition. This is only present on a <b>received</b>
     * {@link ServiceBusMessage}.
     *
     * @return The sequence number for this event. {@code null} if the {@link ServiceBusMessage}
     * was not received from Event Hubs service.
     */
    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    /**
     *
     * @return The time when message expire.
     */
    public Instant getExpiresAt() {
        return null;
    }

    /**
     *
     * @return The time until the message lock is acquired.
     */
    public Instant getLockedUntil() {
        return null;
    }

    /**
     *
     * @param lockToken to be set
     * @return The updated {@link ServiceBusReceivedMessage}.

     */
    public ServiceBusReceivedMessage setLockToken(UUID lockToken) {
        this.lockToken = lockToken;
        return this;
    }

    /**
     *
     * @param sequenceNumber to be set
     * @return The updated {@link ServiceBusReceivedMessage}.

     */
    public ServiceBusReceivedMessage setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

}
