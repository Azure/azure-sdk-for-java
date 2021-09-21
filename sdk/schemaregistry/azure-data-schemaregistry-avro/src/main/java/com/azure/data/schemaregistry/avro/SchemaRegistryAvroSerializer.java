// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SerializationType;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Schema Registry-based serializer implementation for Avro data format.
 */
public final class SchemaRegistryAvroSerializer implements ObjectSerializer {

    private static final Map<Schema.Type, Schema> PRIMITIVE_SCHEMAS;
    static final int SCHEMA_ID_SIZE = 32;
    static final int RECORD_FORMAT_INDICATOR_SIZE = 4;

    private final ClientLogger logger = new ClientLogger(SchemaRegistryAvroSerializer.class);
    private final SchemaRegistryAsyncClient schemaRegistryClient;
    private final AvroSerializer avroSerializer;
    private final String schemaGroup;
    private final boolean autoRegisterSchemas;

    static {
        final HashMap<Schema.Type, Schema> schemas = new HashMap<>();
        schemas.put(Schema.Type.NULL, Schema.create(Schema.Type.NULL));
        schemas.put(Schema.Type.BOOLEAN, Schema.create(Schema.Type.BOOLEAN));
        schemas.put(Schema.Type.INT, Schema.create(Schema.Type.INT));
        schemas.put(Schema.Type.LONG, Schema.create(Schema.Type.LONG));
        schemas.put(Schema.Type.FLOAT, Schema.create(Schema.Type.FLOAT));
        schemas.put(Schema.Type.DOUBLE, Schema.create(Schema.Type.DOUBLE));
        schemas.put(Schema.Type.BYTES, Schema.create(Schema.Type.BYTES));
        schemas.put(Schema.Type.STRING, Schema.create(Schema.Type.STRING));

        PRIMITIVE_SCHEMAS = Collections.unmodifiableMap(schemas);
    }

    SchemaRegistryAvroSerializer(SchemaRegistryAsyncClient schemaRegistryClient,
        AvroSerializer avroSerializer, String schemaGroup, boolean autoRegisterSchemas) {
        this.schemaRegistryClient = Objects.requireNonNull(schemaRegistryClient,
            "'schemaRegistryClient' cannot be null.");
        this.avroSerializer = Objects.requireNonNull(avroSerializer,
            "'avroSchemaRegistryUtils' cannot be null.");
        this.schemaGroup = Objects.requireNonNull(schemaGroup, "'schemaGroup' cannot be null.");
        this.autoRegisterSchemas = autoRegisterSchemas;
    }

    /**
     * Deserializes the {@code inputStream} into a strongly-typed object.
     *
     * @param inputStream The stream to read from.
     * @param typeReference Type reference of the strongly-typed object.
     * @param <T> Strongly typed object.
     *
     * @return The deserialized object. If {@code inputStream} is null then {@code null} is returned.
     *
     * @throws NullPointerException if {@code typeReference} is null.
     */
    @Override
    public <T> T deserialize(InputStream inputStream, TypeReference<T> typeReference) {
        return deserializeAsync(inputStream, typeReference).block();
    }

    /**
     * Deserializes the {@code inputStream} into a strongly-typed object.
     *
     * @param inputStream The stream to read from.
     * @param typeReference Type reference of the strongly-typed object.
     * @param <T> Strongly typed object.
     *
     * @return A Mono that completes with the deserialized object. If {@code inputStream} is null, then Mono completes
     *     with an empty Mono.
     *
     * @throws NullPointerException if {@code typeReference} is null.
     */
    @Override
    public <T> Mono<T> deserializeAsync(InputStream inputStream, TypeReference<T> typeReference) {
        if (inputStream == null) {
            return Mono.empty();
        }

        if (typeReference == null) {
            return monoError(logger, new NullPointerException("'typeReference' cannot be null."));
        }

        return Mono.fromCallable(() -> {
            byte[] payload = new byte[inputStream.available()];
            while (true) {
                if (inputStream.read(payload) == -1) {
                    break;
                }
            }
            return payload;
        })
            .flatMap(payload -> {
                if (payload.length == 0) {
                    return Mono.empty();
                }

                ByteBuffer buffer = ByteBuffer.wrap(payload);

                byte[] recordFormatIndicator = getRecordFormatIndicator(buffer);
                if (!Arrays.equals(recordFormatIndicator, new byte[]{0x00, 0x00, 0x00, 0x00})) {
                    return Mono.error(
                        new IllegalStateException("Illegal format: unsupported record format indicator in payload"));
                }

                String schemaId = getSchemaIdFromPayload(buffer);

                return this.schemaRegistryClient.getSchema(schemaId)
                    .handle((registryObject, sink) -> {
                        byte[] payloadSchema = registryObject.getSchema();
                        int start = buffer.position() + buffer.arrayOffset();
                        int length = buffer.limit() - SCHEMA_ID_SIZE;
                        byte[] b = Arrays.copyOfRange(buffer.array(), start, start + length);

                        sink.next(avroSerializer.decode(b, payloadSchema, typeReference));
                    });
            });
    }

    /**
     * Serializes the {@code object} into the {@code outputStream}.
     *
     * @param outputStream Output stream to write serialization of {@code object} to.
     * @param object The object to serialize into {@code outputStream}.
     *
     * @throws NullPointerException if {@code outputStream} or {@code object} is null.
     */
    @Override
    public void serialize(OutputStream outputStream, Object object) {
        serializeAsync(outputStream, object).block();
    }

    /**
     * Serializes the {@code object} into the {@code outputStream}.
     *
     * @param outputStream Output stream to write serialization of {@code object} to.
     * @param object The object to serialize into {@code outputStream}.
     *
     * @return A Mono that completes when the object has been serialized into the stream.
     *
     * @throws NullPointerException if {@code outputStream} or {@code object} is null.
     */
    @Override
    public Mono<Void> serializeAsync(OutputStream outputStream, Object object) {
        if (outputStream == null) {
            return monoError(logger, new NullPointerException("'outputStream' cannot be null."));
        }

        if (object == null) {
            return monoError(logger, new NullPointerException(
                "Null object, behavior should be defined in concrete serializer implementation."));
        }

        Schema schema;
        try {
            schema = getSchema(object);
        } catch (IllegalArgumentException exception) {
            return monoError(logger, exception);
        }

        String schemaString =
        String schemaName = avroSerializer.getSchemaName(object);

        return this.maybeRegisterSchema(this.schemaGroup, schemaName, schemaString)
            .handle((id, sink) -> {
                ByteBuffer recordFormatIndicatorBuffer = ByteBuffer
                    .allocate(RECORD_FORMAT_INDICATOR_SIZE)
                    .put(new byte[]{0x00, 0x00, 0x00, 0x00});
                ByteBuffer idBuffer = ByteBuffer
                    .allocate(SCHEMA_ID_SIZE)
                    .put(id.getBytes(StandardCharsets.UTF_8));
                try {
                    outputStream.write(recordFormatIndicatorBuffer.array());
                    outputStream.write(idBuffer.array());
                    outputStream.write(avroSerializer.encode(object));
                    sink.complete();
                } catch (IOException e) {
                    sink.error(new UncheckedIOException(e.getMessage(), e));
                }
            });
    }

    /**
     * Returns Avro schema for specified object, including null values
     *
     * @param object object for which Avro schema is being returned
     *
     * @return Avro schema for object's data structure
     *
     * @throws IllegalArgumentException if object type is unsupported
     */
    static Schema getSchema(Object object) throws IllegalArgumentException {
        if (object == null) {
            return PRIMITIVE_SCHEMAS.get(Schema.Type.NULL);
        } else if (object instanceof Boolean) {
            return PRIMITIVE_SCHEMAS.get(Schema.Type.BOOLEAN);
        } else if (object instanceof Integer) {
            return PRIMITIVE_SCHEMAS.get(Schema.Type.INT);
        } else if (object instanceof Long) {
            return PRIMITIVE_SCHEMAS.get(Schema.Type.LONG);
        } else if (object instanceof Float) {
            return PRIMITIVE_SCHEMAS.get(Schema.Type.FLOAT);
        } else if (object instanceof Double) {
            return PRIMITIVE_SCHEMAS.get(Schema.Type.DOUBLE);
        } else if (object instanceof CharSequence) {
            return PRIMITIVE_SCHEMAS.get(Schema.Type.STRING);
        } else if (object instanceof byte[] || object instanceof ByteBuffer || object instanceof Byte[]) {
            return PRIMITIVE_SCHEMAS.get(Schema.Type.BYTES);
        } else if (object instanceof GenericContainer) {
            return ((GenericContainer) object).getSchema();
        } else {
            throw new IllegalArgumentException("Unsupported Avro type. Supported types are null, Boolean, Integer,"
                + " Long, Float, Double, String, byte[], and GenericContainer");
        }
    }

    /**
     * If auto-registering is enabled, register schema against Schema Registry. If auto-registering is disabled, fetch
     * schema ID for provided schema. Requires pre-registering of schema against registry.
     *
     * @param schemaGroup Schema group where schema should be registered.
     * @param schemaName name of schema
     * @param schemaString string representation of schema being stored - must match group schema type
     *
     * @return string representation of schema ID
     */
    private Mono<String> maybeRegisterSchema(String schemaGroup, String schemaName, String schemaString) {
        if (this.autoRegisterSchemas) {
            return this.schemaRegistryClient
                .registerSchema(schemaGroup, schemaName, schemaString, SerializationType.AVRO)
                .map(SchemaProperties::getSchemaId);
        } else {
            return this.schemaRegistryClient.getSchemaId(
                schemaGroup, schemaName, schemaString, SerializationType.AVRO);
        }
    }

    /**
     * @param buffer full payload bytes
     *
     * @return String representation of schema ID
     */
    private static String getSchemaIdFromPayload(ByteBuffer buffer) {
        byte[] schemaGuidByteArray = new byte[SCHEMA_ID_SIZE];
        buffer.get(schemaGuidByteArray);

        return new String(schemaGuidByteArray, StandardCharsets.UTF_8);
    }

    /**
     * Reads the first {@link #RECORD_FORMAT_INDICATOR_SIZE 4} bytes of the buffer.
     *
     * @param buffer The buffer to read from.
     *
     * @return The first 4 bytes from the buffer.
     */
    private static byte[] getRecordFormatIndicator(ByteBuffer buffer) {
        byte[] indicatorBytes = new byte[RECORD_FORMAT_INDICATOR_SIZE];
        buffer.get(indicatorBytes);
        return indicatorBytes;
    }
}

