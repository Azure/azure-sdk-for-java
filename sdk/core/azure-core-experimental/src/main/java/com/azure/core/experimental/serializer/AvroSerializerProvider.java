// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

/**
 * An interface to be implemented by any azure-core plugin that wishes to provide an Avro {@link AvroSerializer}
 * implementation.
 */
public interface AvroSerializerProvider {

    /**
     * Creates a new {@link AvroSerializer} tied to the given schema.
     *
     * @param schema The Avro schema that will be associated to the serializer.
     * @return A new {@link AvroSerializer} instance.
     */
    AvroSerializer createInstance(String schema);

    /**
     * Returns the Avro schema for specified object.
     *
     * @param object The object having its Avro schema retrieved.
     * @return The Avro schema for the object.
     * @throws IllegalArgumentException If the object is an unsupported type.
     */
    String getSchema(Object object);

    /**
     * Returns the Avro schema for specified object.
     *
     * @param object The object having its Avro schema name retrieved.
     * @return The Avro schema name for the object.
     * @throws IllegalArgumentException If the object is an unsupported type.
     */
    String getSchemaName(Object object);
}
