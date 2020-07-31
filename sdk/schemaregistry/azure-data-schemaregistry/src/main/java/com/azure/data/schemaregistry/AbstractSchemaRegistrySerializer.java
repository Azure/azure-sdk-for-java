// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Common implementation for all registry-based serializers.
 */
public abstract class AbstractSchemaRegistrySerializer {
    private final ClientLogger logger = new ClientLogger(AbstractSchemaRegistrySerializer.class);

    private static final Boolean AUTO_REGISTER_SCHEMAS_DEFAULT = false;
    private static final String SCHEMA_GROUP_DEFAULT = "$default";
    static final int SCHEMA_ID_SIZE = 32;

    CachedSchemaRegistryAsyncClient schemaRegistryClient;

    private Codec serializerCodec;
    private final Map<String, Codec> deserializerCodecMap = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
    private String schemaType;

    private Boolean autoRegisterSchemas = AbstractSchemaRegistrySerializer.AUTO_REGISTER_SCHEMAS_DEFAULT;
    private String schemaGroup = AbstractSchemaRegistrySerializer.SCHEMA_GROUP_DEFAULT;

    /**
     * Constructor for AbstractSchemaRegistrySerializer implementations.
     *
     * @param schemaRegistryClient client to be used for interfacing with Schema Registry service
     * @param serializerCodec Codec to be used for serialization operations
     * @param deserializerCodecList list of Codecs to be used to deserialize incoming payloads
     * @param schemaGroup schema group where schema should be found or registered during serialization.
     * @param autoRegisterSchemas if true, serializer will register schema against the service under the specified group
     */
    public AbstractSchemaRegistrySerializer(CachedSchemaRegistryAsyncClient schemaRegistryClient,
                                            Codec serializerCodec, List<Codec> deserializerCodecList,
                                            String schemaGroup, Boolean autoRegisterSchemas) {
        Objects.requireNonNull(serializerCodec);
        Objects.requireNonNull(deserializerCodecList);

        if (schemaRegistryClient == null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Schema registry client must be initialized and passed into builder."));
        }

        if (deserializerCodecList.size() == 0) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("At least one Codec must be provided for deserialization."));
        }

        this.schemaRegistryClient = schemaRegistryClient;
        this.serializerCodec = serializerCodec;
        for (Codec c : deserializerCodecList) {
            if (this.deserializerCodecMap.containsKey(c.getSchemaType())) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Only on Codec can be provided per schema serialization type."));
            }
            this.deserializerCodecMap.put(c.getSchemaType(), c);
        }

        if (schemaGroup != null) {
            this.schemaGroup = schemaGroup;
        }

        if (autoRegisterSchemas != null) {
            this.autoRegisterSchemas = autoRegisterSchemas;
        }
    }

    /**
     * Core implementation of registry-based serialization.
     * ID for data schema is fetched from the registry and prefixed to the encoded byte array
     * representation of the object param.
     *
     * @param s Output stream destination for encoded bytes
     * @param object object to be serialized
     * @param <T> Type of the output stream parameter.
     * @return byte array containing encoded bytes with prefixed schema ID
     * @throws SerializationException if serialization operation fails during runtime.
     */
    protected <T extends OutputStream> Mono<T> serializeImpl(T s, Object object) {
        if (object == null) {
            return monoError(logger, new SerializationException(
                "Null object, behavior should be defined in concrete serializer implementation."));
        }

        if (serializerCodec == null) {
            return monoError(logger, new SerializationException(
                "Byte encoder null, serializer must be initialized with a byte encoder."));
        }

        if (schemaType == null) {
            schemaType = serializerCodec.getSchemaType();
        }

        String schemaString = serializerCodec.getSchemaString(object);
        String schemaName = serializerCodec.getSchemaName(object);

        return this.maybeRegisterSchema(this.schemaGroup, schemaName, schemaString, this.schemaType)
            .onErrorMap(e -> {
                if (e instanceof SchemaRegistryClientException) {
                    StringBuilder builder = new StringBuilder();
                    if (this.autoRegisterSchemas) {
                        builder.append(String.format("Error registering Avro schema. Group: %s, name: %s. ",
                            schemaGroup, schemaName));
                    } else {
                        builder.append(String.format("Error retrieving Avro schema. Group: %s, name: %s. ",
                            schemaGroup, schemaName));
                    }

                    if (e.getCause() instanceof HttpResponseException) {
                        HttpResponseException httpException = (HttpResponseException) e.getCause();
                        builder.append("HTTP ")
                            .append(httpException.getResponse().getStatusCode())
                            .append(" ")
                            .append(httpException.getResponse().getBodyAsString());
                    } else {
                        builder.append(e.getCause().getMessage());
                    }

                    return logger.logExceptionAsError(new SerializationException(builder.toString(), e));
                } else {
                    return logger.logExceptionAsError(new SerializationException(e.getMessage(), e));
                }
            })
            .handle((id, sink) -> {
                ByteBuffer idBuffer = ByteBuffer.allocate(AbstractSchemaRegistrySerializer.SCHEMA_ID_SIZE)
                    .put(id.getBytes(StandardCharsets.UTF_8));
                try {
                    s.write(idBuffer.array());
                    serializerCodec.encode(object).writeTo(s);
                } catch (IOException e) {
                    sink.error(new SerializationException(e.getMessage(), e));
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
     * @throws SerializationException if deserialization of registry schema or message payload fails.
     */
    protected Mono<Object> deserializeImpl(InputStream s) {
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
                System.out.println(schemaId);

                SchemaRegistryObject block = this.schemaRegistryClient.getSchema(schemaId).block();
                System.out.println(block);
                return this.schemaRegistryClient.getSchema(schemaId)
                    .onErrorMap(IOException.class,
                        e -> logger.logExceptionAsError(new SerializationException(e.getMessage(), e)))
                    .handle((registryObject, sink) -> {
                        Object payloadSchema = registryObject.getSchema();

                        if (payloadSchema == null) {
                            sink.error(logger.logExceptionAsError(
                                new SerializationException(
                                    String.format("Payload schema returned as null. Schema type: %s, Schema ID: %s",
                                        registryObject.getSchemaType(), registryObject.getSchemaId()))));
                            return;
                        }

                        int start = buffer.position() + buffer.arrayOffset();
                        int length = buffer.limit() - AbstractSchemaRegistrySerializer.SCHEMA_ID_SIZE;
                        byte[] b = Arrays.copyOfRange(buffer.array(), start, start + length);

                        Codec codec = getDeserializerCodec(registryObject);
                        sink.next(codec.decode(b, payloadSchema));
                    })
                    .onErrorMap(e -> {
                        if (e instanceof SchemaRegistryClientException) {
                            StringBuilder builder = new StringBuilder(
                                String.format("Failed to retrieve schema for id %s", schemaId));

                            if (e.getCause() instanceof HttpResponseException) {
                                HttpResponseException httpException = (HttpResponseException) e.getCause();
                                builder.append("HTTP ")
                                    .append(httpException.getResponse().getStatusCode())
                                    .append(" ")
                                    .append(httpException.getResponse().getBodyAsString());
                            } else {
                                builder.append(e.getCause().getMessage());
                            }

                            return logger.logExceptionAsError(new SerializationException(builder.toString(), e));
                        } else {
                            return logger.logExceptionAsError(new SerializationException(e.getMessage(), e));
                        }
                    });
            });
    }


    /**
     * Fetches the correct Codec based on schema type of the message.
     *
     * @param registryObject object returned from CachedSchemaRegistryAsyncClient, contains schema type
     * @return Codec to be used to deserialize encoded payload bytes
     * @throws SerializationException if decoder for the required schema type has not been loaded
     */
    private Codec getDeserializerCodec(SchemaRegistryObject registryObject) throws SerializationException {
        Codec codec = deserializerCodecMap.get(registryObject.getSchemaType());
        if (codec == null) {
            throw logger.logExceptionAsError(
                new SerializationException(
                    String.format("No deserializer codec class found for schema type '%s'.",
                        registryObject.getSchemaType())
                ));
        }
        return codec;
    }

    /**
     * @param buffer full payload bytes
     * @return String representation of schema ID
     * @throws SerializationException if schema ID could not be extracted from payload
     */
    private String getSchemaIdFromPayload(ByteBuffer buffer) throws SerializationException {
        byte[] schemaGuidByteArray = new byte[AbstractSchemaRegistrySerializer.SCHEMA_ID_SIZE];
        try {
            buffer.get(schemaGuidByteArray);
        } catch (BufferUnderflowException e) {
            throw logger.logExceptionAsError(new SerializationException("Payload too short, no readable guid.", e));
        }

        return new String(schemaGuidByteArray, CachedSchemaRegistryAsyncClient.SCHEMA_REGISTRY_SERVICE_ENCODING);
    }

    /**
     * If auto-registering is enabled, register schema against Schema Registry.
     * If auto-registering is disabled, fetch schema ID for provided schema. Requires pre-registering of schema
     * against registry.
     *
     * @param schemaGroup Schema group where schema should be registered.
     * @param schemaName name of schema
     * @param schemaString string representation of schema being stored - must match group schema type
     * @param schemaType type of schema being stored, e.g. avro
     * @return string representation of schema ID
     * @throws SchemaRegistryClientException upon registry client operation failure
     */
    private Mono<String> maybeRegisterSchema(
        String schemaGroup, String schemaName, String schemaString, String schemaType) {
        if (this.autoRegisterSchemas) {
            return this.schemaRegistryClient.registerSchema(schemaGroup, schemaName, schemaString, schemaType)
                .map(SchemaRegistryObject::getSchemaId);
        } else {
            return this.schemaRegistryClient.getSchemaId(
                schemaGroup, schemaName, schemaString, schemaType);
        }
    }
}
