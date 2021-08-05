// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.complex;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroCompositeSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroIntegerSchema;

import java.util.List;
import java.util.function.Consumer;

/**
 * A union is encoded by first writing a long value indicating the zero-based position within the union of the
 * schema of its value. The value is then encoded per the indicated schema within the union.
 *
 * Add an IntegerSchema and figure out what schema to read, then read that type.
 *
 * Integer TypeSchema
 */
public class AvroUnionSchema extends AvroCompositeSchema {

    private final ClientLogger logger = new ClientLogger(AvroUnionSchema.class);

    private final List<AvroType> types;

    /**
     * Constructs a new AvroUnionSchema.
     *
     * @param types The types the schema could be.
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroUnionSchema(List<AvroType> types, AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
        this.types = types;
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);

        /* Read the index, call onIndex. */
        AvroIntegerSchema indexSchema = new AvroIntegerSchema(
            this.state,
            this::onIndex
        );
        indexSchema.pushToStack();
    }

    /**
     * Index handler.
     *
     * @param index The index.
     */
    private void onIndex(Object index) {
        checkType("index", index, Integer.class);
        Integer i = (Integer) index;
        if (i < 0 || i >= this.types.size()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid index to parse union"));
        }
        /* Using the zero-based index, get the appropriate type. */
        AvroType type = this.types.get(i);

        /* Read the type, call onType. */
        AvroSchema typeSchema = getSchema(
            type,
            this.state,
            this::onType
        );
        typeSchema.pushToStack();
    }

    /**
     * Type handler.
     *
     * @param value the value.
     */
    private void onType(Object value) {
        /* Store the value, then we're done. */
        this.result = value;
        this.done = true;
    }
}
