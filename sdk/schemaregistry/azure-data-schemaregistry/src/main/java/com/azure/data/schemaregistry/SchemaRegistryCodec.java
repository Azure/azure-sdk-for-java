// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.models.SerializationType;

/**
 * An interface defining operations required for registry-based serialization and deserialization.
 */
public interface SchemaRegistryCodec {
    /**
     * @return String representation of schema type, e.g. "avro" or "json".
     *
     * Utilized by schema registry store and client as non-case-sensitive tags for
     * schemas of a specific type.
     */
    SerializationType getSerializationType();

    /**
     * Parses string representation of schema into schema Object
     * @param schemaString string representation of schema
     * @return schema object to be used for decoding payloads
     */
    Object parseSchemaString(String schemaString);

    /**
     * Return schema name for storing in registry store
     * @param object Schema object
     * Refer to Schema Registry documentation for information on schema grouping and naming.
     *
     * @return schema name
     */
    String getSchemaName(Object object);

    String getSchemaGroup();

    /**
     * Returns string representation of schema object to be stored in the service.
     *
     * @param object Schema object used to generate schema string
     * @return String representation of schema object parameter
     */
    String getSchemaString(Object object);

    /**
     * Converts object into stream containing the encoded representation of the object.
     * @param object Object to be encoded into byte stream
     * @return output stream containing byte representation of object
     */
    byte[] encode(Object object);

    /**
     * Decodes byte array into Object given provided schema object.
     * @param encodedBytes payload to be decoded
     * @param schemaObject object used to decode the payload
     * @return deserialized object
     */
    Object decode(byte[] encodedBytes, Object schemaObject);
}
