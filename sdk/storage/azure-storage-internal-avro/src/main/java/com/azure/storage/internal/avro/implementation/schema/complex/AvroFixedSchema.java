// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.complex;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSimpleSchema;

import java.util.function.Consumer;

/**
 * Fixed instances are encoded using the number of bytes declared in the schema.
 * Wait for the cache to fill up, then get the bytes.
 * Note: We return a List of ByteBuffer since the number of bytes requested can be long and a single ByteBuffer can
 * only hold Integer.MAX bytes.
 *
 * FixedBytes
 */
public class AvroFixedSchema extends AvroSimpleSchema {

    private final long size;

    /**
     * Constructs a new AvroFixedSchema.
     *
     * @param size The number of bytes to read.
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroFixedSchema(long size, AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
        this.size = size;
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);
    }

    @Override
    public void progress() {
        /* Consume size bytes, then we're done. */
        this.result = this.state.read(size);
        this.done = true;
    }

    @Override
    public boolean canProgress() {
        /* State must have enough bytes to satisfy size.*/
        return this.state.sizeGreaterThan(this.size);
    }
}
