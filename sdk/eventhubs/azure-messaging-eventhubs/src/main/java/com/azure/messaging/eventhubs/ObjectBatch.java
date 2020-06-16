package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.SchemaRegistryDataSerializer;

public final class ObjectBatch<T> extends Batch {
    private final ClientLogger logger = new ClientLogger(ObjectBatch.class);
    private final Object lock = new Object();
    private final Class<T> batchType;
    private final SchemaRegistryDataSerializer registrySerializer;

    /**
     *
     * @param maxMessageSize
     * @param partitionId
     * @param partitionKey
     * @param batchType
     * @param contextProvider
     * @param tracerProvider
     * @param registrySerializer
     * @param entityPath
     * @param hostname
     */
    ObjectBatch(int maxMessageSize, String partitionId, String partitionKey, Class<T> batchType,
                    ErrorContextProvider contextProvider, TracerProvider tracerProvider,
                    SchemaRegistryDataSerializer registrySerializer, String entityPath, String hostname) {
        super(maxMessageSize, partitionId, partitionKey, contextProvider, tracerProvider, entityPath, hostname);
        this.batchType = batchType;
        this.registrySerializer = registrySerializer;
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

        EventData eventData = new EventData(registrySerializer.serialize(object).block());
        EventData event = tracerProvider.isEnabled() ? traceMessageSpan(eventData) : eventData;

        return tryAdd(event);
    }
}
