// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.client.SchemaRegistryObject;
import com.azure.data.schemaregistry.client.SchemaRegistryClient;
import com.azure.data.schemaregistry.client.SchemaRegistryClientException;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Common implementation for all registry-based deserializers.
 */
public abstract class AbstractDataDeserializer extends AbstractDataSerDe {
    private final ClientLogger logger = new ClientLogger(AbstractDataDeserializer.class);

    private final Map<String, ByteDecoder> byteDecoderMap = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Constructor called by all concrete implementation constructors.
     * Should only call parent constructor.
     * @param schemaRegistryClient client to be used for fetching schemas by ID
     */
    protected AbstractDataDeserializer(SchemaRegistryClient schemaRegistryClient) {
        super(schemaRegistryClient);
    }

    /**
     * Special case constructor for Kafka deserializer's empty constructors.
     */
    protected AbstractDataDeserializer() { }

    /**
     * Fetches schema referenced by prefixed ID and deserializes the subsequent payload into Java object.
     *
     * @param payload byte payload, produced by an Azure Schema Registry client producer
     * @return object, deserialized with the prefixed schema
     * @throws SerializationException if deserialization of registry schema or message payload fails.
     */
    protected Object deserialize(byte[] payload) throws SerializationException {
        if (payload == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(payload);
        String schemaGuid = getSchemaGuidFromPayload(buffer);
        SchemaRegistryObject registryObject;
        Object payloadSchema;

        try {
            registryObject = this.schemaRegistryClient.getSchemaByGuid(schemaGuid);
            payloadSchema = registryObject.deserialize();
        } catch (SchemaRegistryClientException e) {
            throw logger.logExceptionAsError(
                new SerializationException(String.format("Failed to retrieve schema for id %s", schemaGuid), e));
        }

        if (payloadSchema == null) {
            throw logger.logExceptionAsError(
             new SerializationException(
                    String.format("Payload schema returned as null. Schema type: %s, Schema ID: %s",
                            registryObject.getSchemaType(), registryObject.getSchemaId())));
        }

        int start = buffer.position() + buffer.arrayOffset();
        int length = buffer.limit() - AbstractDataSerDe.SCHEMA_ID_SIZE;
        byte[] b = Arrays.copyOfRange(buffer.array(), start, start + length);

        ByteDecoder byteDecoder = getByteDecoder(registryObject);
        return byteDecoder.decodeBytes(b, payloadSchema);
    }


    /**
     * Fetches the correct ByteDecoder based on schema type of the message.
     *
     * @param registryObject object returned from SchemaRegistryClient, contains schema type
     * @return ByteDecoder to be used to deserialize encoded payload bytes
     * @throws SerializationException if decoder for the required schema type has not been loaded
     */
    private ByteDecoder getByteDecoder(SchemaRegistryObject registryObject) throws SerializationException {
        ByteDecoder decoder = byteDecoderMap.get(registryObject.getSchemaType());
        if (decoder == null) {
            throw logger.logExceptionAsError(
                new SerializationException(
                    String.format("No decoder class found for schema type '%s'.", registryObject.getSchemaType())
                ));
        }
        return decoder;
    }

    /**
     * @param buffer full payload bytes
     * @return String representation of schema ID
     * @throws SerializationException if schema ID could not be extracted from payload
     */
    private String getSchemaGuidFromPayload(ByteBuffer buffer) throws SerializationException {
        byte[] schemaGuidByteArray = new byte[AbstractDataSerDe.SCHEMA_ID_SIZE];
        try {
            buffer.get(schemaGuidByteArray);
        } catch (BufferUnderflowException e) {
            throw logger.logExceptionAsError(new SerializationException("Payload too short, no readable guid.", e));
        }

        return new String(schemaGuidByteArray, schemaRegistryClient.getEncoding());
    }

    /**
     * Loads a ByteDecoder to be used for decoding message payloads of specified schema type.
     * @param decoder ByteDecoder class instance to be loaded
     */
    protected void loadByteDecoder(ByteDecoder decoder) {
        if (decoder == null) {
            throw logger.logExceptionAsError(new SerializationException("ByteDecoder cannot be null"));
        }

        this.byteDecoderMap.put(decoder.schemaType(), decoder);
        this.schemaRegistryClient.addSchemaParser(decoder);
    }
}
