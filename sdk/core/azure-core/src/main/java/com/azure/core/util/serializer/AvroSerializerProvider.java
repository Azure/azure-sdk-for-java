// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

/**
 * An interface to be implemented by any azure-core plugin that wishes to provide an Avro {@link ObjectSerializer}
 * implementation.
 */
@FunctionalInterface
public interface AvroSerializerProvider {

    /**
     * Creates a new Avro-based {@link ObjectSerializer} tied to the given schema.
     *
     * @param schema The Avro schema that will be associated to the serializer.
     * @return A new Avro-based {@link ObjectSerializer} instance.
     */
    ObjectSerializer createInstance(String schema);
}
