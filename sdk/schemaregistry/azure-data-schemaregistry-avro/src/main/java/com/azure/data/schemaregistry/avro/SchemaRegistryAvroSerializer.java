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
import java.util.Objects;

/**
 * Asynchronous registry-based serializer for Avro SpecificRecords and GenericRecords.
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
        return this.deserializeAsync(stream, typeReference).block();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        return super.deserializeInternalAsync(stream)
            .handle((o, sink) -> {
                Class<T> outClass = (Class<T>)typeReference.getJavaType();
                if (outClass.isInstance(o)) {
                    sink.next(outClass.cast(o));
                }
                else {
                    sink.error(logger.logExceptionAsError(new IllegalStateException(
                        String.format("Deserialized object is %s, could not be cast to %s",
                            o.getClass().getName(),
                            typeReference.getJavaType()))));
                }
            });
    }

    @Override
    public <S extends OutputStream> S serialize(S stream, Object value) {
        return this.serializeAsync(stream, value).block();
    }

    @Override
    public <S extends OutputStream> Mono<S> serializeAsync(S stream, Object value) {
        Objects.requireNonNull(stream, "'stream' cannot be null.");

        if (value == null) {
            return Mono.empty();
        }

        return super.serializeInternalAsync(stream, value).thenReturn(stream);
    }
}

