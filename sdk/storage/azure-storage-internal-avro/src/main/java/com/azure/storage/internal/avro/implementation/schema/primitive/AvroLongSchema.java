// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSimpleSchema;

import java.util.function.Consumer;

/**
 * long values are written using variable-length zig-zag coding.
 *
 * The equivalent while loop version of the code would look like this.
 *
 * byte b = this.state.consume() & 0xFF;
 * long n = b & 0x7F;
 * long shift = 7;
 * while ((b & 0x80) != 0) {
 *  b = this.state.consume() & 0xFF;
 *  next |= (b & 0x7F) << shift;
 *  shift += 7;
 * }
 * long value = next;
 * return (n >> 1) ^ -(n & 1);
 */
public class AvroLongSchema extends AvroSimpleSchema {

    private long n; /* Keeps track of the number so far. */
    private long shift = 7; /* The current shift value. */
    private boolean first = true; /* Whether or not the first byte has been read yet. (This is to deal with the initial
    code that runs before the while loop in the class level docs.)*/
    private int lastB; /* The last byte read. */

    /**
     * Constructs a new AvroLongSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroLongSchema(AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);
    }

    /**
     * Consuming bytes written into AvroParserState.
     * Please refer to these
     * <a href="https://developers.google.com/protocol-buffers/docs/encoding#types">
     *  Docs</a> for more information.
     */
    @Override
    public void progress() {
        /* Check if done condition is met (This is the negation of the while loop condition).
           If so, return the value. */
        if (!first && (lastB & 0x80) == 0) {
            n = (n >> 1) ^ -(n & 1);
            this.done = true;
            this.result = n;
            return;
        }

        /* Consume a byte. */
        int b = this.state.read() & 0xff;

        /* If this is the first byte, initialize some values. (This is equivalent to the code before the while loop.) */
        if (first) {
            n = b & 0x7F;
            first = false;
            lastB = b;
            /* Check if done condition is met,
               if so return the value,
               otherwise keep making progress on parsing the long. */
            if ((b & 0x80) != 0) {
                return;
            } else {
                n = (n >> 1) ^ -(n & 1);
                this.result = n;
                this.done = true;
            }
        } else {
            /* Keep making progress on parsing the long. */
            n |= (b & 0x7F) << shift;
            shift += 7;
            lastB = b;
        }
    }

    @Override
    public boolean canProgress() {
        /* State must have at least 1 byte to make progress on a variable-sized long. */
        return this.state.sizeGreaterThan(1L);
    }
}
