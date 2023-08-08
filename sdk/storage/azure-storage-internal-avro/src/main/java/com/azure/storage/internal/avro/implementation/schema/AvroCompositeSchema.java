// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema;

import com.azure.storage.internal.avro.implementation.AvroParserState;

import java.util.function.Consumer;

/**
 * An abstract class that represents a composite Avro schema that can return an Object result.
 *
 * Composite avro schemas depend on other avro schemas to populate the result.
 *
 * @see AvroSchema
 */
public abstract class AvroCompositeSchema extends AvroSchema {
    /**
     * Constructs a new Schema.
     *
     * @param state    The state of the parser.
     * @param onResult The result handler.
     */
    public AvroCompositeSchema(AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
    }
}
