// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema;

import com.azure.storage.internal.avro.implementation.AvroParserState;

import java.util.function.Consumer;

/**
 * An abstract class that represents a simple Avro schema that can return an Object result.
 *
 * Simple avro schemas directly consume bytes from the state to populate the result.
 *
 * @see AvroSchema
 */
public abstract class AvroSimpleSchema extends AvroSchema {
    /**
     * Constructs a new Schema.
     *
     * @param state    The state of the parser.
     * @param onResult The result handler.
     */
    public AvroSimpleSchema(AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
    }

    /**
     * @return Whether or not progress can be made for this schema.
     */
    public abstract boolean canProgress();

    /**
     * Makes some progress in parsing the type.
     */
    public abstract void progress();

}
