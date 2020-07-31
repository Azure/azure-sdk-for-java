// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.experimental.serializer.ObjectSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.SchemaRegistrySerializer;
import com.azure.data.schemaregistry.models.SerializationException;
import com.azure.data.schemaregistry.CachedSchemaRegistryAsyncClient;
import reactor.core.publisher.Mono;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

/**
 * Asynchronous registry-based serializer implementation.
 */
public class SchemaRegistryAvroAsyncSerializer extends SchemaRegistrySerializer implements ObjectSerializer {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryAvroAsyncSerializer.class);

    /**
     *
     * @param registryClient
     * @param codec
     * @param schemaGroup
     * @param autoRegisterSchemas
     */
    SchemaRegistryAvroAsyncSerializer(CachedSchemaRegistryAsyncClient registryClient, AvroCodec codec,
                                      String schemaGroup, Boolean autoRegisterSchemas) {
        super(registryClient, codec, Collections.singletonList(codec), autoRegisterSchemas, schemaGroup);
    }

    @Override
    public <S extends OutputStream> Mono<S> serialize(S s, Object o) {
        if (o == null) {
            return Mono.empty();
        }

        return super.serialize(s, o);
    }

    @Override
    public <T> Mono<T> deserialize(InputStream stream, Class<T> clazz) {
        return this.deserialize(stream)
            .map(o -> {
                if (clazz.isInstance(o)) {
                    return clazz.cast(o);
                }
                throw logger.logExceptionAsError(new SerializationException("Deserialized object not of class %s"));
            });
    }
}

