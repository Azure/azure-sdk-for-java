// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.experimental.serializer.AvroSerializerProviders;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.SchemaRegistrySerializationUtils;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.models.SerializationType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * Utility class for Avro serialization.  Provides all methods required for the core Schema Registry serializer code
 * to construct Schema Registry messages.
 */
class SchemaRegistryAvroUtils implements SchemaRegistrySerializationUtils {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryAvroUtils.class);
    private static final Boolean AVRO_SPECIFIC_READER_DEFAULT = false;

    private final Boolean avroSpecificReader;

    /**
     * Instantiates AvroCodec instance
     *
     * @param avroSpecificReader flag indicating if decoder should decode records as SpecificRecords
     */
    public SchemaRegistryAvroUtils(Boolean avroSpecificReader) {
        if (avroSpecificReader == null) {
            this.avroSpecificReader = SchemaRegistryAvroUtils.AVRO_SPECIFIC_READER_DEFAULT;
        } else {
            this.avroSpecificReader = avroSpecificReader;
        }
    }

    @Override
    public SerializationType getSerializationType() {
        return SerializationType.AVRO;
    }

    /**
     * @param schemaString string representation of schema
     * @return avro schema
     */
    @Override
    public String parseSchemaString(String schemaString) {
        return schemaString;
    }


    /**
     * @param object Schema object used to generate schema string
     * @return string representation of schema
     */
    @Override
    public String getSchemaString(Object object) {
        return AvroSerializerProviders.getSchema(object);
    }

    /**
     * Returns schema name for storing schemas in schema registry store.
     *
     * @param object Schema object used to generate schema path
     * @return schema name as string
     */
    @Override
    public String getSchemaName(Object object) {
        return AvroSerializerProviders.getSchemaName(object);
    }

    /**
     * Returns ByteArrayOutputStream containing Avro encoding of object parameter
     *
     * @param object Object to be encoded into byte stream
     * @return closed ByteArrayOutputStream
     */
    @Override
    public byte[] encode(Object object) {
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
    @Override
    public Object decode(byte[] b, Object object) {
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
