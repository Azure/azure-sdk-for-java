// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSimpleSchema;

import java.util.function.Consumer;

/**
 *  boolean is written as a single byte whose value is either 0 (false) or 1 (true).
 *
 *  Byte
 */
public class AvroBooleanSchema extends AvroSimpleSchema {

    private final ClientLogger logger = new ClientLogger(AvroBooleanSchema.class);

    /**
     * Constructs a new AvroBooleanSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroBooleanSchema(AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);
    }

    @Override
    public void progress() {
        /* Consume a byte, and determine what it represents. */
        byte b = this.state.read();
        if (b == (byte) 0) {
            this.result = false;
        } else if (b == (byte) 1) {
            this.result = true;
        } else {
            throw logger.logExceptionAsError(new IllegalStateException(String.format(
                "Expected byte %b to be a boolean value.", b)));
        }
        this.done = true;
    }

    @Override
    public boolean canProgress() {
        /* State must have at least BOOLEAN_SIZE bytes to progess on a boolean. */
        return this.state.sizeGreaterThan(AvroConstants.BOOLEAN_SIZE);
    }
}
