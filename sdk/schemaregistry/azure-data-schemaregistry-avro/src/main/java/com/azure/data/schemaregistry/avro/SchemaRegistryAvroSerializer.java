// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.experimental.serializer.AvroSerializerProviders;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistrySerializer;
import com.azure.data.schemaregistry.models.SerializationType;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Asynchronous registry-based serializer implementation.
 */
public final class SchemaRegistryAvroSerializer extends SchemaRegistrySerializer implements ObjectSerializer {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryAvroSerializer.class);
    private static final Boolean AVRO_SPECIFIC_READER_DEFAULT = false;

    private final Boolean avroSpecificReader;

    /**
     * @param registryClient
     * @param avroSpecificReader
     * @param schemaGroup
     * @param autoRegisterSchemas
     */
    SchemaRegistryAvroSerializer(SchemaRegistryAsyncClient registryClient, Boolean avroSpecificReader,
        String schemaGroup, Boolean autoRegisterSchemas) {
        super(registryClient, autoRegisterSchemas, schemaGroup);

        if (avroSpecificReader == null) {
            this.avroSpecificReader = SchemaRegistryAvroSerializer.AVRO_SPECIFIC_READER_DEFAULT;
        } else {
            this.avroSpecificReader = avroSpecificReader;
        }
    }

    /**
     * @return serialization type
     */
    protected SerializationType getSerializationType() {
        return SerializationType.AVRO;
    }

    /**
     * Returns schema name for storing schemas in schema registry store.
     *
     * @param object Schema object used to generate schema path
     * @return schema name as string
     */
    protected String getSchemaName(Object object) {
        return AvroSerializerProviders.getSchemaName(object);
    }

    /**
     * @param object Schema object used to generate schema string
     * @return string representation of schema
     */
    protected String getSchemaString(Object object) {
        return AvroSerializerProviders.getSchema(object);
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
    public void serialize(OutputStream stream, Object value) {
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream stream, Object object) {
        if (object == null) {
            return Mono.empty();
        }

        return super.serializeAsync(stream, object);
    }

    /**
     * Returns ByteArrayOutputStream containing Avro encoding of object parameter
     *
     * @param object Object to be encoded into byte stream
     * @return closed ByteArrayOutputStream
     */
    protected byte[] encode(Object object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        AvroSerializerProviders.createInstance(getSchemaString(object))
            .serialize(byteArrayOutputStream, object);

        return byteArrayOutputStream.toByteArray();
    }


    /**
     * @param b byte array containing encoded bytes
     * @param object schema for Avro reader read - fetched from Azure Schema Registry
     * @return deserialized object
     */
    protected Object decode(byte[] b, Object object) {
        Objects.requireNonNull(object, "Schema must not be null.");

        if (!(object instanceof String)) {
            throw logger.logExceptionAsError(
                new IllegalStateException("Object must be an Avro schema."));
        }

        String schema = (String) object;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(b);

        return AvroSerializerProviders.createInstance(schema)
            .deserialize(inputStream, TypeReference.createInstance(Object.class));
    }
}

