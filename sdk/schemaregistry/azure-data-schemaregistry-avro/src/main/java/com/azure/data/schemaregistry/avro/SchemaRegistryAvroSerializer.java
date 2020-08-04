// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.experimental.serializer.ObjectSerializer;
import com.azure.core.experimental.serializer.TypeReference;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.SchemaRegistrySerializer;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import reactor.core.publisher.Mono;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

/**
 * Asynchronous registry-based serializer implementation.
 */
public final class SchemaRegistryAvroSerializer extends SchemaRegistrySerializer implements ObjectSerializer {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryAvroSerializer.class);

    /**
     *
     * @param registryClient
     * @param codec
     * @param schemaGroup
     * @param autoRegisterSchemas
     */
    SchemaRegistryAvroSerializer(SchemaRegistryAsyncClient registryClient, AvroSchemaRegistryCodec codec,
                                      String schemaGroup, Boolean autoRegisterSchemas) {
        super(registryClient, codec, Collections.singletonList(codec), autoRegisterSchemas, schemaGroup);
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        return null;
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream,
        TypeReference<T> typeReference) {
        return null;
    }

    @Override
    public <S extends OutputStream> S serialize(S stream, Object value) {
        return null;
    }

    @Override
    public <S extends OutputStream> Mono<S> serializeAsync(S stream, Object object) {
        if (object == null) {
            return Mono.empty();
        }

        return super.serializeAsync(stream, object);
    }

//    @Override
//    public <T> Mono<T> deserialize(InputStream stream, Class<T> clazz) {
//        return this.deserialize(stream)
//            .map(o -> {
//                if (clazz.isInstance(o)) {
//                    return clazz.cast(o);
//                }
//                throw logger.logExceptionAsError(new IllegalStateException("Deserialized object not of class %s"));
//            });
//    }
}

