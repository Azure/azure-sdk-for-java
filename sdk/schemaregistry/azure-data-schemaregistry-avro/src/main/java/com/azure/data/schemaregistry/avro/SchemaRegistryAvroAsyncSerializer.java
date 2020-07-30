// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.experimental.serializer.ObjectSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.AbstractSchemaRegistrySerializer;
import com.azure.data.schemaregistry.SerializationException;
import com.azure.data.schemaregistry.client.CachedSchemaRegistryAsyncClient;
import reactor.core.publisher.Mono;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Asynchronous registry-based serializer implementation.
 */
public class SchemaRegistryAvroAsyncSerializer extends AbstractSchemaRegistrySerializer implements ObjectSerializer {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryAvroAsyncSerializer.class);

    /**
     *
     * @param registryClient
     * @param codec
     * @param schemaGroup
     * @param autoRegisterSchemas
     */
    SchemaRegistryAvroAsyncSerializer(CachedSchemaRegistryAsyncClient registryClient, AvroCodec codec,
                                      String schemaGroup, boolean autoRegisterSchemas) {
        super(registryClient, codec, Collections.singletonList(codec));

        // send configurations only
        this.autoRegisterSchemas = autoRegisterSchemas;
        this.schemaGroup = schemaGroup;
    }

    @Override
    public <S extends OutputStream> Mono<S> serialize(S s, Object o) {
        if (o == null) {
            return Mono.empty();
        }

        return this.serializeImpl(s, o);
    }

    @Override
    public <T> Mono<T> deserialize(InputStream stream, Class<T> clazz) {
        return this.deserializeImpl(stream)
            .map(o -> {
                if (clazz.isInstance(o)) {
                    return clazz.cast(o);
                }
                throw logger.logExceptionAsError(new SerializationException("Deserialized object not of class %s"));
            });
    }
}

