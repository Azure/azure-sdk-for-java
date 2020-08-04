// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import static com.azure.core.util.FluxUtil.monoError;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.models.SchemaRegistryObject;
import com.azure.data.schemaregistry.models.SerializationType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import reactor.core.publisher.Mono;

/**
 * Common implementation for all registry-based serializers.
 */
public abstract class SchemaRegistrySerializer {
    private final ClientLogger logger = new ClientLogger(SchemaRegistrySerializer.class);

    static final Boolean AUTO_REGISTER_SCHEMAS_DEFAULT = false;
    static final String SCHEMA_GROUP_DEFAULT = "$default";
    static final int SCHEMA_ID_SIZE = 32;

    SchemaRegistryAsyncClient schemaRegistryClient;

    private SchemaRegistryCodec serializerSchemaRegistryCodec;
    private final Map<String, SchemaRegistryCodec> deserializerCodecMap = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
    private SerializationType serializationType;

    Boolean autoRegisterSchemas = SchemaRegistrySerializer.AUTO_REGISTER_SCHEMAS_DEFAULT;
    String schemaGroup = SchemaRegistrySerializer.SCHEMA_GROUP_DEFAULT;

    /**
     * Constructor for AbstractSchemaRegistrySerializer implementations.
     *
     * @param schemaRegistryClient client to be used for interfacing with Schema Registry service
     * @param serializerSchemaRegistryCodec Codec to be used for serialization operations
     * @param deserializerSchemaRegistryCodecList list of Codecs to be used to deserialize incoming payloads
     */
    public SchemaRegistrySerializer(SchemaRegistryAsyncClient schemaRegistryClient,
                                            SchemaRegistryCodec serializerSchemaRegistryCodec, List<SchemaRegistryCodec> deserializerSchemaRegistryCodecList) {
        this(schemaRegistryClient, serializerSchemaRegistryCodec, deserializerSchemaRegistryCodecList, null, null);
    }

    public SchemaRegistrySerializer(SchemaRegistryAsyncClient schemaRegistryClient,
        SchemaRegistryCodec serializerSchemaRegistryCodec, List<SchemaRegistryCodec> deserializerSchemaRegistryCodecList, Boolean autoRegisterSchemas,
        String schemaGroup) {

        Objects.requireNonNull(serializerSchemaRegistryCodec);
        Objects.requireNonNull(deserializerSchemaRegistryCodecList);

        if (schemaRegistryClient == null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Schema registry client must be initialized and passed into builder."));
        }

        if (deserializerSchemaRegistryCodecList.size() == 0) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("At least one Codec must be provided for deserialization."));
        }

        this.schemaRegistryClient = schemaRegistryClient;
        this.serializerSchemaRegistryCodec = serializerSchemaRegistryCodec;
        for (SchemaRegistryCodec c : deserializerSchemaRegistryCodecList) {
            if (this.deserializerCodecMap.containsKey(c.getSerializationType())) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Only on Codec can be provided per schema serialization type."));
            }
            this.deserializerCodecMap.put(c.getSerializationType().toString(), c);
        }

        // send configurations only
        if (autoRegisterSchemas != null) {
            this.autoRegisterSchemas = autoRegisterSchemas;
        }

        if (schemaGroup != null) {
            this.schemaGroup = schemaGroup;
        }
    }

    /**
     * @return String representation of schema type, e.g. "avro" or "json".
     *
     * Utilized by schema registry store and client as non-case-sensitive tags for
     * schemas of a specific type.
     */
    protected abstract SerializationType getSerializationType();

    /**
     * Core implementation of registry-based serialization.
     * ID for data schema is fetched from the registry and prefixed to the encoded byte array
     * representation of the object param.
     *
     * @param s Output stream destination for encoded bytes
     * @param object object to be serialized
     * @param <T> Type of the output stream parameter.
     * @return byte array containing encoded bytes with prefixed schema ID
     */
    protected <S extends OutputStream> Mono<S> serializeAsync(S s, Object object) {
        if (object == null) {
            return monoError(logger, new NullPointerException(
                "Null object, behavior should be defined in concrete serializer implementation."));
        }

        if (serializerSchemaRegistryCodec == null) {
            return monoError(logger, new NullPointerException(
                "Byte encoder null, serializer must be initialized with a byte encoder."));
        }

        if (serializationType == null) {
            serializationType = serializerSchemaRegistryCodec.getSerializationType();
        }

        String schemaString = getSchemaString(object);
        String schemaName = getSchemaName(object);

        return this.maybeRegisterSchema(this.schemaGroup, schemaName, schemaString, this.serializationType)
//            .onErrorMap(e -> {
//                if (e instanceof SchemaRegistryClientException) {
//                    StringBuilder builder = new StringBuilder();
//                    if (this.autoRegisterSchemas) {
//                        builder.append(String.format("Error registering Avro schema. Group: %s, name: %s. ",
//                            schemaGroup, schemaName));
//                    } else {
//                        builder.append(String.format("Error retrieving Avro schema. Group: %s, name: %s. ",
//                            schemaGroup, schemaName));
//                    }
//
//                    if (e.getCause() instanceof HttpResponseException) {
//                        HttpResponseException httpException = (HttpResponseException) e.getCause();
//                        builder.append("HTTP ")
//                            .append(httpException.getResponse().getStatusCode())
//                            .append(" ")
//                            .append(httpException.getResponse().getBodyAsString());
//                    } else {
//                        builder.append(e.getCause().getMessage());
//                    }
//
//                    return logger.logExceptionAsError(new SerializationException(builder.toString(), e));
//                } else {
//                    return logger.logExceptionAsError(new SerializationException(e.getMessage(), e));
//                }
//            })
            .handle((id, sink) -> {
                ByteBuffer idBuffer = ByteBuffer.allocate(SchemaRegistrySerializer.SCHEMA_ID_SIZE)
                    .put(id.getBytes(StandardCharsets.UTF_8));
                try {
                    s.write(idBuffer.array());
                    s.write(encode(object));
                } catch (IOException e) {
                    sink.error(new UncheckedIOException(e.getMessage(), e));
                }
                sink.next(s);
            });
    }


    /**
     * Core implementation for registry-based deserialization.
     * Fetches schema referenced by prefixed ID and deserializes the subsequent payload into Java object.
     *
     * @param s InputStream containing bytes encoded by an Azure Schema Registry producer
     * @return object, deserialized with the prefixed schema
     */
    protected Mono<Object> deserializeAsync(InputStream s) {
        if (s == null) {
            return Mono.empty();
        }

        return Mono.fromCallable(() -> {
            byte[] payload = new byte[s.available()];
            while (s.read(payload) != -1) {}
            return payload;
        })
            .flatMap(payload -> {
                if (payload == null || payload.length == 0) {
                    return Mono.empty();
                }

                ByteBuffer buffer = ByteBuffer.wrap(payload);
                String schemaId = getSchemaIdFromPayload(buffer);

                return this.schemaRegistryClient.getSchema(schemaId)
//                    .onErrorMap(IOException.class,
//                        e -> logger.logExceptionAsError(new SerializationException(e.getMessage(), e)))
                    .handle((registryObject, sink) -> {
                        Object payloadSchema = registryObject.getSchema();

                        if (payloadSchema == null) {
                            sink.error(logger.logExceptionAsError(
                                new NullPointerException(
                                    String.format("Payload schema returned as null. Schema type: %s, Schema ID: %s",
                                        registryObject.getSerializationType(), registryObject.getSchemaId()))));
                            return;
                        }

                        int start = buffer.position() + buffer.arrayOffset();
                        int length = buffer.limit() - SchemaRegistrySerializer.SCHEMA_ID_SIZE;
                        byte[] b = Arrays.copyOfRange(buffer.array(), start, start + length);

                        sink.next(decode(b, payloadSchema));
                    });
//                    .onErrorMap(e -> {
//                        if (e instanceof SchemaRegistryClientException) {
//                            StringBuilder builder = new StringBuilder(
//                                String.format("Failed to retrieve schema for id %s", schemaId));
//
//                            if (e.getCause() instanceof HttpResponseException) {
//                                HttpResponseException httpException = (HttpResponseException) e.getCause();
//                                builder.append("HTTP ")
//                                    .append(httpException.getResponse().getStatusCode())
//                                    .append(" ")
//                                    .append(httpException.getResponse().getBodyAsString());
//                            } else {
//                                builder.append(e.getCause().getMessage());
//                            }
//
//                            return logger.logExceptionAsError(new SerializationException(builder.toString(), e));
//                        } else {
//                            return logger.logExceptionAsError(new SerializationException(e.getMessage(), e));
//                        }
//                    });
            });
    }

    /**
     * Return schema name for storing in registry store
     * @param object Schema object
     * Refer to Schema Registry documentation for information on schema grouping and naming.
     *
     * @return schema name
     */
    protected abstract String getSchemaName(Object object);

    /**
     * Returns string representation of schema object to be stored in the service.
     *
     * @param object Schema object used to generate schema string
     * @return String representation of schema object parameter
     */
    protected abstract String getSchemaString(Object object);

    /**
     * Converts object into stream containing the encoded representation of the object.
     * @param object Object to be encoded into byte stream
     * @return output stream containing byte representation of object
     */
    protected abstract byte[] encode(Object object);

    /**
     * Decodes byte array into Object given provided schema object.
     * @param encodedBytes payload to be decoded
     * @param schemaObject object used to decode the payload
     * @return deserialized object
     */
    protected abstract Object decode(byte[] encodedBytes, Object schemaObject);

    /**
     * Fetches the correct Codec based on schema type of the message.
     *
     * @param registryObject object returned from CachedSchemaRegistryAsyncClient, contains schema type
     * @return Codec to be used to deserialize encoded payload bytes
     */
    private SchemaRegistryCodec getDeserializerCodec(SchemaRegistryObject registryObject) {
        SchemaRegistryCodec schemaRegistryCodec = deserializerCodecMap.get(registryObject.getSerializationType());
        if (schemaRegistryCodec == null) {
            throw logger.logExceptionAsError(
                new NullPointerException(
                    String.format("No deserializer codec class found for schema type '%s'.",
                        registryObject.getSerializationType())
                ));
        }
        return schemaRegistryCodec;
    }

    /**
     * @param buffer full payload bytes
     * @return String representation of schema ID
     */
    private String getSchemaIdFromPayload(ByteBuffer buffer) {
        byte[] schemaGuidByteArray = new byte[SchemaRegistrySerializer.SCHEMA_ID_SIZE];
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
     * @param serializationType type of schema being stored, e.g. avro
     * @return string representation of schema ID
     */
    private Mono<String> maybeRegisterSchema(
        String schemaGroup, String schemaName, String schemaString, SerializationType serializationType) {
        if (this.autoRegisterSchemas) {
            return this.schemaRegistryClient.registerSchema(schemaGroup, schemaName, schemaString, serializationType)
                .map(SchemaRegistryObject::getSchemaId);
        } else {
            return this.schemaRegistryClient.getSchemaId(
                schemaGroup, schemaName, schemaString, serializationType);
        }
    }
}
