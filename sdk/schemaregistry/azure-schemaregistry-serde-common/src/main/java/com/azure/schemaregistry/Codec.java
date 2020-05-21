// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.schemaregistry;

/**
 * Base interface for all ByteEncoder and ByteDecoder interfaces
 */
public interface Codec {
    /**
     * @return String representation of serialization format type, e.g. "avro" or "json".
     *
     * Utilized by schema registry store and client as non-case-sensitive tags for
     * schemas of specific serialization format.
     */
    String serializationFormat();

    /**
     * Parses string representation of schema into schema Object
     * @param schemaString string representation of schema
     * @return schema object to be used for decoding payloads
     */
    Object parseSchemaString(String schemaString);
}
