// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.experimental.serializer.ObjectSerializer;
import com.azure.data.schemaregistry.AbstractDataSerializer;
import com.azure.data.schemaregistry.SerializationException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;

/**
 * Asynchronous registry-based serializer implementation.
 */
public class SchemaRegistryAvroAsyncSerializer extends AbstractDataSerializer implements ObjectSerializer {
    private static final int DEFAULT_THREAD_POOL_SIZE = 8;

    private final SchemaRegistryAvroSerializer serializer;
    private final SchemaRegistryAvroDeserializer deserializer;
    private final Scheduler scheduler;

    /**
     * @param serializer synchronous Avro serializer implementation
     * @param deserializer
     */
    SchemaRegistryAvroAsyncSerializer(SchemaRegistryAvroSerializer serializer, SchemaRegistryAvroDeserializer deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.scheduler = Schedulers.fromExecutor(Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE));
    }

    /**
     * Async wrapper around sync serialization operation
     *
     * @param o object to be serialized to bytes
     * @return Avro byte representation of object
     * @throws SerializationException upon serialization operation failure
     */
    public Mono<byte[]> serialize(Object o) {
        if (o == null) {
            return Mono.empty();
        }

        ByteArrayOutputStream s = new ByteArrayOutputStream();

        return Mono.fromCallable(() -> {
                this.serializeImpl(s, o);
                return s.toByteArray();
            });
    }

    public <S extends OutputStream> Mono<S> serialize(S s, Object o) {
        if (o == null) {
            return Mono.empty();
        }

        return this.serializeImpl(s, o);
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

    @Override
    public <T> Mono<T> deserialize(InputStream stream, Class<T> clazz) {
        try {
            return this.deserialize(stream.readAllBytes()).map(o -> {
                if (clazz.isInstance(o)) {
                    return clazz.cast(o);
                }
                throw new RuntimeException("Deserialized object not of class T!");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

