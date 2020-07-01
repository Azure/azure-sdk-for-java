package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.experimental.serializer.ObjectSerializer;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public final class ObjectBatch<T> extends Batch {
    private final ClientLogger logger = new ClientLogger(ObjectBatch.class);
    private final Object lock = new Object();
    private final Class<T> batchType;
    private final ObjectSerializer serializer;

    /**
     *
     * @param maxMessageSize
     * @param partitionId
     * @param partitionKey
     * @param batchType
     * @param contextProvider
     * @param tracerProvider
     * @param serializer
     * @param entityPath
     * @param hostname
     */
    ObjectBatch(int maxMessageSize, String partitionId, String partitionKey, Class<T> batchType,
                    ErrorContextProvider contextProvider, TracerProvider tracerProvider,
                ObjectSerializer serializer, String entityPath, String hostname) {
        super(maxMessageSize, partitionId, partitionKey, contextProvider, tracerProvider, entityPath, hostname);
        this.batchType = batchType;
        this.serializer = serializer;
    }

    /**
     * Tries to add an object to the batch.
     *
     * @param object The object to add to the batch.
     * @return {@code true} if the object could be added to the batch; {@code false} if the serialized object
     *      was too large to fit in the batch.
     * @throws IllegalArgumentException if object is {@code null}.
     * @throws AmqpException if serialized object as {@link EventData} is larger than the maximum size
     *      of the {@link EventDataBatch}.
     */
    public boolean tryAdd(final T object) {
        if (object == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("object cannot be null"));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(outputStream, object).block();
        EventData eventData = new EventData(outputStream.toByteArray());
        EventData event = tracerProvider.isEnabled() ? traceMessageSpan(eventData) : eventData;

        return tryAdd(event);
    }

    <S extends OutputStream> Mono<S> serialize(S stream, Object value) {
        return null;
    }
}
