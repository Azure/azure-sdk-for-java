// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.client.SchemaRegistryClient;
import com.azure.data.schemaregistry.client.SchemaRegistryClientException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Common implementation for all registry-based serializers.
 */
public abstract class AbstractDataSerializer extends AbstractDataSerDe {
    private final ClientLogger logger = new ClientLogger(AbstractDataSerializer.class);

    public static final Boolean AUTO_REGISTER_SCHEMAS_DEFAULT = false;
    public static final String SCHEMA_GROUP_DEFAULT = "$default";

    protected ByteEncoder byteEncoder = null;
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
     * Set ByteEncoder class to be used for serialized objects into bytes
     * @param byteEncoder ByteEncoder instance
     */
    protected void setByteEncoder(ByteEncoder byteEncoder) {
        if (this.byteEncoder != null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Setting multiple encoders on serializer not permitted"));
        }
        this.byteEncoder = byteEncoder;
        this.schemaType = byteEncoder.schemaType();
        this.schemaRegistryClient.addSchemaParser(byteEncoder);
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
    protected byte[] serializeImpl(Object object) {
        if (object == null) {
            throw logger.logExceptionAsError(new SerializationException(
                "Null object, behavior should be defined in concrete serializer implementation."));
        }

        if (byteEncoder == null) {
            throw logger.logExceptionAsError(
                new SerializationException("Byte encoder null, serializer must be initialized with a byte encoder."));
        }

        if (schemaType == null) {
            schemaType = byteEncoder.schemaType();
        }

        String schemaString = byteEncoder.getSchemaString(object);
        String schemaName = byteEncoder.getSchemaName(object);

        try {
            String schemaGuid = maybeRegisterSchema(
                this.schemaGroup, schemaName, schemaString, this.schemaType);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteBuffer guidBuffer = ByteBuffer.allocate(AbstractDataSerDe.SCHEMA_ID_SIZE)
                .put(schemaGuid.getBytes(StandardCharsets.UTF_8));
            out.write(guidBuffer.array());
            byteEncoder.encode(object).writeTo(out);
            return out.toByteArray();
        } catch (SchemaRegistryClientException | IOException e) {
            if (this.autoRegisterSchemas) {
                throw logger.logExceptionAsError(
                    new SerializationException(
                        String.format("Error registering Avro schema. Group: %s, name: %s", schemaGroup, schemaName),
                        e));
            } else {
                throw logger.logExceptionAsError(
                    new SerializationException(
                        String.format("Error retrieving Avro schema. Group: %s, name: %s", schemaGroup, schemaName),
                        e));
            }
        }
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
    private String maybeRegisterSchema(
        String schemaGroup, String schemaName, String schemaString, String schemaType)
        throws SchemaRegistryClientException {
        if (this.autoRegisterSchemas) {
            return this.schemaRegistryClient.register(schemaGroup, schemaName, schemaString, schemaType)
                .getSchemaId();
        } else {
            return this.schemaRegistryClient.getSchemaId(
                schemaGroup, schemaName, schemaString, schemaType);
        }
    }
}
