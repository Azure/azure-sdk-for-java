// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client;

import com.azure.core.util.logging.ClientLogger;
import java.util.function.Function;

/**
 * Stores all relevant information returned from SchemaRegistryClient layer.
 */
public class SchemaRegistryObject {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryObject.class);

    private final String schemaId;
    private final String schemaType;
    private final Function<String, Object> parseMethod;
    private final byte[] schemaBytes;

    private Object deserialized;

    /**
     * Initializes SchemaRegistryObject instance.
     *
     * @param schemaId schema ID
     * @param schemaType type of schema, e.g. avro, json
     * @param schemaByteArray byte payload representing schema, returned from Azure Schema Registry
     * @param parseMethod method to deserialize schema payload into Object
     */
    public SchemaRegistryObject(
        String schemaId,
        String schemaType,
        byte[] schemaByteArray,
        Function<String, Object> parseMethod) {
        this.schemaId = schemaId;
        this.schemaType = schemaType;
        this.schemaBytes = schemaByteArray.clone();
        this.deserialized = null;
        this.parseMethod = parseMethod;
    }

    /**
     * @return schema ID
     */
    public String getSchemaId() {
        return schemaId;
    }

    /**
     * @return schema type associated with the schema payload
     */
    public String getSchemaType() {
        return schemaType;
    }

    /**
     *  Deserialize schema bytes returned from Schema Registry.  If deserialization has happened once, the deserialized
     *  object is stored and returned.
     *
     *  @return schema object, deserialized using stored schema parser method.
     */
    public Object deserialize() {
        if (this.deserialized == null) {
            String schemaString = new String(
                this.schemaBytes, CachedSchemaRegistryClient.SCHEMA_REGISTRY_SERVICE_ENCODING);

            logger.verbose("Deserializing schema, id: '{}', schema string '{}'", this.schemaId, schemaString);

            try {
                this.deserialized = parseMethod.apply(schemaString);
            } catch (Exception e) {
                logger.logExceptionAsError(new SchemaRegistryClientException("Failed to deserialize schema", e));
            }

        }
        return deserialized;
    }
}
