// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A class for aggregating Java objects into a single, size-limited, batch. Objects are serialized into EventData
 * objects and are added to the batch.  It is treated as a single message when sent to the Azure Event Hubs service.
 *
 * @param <T> type of objects in the batch.  Multi-type batches are not permitted.
 */
public final class ObjectBatch<T> extends EventDataBatchBase {
    private final ClientLogger logger = new ClientLogger(ObjectBatch.class);
    private final Class<T> batchType;
    private final ObjectSerializer serializer;

    ObjectBatch(int maxMessageSize, String partitionId, String partitionKey, Class<T> batchType,
                    ErrorContextProvider contextProvider, TracerProvider tracerProvider,
                    ObjectSerializer serializer, String entityPath, String hostname,
                    boolean isPublishingSequenceNumberRequired) {
        super(maxMessageSize, partitionId, partitionKey, contextProvider, tracerProvider, entityPath, hostname,
            isPublishingSequenceNumberRequired);
        this.batchType = Objects.requireNonNull(batchType, "'batchType' cannot be null.");
        this.serializer = Objects.requireNonNull(serializer, "'serializer' cannot be null.");
    }

    ObjectBatch(int maxMessageSize, String partitionId, String partitionKey, Class<T> batchType,
                ErrorContextProvider contextProvider, TracerProvider tracerProvider,
                ObjectSerializer serializer, String entityPath, String hostname) {
        this(maxMessageSize, partitionId, partitionKey, batchType, contextProvider, tracerProvider,
            serializer, entityPath, hostname, false);
    }

    /**
     * Tries to synchronously serialize an object into an EventData payload and add the EventData to the batch.
     *
     * @param object The object to add to this batch.
     * @return {@code true} is the object is successfully added to the batch.
     * @throws IllegalArgumentException if object is {@code null}.
     * @throws AmqpException if serialized object as {@link EventData} is larger than the maximum size
     * of the {@link ObjectBatch}.
     */
    public boolean tryAdd(T object) {
        return tryAdd(object, null);
    }


    /**
     * Tries to synchronously serialize an object into an EventData payload and add the EventData to the batch.
     *
     * @param object The object to add to this batch.
     * @param eventProperties Properties to add to the event associated with this object.
     * @return {@code true} is the object is successfully added to the batch.
     * @throws IllegalArgumentException if object is {@code null}.
     * @throws AmqpException if serialized object as {@link EventData} is larger than the maximum size
     * of the {@link ObjectBatch}.
     */
    public boolean tryAdd(T object, Map<String, Object> eventProperties) {
        Boolean success = tryAddAsync(object, eventProperties).block();
        return success != null && success;
    }

    /**
     * Tries to asynchronously serialize an object into an EventData payload and add the EventData to the batch.
     *
     * @param object The object to add to the batch.
     * @return {@code true} is the object is successfully added to the batch.
     * @throws IllegalArgumentException if object is {@code null}.
     * @throws AmqpException if serialized object as {@link EventData} is larger than the maximum size
     *      of the {@link EventDataBatch}.
     */
    public Mono<Boolean> tryAddAsync(T object) {
        return tryAddAsync(object, null);
    }


    /**
     * Tries to asynchronously serialize an object into an EventData payload and add the EventData to the batch.
     *
     * @param object The object to add to this batch.
     * @param eventProperties Properties to add to the event associated with this object.
     * @return {@code true} is the object is successfully added to the batch.
     * @throws IllegalArgumentException if object is {@code null}.
     * @throws AmqpException if serialized object as {@link EventData} is larger than the maximum size
     * of the {@link ObjectBatch}.
     */
    public Mono<Boolean> tryAddAsync(T object, Map<String, Object> eventProperties) {
        if (object == null) {
            return monoError(logger, new IllegalArgumentException("object cannot be null"));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        return serializer.serializeAsync(outputStream, object)
            .then(Mono.defer(() -> {
                EventData eventData = new EventData(outputStream.toByteArray());
                if (eventProperties != null) {
                    eventData.getProperties().putAll(eventProperties);
                }
                return Mono.just(tryAdd(eventData));
            }));
    }

}
