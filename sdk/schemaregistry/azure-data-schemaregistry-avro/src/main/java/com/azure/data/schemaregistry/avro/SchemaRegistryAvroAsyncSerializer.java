// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.data.schemaregistry.AbstractDataSerializer;
import com.azure.data.schemaregistry.SerializationException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

/**
 * Asynchronous registry-based serializer implementation.
 */
public class SchemaRegistryAvroAsyncSerializer extends AbstractDataSerializer {
    private static final int DEFAULT_THREAD_POOL_SIZE = 8;

    private final SchemaRegistryAvroSerializer serializer;
    private final Scheduler scheduler;

    /**
     * @param serializer synchronous Avro serializer implementation
     */
    SchemaRegistryAvroAsyncSerializer(SchemaRegistryAvroSerializer serializer) {
        this.serializer = serializer;
        this.scheduler = Schedulers.fromExecutor(Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE));
    }

    /**
     * Async wrapper around sync serialization operation
     *
     * @param object object to be serialized to bytes
     * @return Avro byte representation of object
     * @throws SerializationException upon serialization operation failure
     */
    public Mono<byte[]> serialize(Object object) throws SerializationException {
        if (object == null) {
            return Mono.empty();
        }

        return Mono
            .fromCallable(() -> this.serializer.serialize(object))
            .subscribeOn(scheduler);
    }
}

