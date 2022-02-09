// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroCompositeSchema;
import com.azure.storage.internal.avro.implementation.schema.complex.AvroFixedSchema;

import java.util.List;
import java.util.function.Consumer;

/**
 * bytes are encoded as a long followed by that many bytes of data.
 *
 * Add a LongSchema to read the length, then add a FixedSchema to read that many bytes.
 * Note: We return a List of ByteBuffer since the number of bytes requested can be long and a single ByteBuffer can
 * only hold Integer.MAX bytes.
 *
 * Integer FixedBytes
 */
public class AvroBytesSchema extends AvroCompositeSchema {

    /**
     * Constructs a new AvroBytesSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroBytesSchema(AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
    }

    /**
     * Push parent to the stack.
     * Read the length, call onLength
     */
    @Override
    public void pushToStack() {
        this.state.pushToStack(this);
        AvroLongSchema lengthSchema = new AvroLongSchema(
            this.state,
            this::onLength
        );
        lengthSchema.pushToStack();
    }

    /**
     * Length handler.
     *
     * @param length The number of bytes to read.
     */
    private void onLength(Object length) {
        checkType("length", length, Long.class);
        /* Read length number of bytes, call onBytes. */
        AvroFixedSchema bytesSchema = new AvroFixedSchema(
            (Long) length,
            this.state,
            this::onBytes
        );
        bytesSchema.pushToStack();
    }

    /**
     * Bytes handler
     *
     * @param bytes The bytes.
     */
    private void onBytes(Object bytes) {
        checkType("bytes", bytes, List.class);
        /* We're done. */
        this.result = bytes;
        this.done = true;
    }
}
