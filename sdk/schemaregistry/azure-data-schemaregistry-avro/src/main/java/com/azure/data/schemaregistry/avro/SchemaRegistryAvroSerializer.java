// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SerializationType;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Schema Registry-based serializer implementation for Avro data format.
 */
public final class SchemaRegistryAvroSerializer implements ObjectSerializer {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryAvroSerializer.class);

    static final int SCHEMA_ID_SIZE = 32;
    private final SchemaRegistryAsyncClient schemaRegistryClient;
    private final AvroSchemaRegistryUtils codec;
    private final String schemaGroup;
    private final Boolean autoRegisterSchemas;

    SchemaRegistryAvroSerializer(SchemaRegistryAsyncClient registryClient, AvroSchemaRegistryUtils codec,
                                      String schemaGroup, Boolean autoRegisterSchemas) {
        this.schemaRegistryClient = registryClient;
        this.codec = codec;
        this.schemaGroup = schemaGroup;
        this.autoRegisterSchemas = autoRegisterSchemas;
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        return deserializeAsync(stream, typeReference).block();
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream,
        TypeReference<T> typeReference) {

        if (stream == null) {
            return Mono.empty();
        }

        return Mono.fromCallable(() -> {
            byte[] payload = new byte[stream.available()];

            while (true) {
                if (stream.read(payload) == -1) {
                    break;
                }
            }
            return payload;
        })
            .flatMap(payload -> {
                if (payload == null || payload.length == 0) {
                    return Mono.empty();
                }

                ByteBuffer buffer = ByteBuffer.wrap(payload);
                String schemaId = getSchemaIdFromPayload(buffer);

                return this.schemaRegistryClient.getSchema(schemaId)
                    .handle((registryObject, sink) -> {
                        byte[] payloadSchema = registryObject.getSchema();

                        if (payloadSchema == null) {
                            sink.error(logger.logExceptionAsError(
                                new NullPointerException(
                                    String.format("Payload schema returned as null. Schema type: %s, Schema ID: %s",
                                        registryObject.getSerializationType(), registryObject.getSchemaId()))));
                            return;
                        }

                        int start = buffer.position() + buffer.arrayOffset();
                        int length = buffer.limit() - SCHEMA_ID_SIZE;
                        byte[] b = Arrays.copyOfRange(buffer.array(), start, start + length);

                        sink.next(codec.decode(b, payloadSchema));
                    });
            });
    }

    @Override
    public void serialize(OutputStream outputStream, Object value) {
        serializeAsync(outputStream, value).block();
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream outputStream, Object object) {

        if (object == null) {
            return monoError(logger, new NullPointerException(
                "Null object, behavior should be defined in concrete serializer implementation."));
        }

        String schemaString = codec.getSchemaString(object);
        String schemaName = codec.getSchemaName(object);

        return this.maybeRegisterSchema(this.schemaGroup, schemaName, schemaString)
            .handle((id, sink) -> {
                ByteBuffer idBuffer = ByteBuffer.allocate(SCHEMA_ID_SIZE)
                    .put(id.getBytes(StandardCharsets.UTF_8));
                try {
                    outputStream.write(idBuffer.array());
                    outputStream.write(codec.encode(object));
                    sink.complete();
                } catch (IOException e) {
                    sink.error(new UncheckedIOException(e.getMessage(), e));
                }
            });
    }

    /**
     * @param buffer full payload bytes
     * @return String representation of schema ID
     */
    private String getSchemaIdFromPayload(ByteBuffer buffer) {
        byte[] schemaGuidByteArray = new byte[SCHEMA_ID_SIZE];
        buffer.get(schemaGuidByteArray);

        return new String(schemaGuidByteArray, StandardCharsets.UTF_8);
    }

    /**
     * If auto-registering is enabled, register schema against Schema Registry.
     * If auto-registering is disabled, fetch schema ID for provided schema. Requires pre-registering of schema
     * against registry.
     *
     * @param schemaGroup Schema group where schema should be registered.
     * @param schemaName name of schema
     * @param schemaString string representation of schema being stored - must match group schema type
     * @return string representation of schema ID
     */
    private Mono<String> maybeRegisterSchema(
        String schemaGroup, String schemaName, String schemaString) {
        if (this.autoRegisterSchemas) {
            return this.schemaRegistryClient
                .registerSchema(schemaGroup, schemaName, schemaString, SerializationType.AVRO)
                .map(SchemaProperties::getSchemaId);
        } else {
            return this.schemaRegistryClient.getSchemaId(
                schemaGroup, schemaName, schemaString, SerializationType.AVRO);
        }
    }
}

