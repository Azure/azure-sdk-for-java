// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.data.schemaregistry.SerializationException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

/**
 * Asynchronous registry-based deserializer implementation.
 */
public class SchemaRegistryAvroAsyncDeserializer {
    private static final int DEFAULT_THREAD_POOL_SIZE = 8;

    private final SchemaRegistryAvroDeserializer deserializer;
    private final Scheduler scheduler;

    /**
     * Instantiates instance of async deserializer.
     *
     * @param deserializer synchronous internal deserializer implementation
     */
    SchemaRegistryAvroAsyncDeserializer(SchemaRegistryAvroDeserializer deserializer) {
        this.deserializer = deserializer;
        this.scheduler = Schedulers.fromExecutor(Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE));
    }

    /**
     * Async wrapper around synchronous deserialization method
     * @param data bytes containing schema ID and encoded byte representation of object
     * @return Mono wrapper around deserialized object
     * @throws SerializationException if deserialization operation fails
     */
    public Mono<Object> deserialize(byte[] data) throws SerializationException {
        return Mono
            .fromCallable(() -> this.deserializer.deserialize(data))
            .subscribeOn(scheduler);
    }
}
