// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSimpleSchema;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.function.Consumer;

/**
 * a double is written as 8 bytes. The double is converted into a 64-bit integer using a method equivalent to Java's
 * doubleToLongBits and then encoded in little-endian format.
 *
 * Byte Byte Byte Byte Byte Byte Byte Byte
 */
public class AvroDoubleSchema extends AvroSimpleSchema {

    /**
     * Constructs a new AvroDoubleSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroDoubleSchema(AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);
    }

    @Override
    public void progress() {
        /* Consume 8 bytes. */
        List<ByteBuffer> buffers = this.state.read(AvroConstants.DOUBLE_SIZE);
        byte[] doubleBytes = AvroSchema.getBytes(buffers);

        /* Long encoded in little endian format. */
        long doubleLong = ByteBuffer.wrap(doubleBytes).order(ByteOrder.LITTLE_ENDIAN).getLong();

        /* Encoded using a method equivalent to doubleToLongBits, then we're done. */
        this.result = Double.longBitsToDouble(doubleLong);
        this.done = true;
    }

    @Override
    public boolean canProgress() {
        /* State must have at least DOUBLE_SIZE bytes to progres on a double. */
        return this.state.sizeGreaterThan(AvroConstants.DOUBLE_SIZE);
    }
}
