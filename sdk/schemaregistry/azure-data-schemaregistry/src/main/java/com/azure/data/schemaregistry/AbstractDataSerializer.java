// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.client.SchemaRegistryClient;
import com.azure.data.schemaregistry.client.SchemaRegistryClientException;
import com.azure.data.schemaregistry.client.SchemaRegistryObject;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Common implementation for all registry-based serializers.
 */
public abstract class AbstractDataSerializer extends AbstractDataSerDe {
    private final ClientLogger logger = new ClientLogger(AbstractDataSerializer.class);

    public static final Boolean AUTO_REGISTER_SCHEMAS_DEFAULT = false;
    public static final String SCHEMA_GROUP_DEFAULT = "$default";

    protected Codec serializerCodec = null;
    private final Map<String, Codec> deserializerCodecMap = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);

    protected String schemaType;
    protected Boolean autoRegisterSchemas = AbstractDataSerializer.AUTO_REGISTER_SCHEMAS_DEFAULT;
    protected String schemaGroup = AbstractDataSerializer.SCHEMA_GROUP_DEFAULT;

    /**
     * @param schemaRegistryClient registry client to be used for storing schemas.  Not null.
     */
    public AbstractDataSerializer(SchemaRegistryClient schemaRegistryClient) {
        super(schemaRegistryClient);
    }

    /**
     * Special case constructor for Kafka serializer.
     */
    public AbstractDataSerializer() {
    }

    /**
     * Set Codec class to be used for serialized objects into bytes
     * @param codec Codec instance
     */
    protected void setSerializerCodec(Codec codec) {
        if (this.serializerCodec != null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Setting multiple encoders on serializer not permitted"));
        }
        this.serializerCodec = codec;
        this.schemaType = codec.schemaType();
        this.schemaRegistryClient.addSchemaParser(codec);
    }

    /**
     * Core implementation of registry-based serialization.
     * ID for data schema is fetched from the registry and prefixed to the encoded byte array
     * representation of the object param.
     *
     * @param object object to be serialized
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
            schemaType = serializerCodec.schemaType();
        }

        String schemaString = serializerCodec.getSchemaString(object);
        String schemaName = serializerCodec.getSchemaName(object);

        return this.maybeRegisterSchema(this.schemaGroup, schemaName, schemaString, this.schemaType)
            .onErrorResume(e -> {
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
                    }
                    else {
                        builder.append(e.getCause().getMessage());
                    }

                    return monoError(logger, new SerializationException(builder.toString(), e));
                }
                else {
                    return monoError(logger, new SerializationException(e.getMessage(), e));
                }
            })
            .map(id -> {
                ByteBuffer idBuffer = ByteBuffer.allocate(AbstractDataSerDe.SCHEMA_ID_SIZE)
                    .put(id.getBytes(StandardCharsets.UTF_8));
                try {
                    s.write(idBuffer.array());
                    serializerCodec.encode(object).writeTo(s);
                } catch (IOException e) {
                    throw new SerializationException(e.getMessage(), e);
                }
                return s;
            })
            .onErrorResume(
                SerializationException.class,
                e -> Mono.error(logger.logExceptionAsError(e)));
    }


    /**
     * Core implementation for registry-based deserialization.
     * Fetches schema referenced by prefixed ID and deserializes the subsequent payload into Java object.
     *
     * @param s InputStream containing bytes encoded by an Azure Schema Registry producer
     * @return object, deserialized with the prefixed schema
     * @throws SerializationException if deserialization of registry schema or message payload fails.
     */
    protected Mono<Object> deserialize(InputStream s) throws SerializationException {
        if (s == null) {
            return Mono.empty();
        }

        return Mono.fromCallable(s::readAllBytes)
            .onErrorResume(IOException.class, e -> monoError(logger, new SerializationException(e.getMessage(), e)))
            .map(payload -> {
                if (payload == null || payload.length == 0) {
                    return Mono.empty();
                }

                ByteBuffer buffer = ByteBuffer.wrap(payload);
                String schemaId = getSchemaIdFromPayload(buffer);

                return this.schemaRegistryClient.getSchemaById(schemaId).onErrorResume(e -> {
                    if (e instanceof SchemaRegistryClientException) {
                        StringBuilder builder = new StringBuilder(
                            String.format("Failed to retrieve schema for id %s", schemaId));

                        if (e.getCause() instanceof HttpResponseException) {
                            HttpResponseException httpException = (HttpResponseException) e.getCause();
                            builder.append("HTTP ")
                                .append(httpException.getResponse().getStatusCode())
                                .append(" ")
                                .append(httpException.getResponse().getBodyAsString());
                        }
                        else {
                            builder.append(e.getCause().getMessage());
                        }

                        return monoError(logger, new SerializationException(builder.toString(), e));
                    }
                    else {
                        return monoError(logger, new SerializationException(e.getMessage(), e));
                    }})
                    .map(registryObject -> {
                        Object payloadSchema = registryObject.deserialize();

                        if (payloadSchema == null) {
                            throw logger.logExceptionAsError(
                                new SerializationException(
                                    String.format("Payload schema returned as null. Schema type: %s, Schema ID: %s",
                                        registryObject.getSchemaType(), registryObject.getSchemaId())));
                        }

                        int start = buffer.position() + buffer.arrayOffset();
                        int length = buffer.limit() - AbstractDataSerDe.SCHEMA_ID_SIZE;
                        byte[] b = Arrays.copyOfRange(buffer.array(), start, start + length);

                        Codec codec = getDeserializerCodec(registryObject);
                        return codec.decodeBytes(b, payloadSchema);
                    });
            });
    }


    /**
     * Fetches the correct ByteDecoder based on schema type of the message.
     *
     * @param registryObject object returned from SchemaRegistryClient, contains schema type
     * @return ByteDecoder to be used to deserialize encoded payload bytes
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
        byte[] schemaGuidByteArray = new byte[AbstractDataSerDe.SCHEMA_ID_SIZE];
        try {
            buffer.get(schemaGuidByteArray);
        } catch (BufferUnderflowException e) {
            throw logger.logExceptionAsError(new SerializationException("Payload too short, no readable guid.", e));
        }

        return new String(schemaGuidByteArray, schemaRegistryClient.getEncoding());
    }

    /**
     * Loads Codec to be used for decoding message payloads of specified schema type.
     * @param codec Codec class instance to be loaded
     */
    protected void loadDeserializerCodec(Codec codec) {
        if (codec == null) {
            throw logger.logExceptionAsError(new SerializationException("ByteDecoder cannot be null"));
        }

        this.deserializerCodecMap.put(codec.schemaType(), codec);
        this.schemaRegistryClient.addSchemaParser(codec);
    }

    /**
     * If auto-registering is enabled, register schema against SchemaRegistryClient.
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
            return this.schemaRegistryClient.register(schemaGroup, schemaName, schemaString, schemaType)
                .map(SchemaRegistryObject::getSchemaId);
        } else {
            return this.schemaRegistryClient.getSchemaId(
                schemaGroup, schemaName, schemaString, schemaType);
        }
    }
}
