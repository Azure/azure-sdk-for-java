// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import reactor.core.publisher.Signal;

import java.nio.BufferOverflowException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_SERVICE_NAME;

/**
 * A class for aggregating {@link ServiceBusMessage messages} into a single, size-limited, batch. It is treated as a
 * single AMQP message when sent to the Azure Service Bus service.
 */
public final class ServiceBusMessageBatch {
    private static final String AZ_TRACING_NAMESPACE_VALUE = "Microsoft.ServiceBus";
    private final ClientLogger logger = new ClientLogger(ServiceBusMessageBatch.class);
    private final Object lock = new Object();
    private final int maxMessageSize;
    private final ErrorContextProvider contextProvider;
    private final MessageSerializer serializer;
    private final List<ServiceBusMessage> serviceBusMessageList;
    private final byte[] eventBytes;
    private int sizeInBytes;
    private final TracerProvider tracerProvider;
    private final String entityPath;
    private final String hostname;

    ServiceBusMessageBatch(int maxMessageSize, ErrorContextProvider contextProvider, TracerProvider tracerProvider,
        MessageSerializer serializer, String entityPath, String hostname) {
        this.maxMessageSize = maxMessageSize;
        this.contextProvider = contextProvider;
        this.serializer = serializer;
        this.serviceBusMessageList = new LinkedList<>();
        this.sizeInBytes = (maxMessageSize / 65536) * 1024; // reserve 1KB for every 64KB
        this.eventBytes = new byte[maxMessageSize];
        this.tracerProvider = tracerProvider;
        this.entityPath = entityPath;
        this.hostname = hostname;
    }

    /**
     * Gets the number of {@link ServiceBusMessage events} in the batch.
     *
     * @return The number of {@link ServiceBusMessage events} in the batch.
     */
    public int getCount() {
        return serviceBusMessageList.size();
    }

    /**
     * Gets the maximum size, in bytes, of the {@link ServiceBusMessageBatch}.
     *
     * @return The maximum size, in bytes, of the {@link ServiceBusMessageBatch}.
     */
    public int getMaxSizeInBytes() {
        return maxMessageSize;
    }

    /**
     * Gets the size of the {@link ServiceBusMessageBatch} in bytes.
     *
     * @return the size of the {@link ServiceBusMessageBatch} in bytes.
     */
    public int getSizeInBytes() {
        return this.sizeInBytes;
    }

    /**
     * Tries to add an {@link ServiceBusMessage message} to the batch.
     *
     * @param serviceBusMessage The {@link ServiceBusMessage} to add to the batch.
     *
     * @return {@code true} if the message could be added to the batch; {@code false} if the event was too large to fit
     *     in the batch.
     *
     * @throws IllegalArgumentException if {@code message} is {@code null}.
     * @throws AmqpException if {@code message} is larger than the maximum size of the {@link
     *     ServiceBusMessageBatch}.
     */
    public boolean tryAdd(final ServiceBusMessage serviceBusMessage) {
        if (serviceBusMessage == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("message cannot be null"));
        }
        ServiceBusMessage serviceBusMessageUpdated =
            tracerProvider.isEnabled() ? traceMessageSpan(serviceBusMessage) : serviceBusMessage;

        final int size;
        try {
            size = getSize(serviceBusMessageUpdated, serviceBusMessageList.isEmpty());
        } catch (BufferOverflowException exception) {
            throw logger.logExceptionAsWarning(new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
                String.format(Locale.US, "Size of the payload exceeded maximum message size: %s kb",
                    maxMessageSize / 1024),
                contextProvider.getErrorContext()));
        }

        synchronized (lock) {
            if (this.sizeInBytes + size > this.maxMessageSize) {
                return false;
            }

            this.sizeInBytes += size;
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

    /**
     * Method to start and end a "Azure.EventHubs.message" span and add the "DiagnosticId" as a property of the
     * message.
     *
     * @param serviceBusMessage The Message to add tracing span for.
     *
     * @return the updated Message data object.
     */
    private ServiceBusMessage traceMessageSpan(ServiceBusMessage serviceBusMessage) {
        Optional<Object> eventContextData = serviceBusMessage.getContext().getData(SPAN_CONTEXT_KEY);
        if (eventContextData.isPresent()) {
            // if message has context (in case of retries), don't start a message span or add a new context
            return serviceBusMessage;
        } else {
            // Starting the span makes the sampling decision (nothing is logged at this time)
            Context messageContext = serviceBusMessage.getContext()
                .addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE)
                .addData(ENTITY_PATH_KEY, entityPath)
                .addData(HOST_NAME_KEY, hostname);
            Context eventSpanContext = tracerProvider.startSpan(AZ_TRACING_SERVICE_NAME, messageContext,
                ProcessKind.MESSAGE);
            Optional<Object> eventDiagnosticIdOptional = eventSpanContext.getData(DIAGNOSTIC_ID_KEY);
            if (eventDiagnosticIdOptional.isPresent()) {
                serviceBusMessage.getApplicationProperties().put(DIAGNOSTIC_ID_KEY, eventDiagnosticIdOptional.get()
                    .toString());
                tracerProvider.endSpan(eventSpanContext, Signal.complete());
                serviceBusMessage.addContext(SPAN_CONTEXT_KEY, eventSpanContext);
            }
        }

        return serviceBusMessage;
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
