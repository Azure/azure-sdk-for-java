// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroCompositeSchema;

import java.util.function.Consumer;

/**
 * int values are written using variable-length zig-zag coding.
 *
 * Since longs and ints share the same encoding, just add a LongSchema and convert the result into an Integer.
 */
public class AvroIntegerSchema extends AvroCompositeSchema {

    private final ClientLogger logger = new ClientLogger(AvroIntegerSchema.class);

    /**
     * Constructs a new AvroIntegerSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroIntegerSchema(AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);
        /* Read the Long, then convert it to an Integer. */
        AvroLongSchema numberSchema = new AvroLongSchema(
            this.state,
            this::onNumber
        );
        numberSchema.pushToStack();
    }

    /**
     * Number handler
     *
     * @param number The Long to convert.
     */
    private void onNumber(Object number) {
        checkType("number", number, Long.class);
        /* Convert the Long into an Integer, then we're done. */
        this.result = Math.toIntExact((Long) number);
        this.done = true;
    }
}
