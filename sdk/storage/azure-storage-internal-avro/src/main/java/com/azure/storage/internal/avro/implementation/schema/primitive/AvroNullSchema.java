// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSimpleSchema;

import java.util.function.Consumer;

/**
 * A null is written as zero bytes.
 */
public class AvroNullSchema extends AvroSimpleSchema {

    /**
     * Constructs a new AvroNullSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroNullSchema(AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);
    }

    @Override
    public void progress() {
        /* Just return a custom null object, then we're done. */
        this.result = new Null();
        this.done = true;
    }

    /**
     * Can always make progress since null is zero bytes.
     */
    @Override
    public boolean canProgress() {
        return true;
    }

    /* We use a custom type to return null since null cannot be emitted in a Flux.
       Users of the AvroParser must transform all NullSchema.Null objects to null if necessary. */
    public static class Null {
    }

}
