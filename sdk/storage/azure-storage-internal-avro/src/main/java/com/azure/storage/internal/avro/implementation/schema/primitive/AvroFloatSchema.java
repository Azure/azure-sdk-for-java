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
 * a float is written as 4 bytes. The float is converted into a 32-bit integer using a method equivalent to Java's
 * floatToIntBits and then encoded in little-endian format.
 *
 * Byte Byte Byte Byte
 */
public class AvroFloatSchema extends AvroSimpleSchema {

    /**
     * Constructs a new AvroFloatSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroFloatSchema(AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);
    }

    @Override
    public void progress() {
        /* Consume 4 bytes. */
        List<ByteBuffer> buffers = this.state.read(AvroConstants.FLOAT_SIZE);
        byte[] floatBytes = AvroSchema.getBytes(buffers);

        /* Integer encoded in little endian format. */
        int floatInt = ByteBuffer.wrap(floatBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

        /* Encoded using a method equivalent to floatToIntBits, then we're done. */
        this.result = Float.intBitsToFloat(floatInt);
        this.done = true;
    }

    @Override
    public boolean canProgress() {
        /* State must have at least FLOAT_SIZE bytes to progress on a float. */
        return this.state.sizeGreaterThan(AvroConstants.FLOAT_SIZE);
    }
}
