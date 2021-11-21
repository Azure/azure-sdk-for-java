// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;

import java.nio.BufferOverflowException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.messaging.servicebus.implementation.MessageUtils.traceMessageSpan;

/**
 * A class for aggregating {@link ServiceBusMessage messages} into a single, size-limited, batch. It is treated as a
 * single AMQP message when sent to the Azure Service Bus service.
 */
public final class ServiceBusMessageBatch {
    private final ClientLogger logger = new ClientLogger(ServiceBusMessageBatch.class);
    private final Object lock = new Object();
    private final int maxMessageSize;
    private final ErrorContextProvider contextProvider;
    private final MessageSerializer serializer;
    private final List<ServiceBusMessage> serviceBusMessageList;
    private final byte[] eventBytes;
    private final AtomicInteger sizeInBytes;
    private final TracerProvider tracerProvider;
    private final String entityPath;
    private final String hostname;

    ServiceBusMessageBatch(int maxMessageSize, ErrorContextProvider contextProvider, TracerProvider tracerProvider,
        MessageSerializer serializer, String entityPath, String hostname) {
        this.maxMessageSize = maxMessageSize;
        this.contextProvider = contextProvider;
        this.serializer = serializer;
        this.serviceBusMessageList = Collections.synchronizedList(new LinkedList<>());
        this.sizeInBytes = new AtomicInteger((maxMessageSize / 65536) * 1024); // reserve 1KB for every 64KB
        this.eventBytes = new byte[maxMessageSize];
        this.tracerProvider = tracerProvider;
        this.entityPath = entityPath;
        this.hostname = hostname;
    }

    /**
     * Gets the number of {@link ServiceBusMessage messages} in the batch.
     *
     * @return The number of {@link ServiceBusMessage messages} in the batch.
     */
    public int getCount() {
        return serviceBusMessageList.size();
    }

    /**
     * Gets the maximum size, in bytes, of the {@link ServiceBusMessageBatch batch}.
     *
     * @return The maximum size, in bytes, of the {@link ServiceBusMessageBatch batch}.
     */
    public int getMaxSizeInBytes() {
        return maxMessageSize;
    }

    /**
     * Gets the size of the {@link ServiceBusMessageBatch batch} in bytes.
     *
     * @return The size of the {@link ServiceBusMessageBatch batch} in bytes.
     */
    public int getSizeInBytes() {
        return this.sizeInBytes.get();
    }

    /**
     * Tries to add an {@link ServiceBusMessage message} to the batch.
     *
     * @param serviceBusMessage The {@link ServiceBusMessage} to add to the batch.
     *
     * @return {@code true} if the message could be added to the batch; {@code false} if the event was too large to fit
     *     in the batch.
     *
     * @throws NullPointerException if {@code message} is {@code null}.
     * @throws AmqpException if {@code message} is larger than the maximum size of the {@link
     *     ServiceBusMessageBatch}.
     */
    public boolean tryAddMessage(final ServiceBusMessage serviceBusMessage) {
        if (serviceBusMessage == null) {
            throw logger.logExceptionAsWarning(new NullPointerException("'serviceBusMessage' cannot be null"));
        }
        ServiceBusMessage serviceBusMessageUpdated =
            tracerProvider.isEnabled()
                ? traceMessageSpan(serviceBusMessage, serviceBusMessage.getContext(), hostname, entityPath,
                tracerProvider)
                : serviceBusMessage;

        final AtomicInteger size = new AtomicInteger();
        try {
            size.set(getSize(serviceBusMessageUpdated, serviceBusMessageList.isEmpty()));
        } catch (BufferOverflowException exception) {
            final RuntimeException ex = new ServiceBusException(
                    new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
                        String.format(Locale.US, "Size of the payload exceeded maximum message size: %s kb",
                            maxMessageSize / 1024), contextProvider.getErrorContext()), ServiceBusErrorSource.SEND);

            throw logger.logExceptionAsWarning(ex);
        }

        if (this.sizeInBytes.addAndGet(size.get()) > this.maxMessageSize) {
            this.sizeInBytes.addAndGet(-1 * size.get());
            return false;
        }

        this.serviceBusMessageList.add(serviceBusMessageUpdated);
        return true;
    }

    /**
     * Gets the messages in the batch.
     *
     * @return The messages in the message batch.
     */
    List<ServiceBusMessage> getMessages() {
        return serviceBusMessageList;
    }

    private int getSize(final ServiceBusMessage serviceBusMessage, final boolean isFirst) {
        Objects.requireNonNull(serviceBusMessage, "'serviceBusMessage' cannot be null.");

        final org.apache.qpid.proton.message.Message amqpMessage = serializer.serialize(serviceBusMessage);
        int eventSize = amqpMessage.encode(this.eventBytes, 0, maxMessageSize); // actual encoded bytes size
        eventSize += 16; // data section overhead

        if (isFirst) {
            amqpMessage.setBody(null);
            amqpMessage.setApplicationProperties(null);
            amqpMessage.setProperties(null);
            amqpMessage.setDeliveryAnnotations(null);

            eventSize += amqpMessage.encode(this.eventBytes, 0, maxMessageSize);
        }

        return eventSize;
    }
}
