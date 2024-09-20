// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.core.util.serializer.TypeReference;

/**
 * <a href="https://json-schema.org/">JSON schema</a> aware class that can validate and generate JSON schemas.
 */
public abstract class JsonSchemaGenerator {

    /**
     * True if the data matches its schema. Otherwise, false.
     *
     * @param data Data to verify.
     * @param dataType Type of data.
     * @param schemaDefinition The schema to verify the data against.
     * @return True if the data matches its schema definition otherwise false.
     * @param <T> Target type of data.
     * @throws SchemaRegistryJsonSchemaException If an error occurs while validating JSON schema
     */
    public abstract <T> boolean isValid(Object data, TypeReference<T> dataType, String schemaDefinition);

    /**
     * Given a type, gets its JSON schema definition.
     *
     * @param type Type to get schema for.
     * @return A string representing its JSON schema.
     * @param <T> Type of data.
     * @throws SchemaRegistryJsonSchemaException If a JSON schema could not be generated.
     */
    public abstract <T> String generateSchema(TypeReference<T> type);
}
