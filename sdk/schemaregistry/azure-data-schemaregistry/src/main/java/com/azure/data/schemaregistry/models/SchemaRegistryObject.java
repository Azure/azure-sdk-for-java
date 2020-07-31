// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.models;

import com.azure.core.util.logging.ClientLogger;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * Stores all relevant information returned from CachedSchemaRegistryClient layer.
 */
public class SchemaRegistryObject {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryObject.class);

    private final String schemaId;
    private final String schemaType;
    private final Function<String, Object> parseMethod;
    private final byte[] schemaBytes;
    private final String schemaName;
    private final String schemaGroup;
    private final int schemaVersion;

    private Object deserialized;

    /**
     * Initializes SchemaRegistryObject instance.
     *  @param schemaId schema ID
     * @param schemaType type of schema, e.g. avro, json
     * @param schemaGroup schema group under which schema is stored in the registry
     * @param schemaName schema name
     * @param schemaVersion schema version
     * @param schemaByteArray byte payload representing schema, returned from registry
     * @param parseMethod method to deserialize schema payload into Object
     */
    public SchemaRegistryObject(
        String schemaId,
        String schemaType,
        String schemaGroup, String schemaName,
        int schemaVersion,
        byte[] schemaByteArray,
        Function<String, Object> parseMethod) {
        this.schemaId = schemaId;
        this.schemaType = schemaType;
        this.schemaBytes = schemaByteArray.clone();
        this.deserialized = null;
        this.parseMethod = parseMethod;
        this.schemaGroup = schemaGroup;
        this.schemaName = schemaName;
        this.schemaVersion = schemaVersion;
    }

    /**
     * @return schema ID
     */
    public String getSchemaId() {
        return this.schemaId;
    }

    /**
     * @return schema type associated with the schema payload
     */
    public String getSchemaType() {
        return this.schemaType;
    }

    /**
     * @return schema group under which schema is stored
     */
    public String getSchemaGroup() {
        return this.schemaGroup;
    }

    /**
     * @return schema name
     */
    public String getSchemaName() {
        return this.schemaName;
    }

    /**
     * @return schema version
     */
    public int getSchemaVersion() {
        return this.schemaVersion;
    }

    /**
     *  Deserialize schema bytes returned from Schema Registry.  If deserialization has happened once, the deserialized
     *  object is stored and returned.
     *
     *  @return schema object, deserialized using stored schema parser method.
     */
    public Object getSchema() {
        if (this.deserialized == null) {
            String schemaString = new String(
                this.schemaBytes, StandardCharsets.UTF_8);

            logger.verbose("Deserializing schema, id: '{}', schema string '{}'", this.schemaId, schemaString);

            try {
                this.deserialized = parseMethod.apply(schemaString);
            } catch (Exception e) {
                throw logger.logExceptionAsError(new SchemaRegistryClientException("Failed to deserialize schema", e));
            }

        }
        return deserialized;
    }
}
