// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistrySerializer;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Asynchronous registry-based serializer implementation.
 */
public final class SchemaRegistryAvroSerializer extends SchemaRegistrySerializer implements ObjectSerializer {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryAvroSerializer.class);
    private static final Boolean AVRO_SPECIFIC_READER_DEFAULT = false;


    /**
     * @param registryClient
     * @param schemaGroup
     * @param avroUtils
     * @param autoRegisterSchemas
     */
    SchemaRegistryAvroSerializer(SchemaRegistryAsyncClient registryClient, String schemaGroup,
                                 Boolean autoRegisterSchemas, SchemaRegistryAvroUtils avroUtils) {
        super(registryClient, avroUtils, autoRegisterSchemas, schemaGroup);
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        return null;
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        return null;
    }

    @Override
    public <S extends OutputStream> S serialize(S stream, Object value) {
        return null;
    }

    @Override
    public <S extends OutputStream> Mono<S> serializeAsync(S stream, Object value) {
        return null;
    }

//
//    @Override
//    @SuppressWarnings("unchecked")
//    public OutputStream serialize(OutputStream stream, Object value) {
//        return stream;
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public Mono<Void> serializeAsync(OutputStream stream, Object object) {
//        if (object == null) {
//            return Mono.empty();
//        }
//
//        return super.serializeAsync(stream, object);
//    }

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

