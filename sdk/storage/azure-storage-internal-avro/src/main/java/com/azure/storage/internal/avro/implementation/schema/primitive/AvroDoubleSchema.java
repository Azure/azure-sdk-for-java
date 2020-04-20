package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.util.AvroUtils;

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
public class AvroDoubleSchema extends AvroSchema<Double> {

    /**
     * Constructs a new AvroDoubleSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroDoubleSchema(AvroParserState state, Consumer<Double> onResult) {
        super(state, onResult);
    }

    @Override
    public void add() {
        this.state.push(this);
    }

    @Override
    public void progress() {
        /* Consume 8 bytes. */
        List<ByteBuffer> buffers = this.state.consume(AvroConstants.DOUBLE_SIZE);
        byte[] doubleBytes = AvroUtils.getBytes(buffers);

        /* Long encoded in little endian format. */
        long doubleLong = ByteBuffer.wrap(doubleBytes).order(ByteOrder.LITTLE_ENDIAN).getLong();

        /* Encoded using a method equivalent to doubleToLongBits, then we're done. */
        this.result = Double.longBitsToDouble(doubleLong);
        this.done = true;
    }

    @Override
    public boolean canProgress() {
        /* State must have at least DOUBLE_SIZE bytes to progres on a double. */
        return this.state.contains(AvroConstants.DOUBLE_SIZE);
    }
}
