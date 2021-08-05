// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.complex;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroCompositeSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroIntegerSchema;

import java.util.List;
import java.util.function.Consumer;

/**
 * An enum is encoded by a int, representing the zero-based position of the symbol in the schema.
 *
 * Add an IntegerSchema and convert the result into the Enum by indexing the values.
 *
 * Integer
 */
public class AvroEnumSchema extends AvroCompositeSchema {

    private final ClientLogger logger = new ClientLogger(AvroEnumSchema.class);

    private final List<String> values;

    /**
     * Constructs a new AvroEnumSchema.
     *
     * @param symbols The enum symbols.
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroEnumSchema(List<String> symbols, AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
        this.values = symbols;
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
        if (i < 0 || i >= this.values.size()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid index to parse enum"));
        }
        /* Using the zero-based index, get the appropriate symbol, then we're done. */
        this.result = this.values.get(i);
        this.done = true;
    }
}
