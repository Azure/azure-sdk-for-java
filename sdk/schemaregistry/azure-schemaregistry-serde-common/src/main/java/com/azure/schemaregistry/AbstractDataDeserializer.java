/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

import com.azure.schemaregistry.client.SchemaRegistryObject;
import com.azure.schemaregistry.client.SchemaRegistryClient;
import com.azure.schemaregistry.client.SchemaRegistryClientException;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDataDeserializer extends AbstractDataSerDe {
    protected final Map<String, ByteDecoder> byteDecoderMap = new ConcurrentHashMap<>();

    protected AbstractDataDeserializer(SchemaRegistryClient schemaRegistryClient) {
        super(schemaRegistryClient);
    }

    // special case for KafkaAvroDeserializer
    protected AbstractDataDeserializer() {
    }

    protected Object deserialize(byte[] payload) throws SerializationException {
        if (payload == null) {
            return null;
        }

        ByteBuffer buffer = getByteBuffer(payload);
        String schemaGuid = getSchemaGuidFromPayload(buffer);
        SchemaRegistryObject registryObject = null;
        Object payloadSchema = null;

        try {
            registryObject = this.schemaRegistryClient.getSchemaByGuid(schemaGuid);
            payloadSchema = registryObject.deserialize();
        } catch (IOException | SchemaRegistryClientException e) {
            throw new SerializationException("Failed to retrieve schema for id " + schemaGuid, e);
        }

        // TODO: how to handle unknown formats
        if (payloadSchema == null) {
            throw new SerializationException(
                    String.format("Cast failure for REST object from registry. Object type: %s",
                            registryObject.deserialize().getClass().getName()));
        }

        int start = buffer.position() + buffer.arrayOffset();
        int length = buffer.limit() - AbstractDataSerDe.idSize;
        byte[] b = Arrays.copyOfRange(buffer.array(), start, start + length);

        ByteDecoder byteDecoder = getByteDecoder(registryObject);
        return byteDecoder.decodeBytes(b, payloadSchema);
    }


    private ByteDecoder getByteDecoder(SchemaRegistryObject registryObject) throws SerializationException {
        ByteDecoder decoder = byteDecoderMap.get(registryObject.serializationType);
        if (decoder == null) {
            throw new SerializationException("No decoder class found for serialization type " +
                    registryObject.serializationType);
        }
        return decoder;
    }

    private ByteBuffer getByteBuffer(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        return buffer;
    }

    private String getSchemaGuidFromPayload(ByteBuffer buffer) throws SerializationException {
        byte[] schemaGuidByteArray = new byte[AbstractDataSerDe.idSize];
        try {
            buffer.get(schemaGuidByteArray);
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Payload too short, no readable guid.", e);
        }

        return new String(schemaGuidByteArray);
    }

    protected void loadByteDecoder(ByteDecoder decoder) {
        this.byteDecoderMap.put(decoder.serializationFormat(), decoder);
        this.schemaRegistryClient.loadSchemaParser(decoder.serializationFormat(), decoder::parseSchemaString);
    }
}
