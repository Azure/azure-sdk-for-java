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
 * a float is written as 4 bytes. The float is converted into a 32-bit integer using a method equivalent to Java's
 * floatToIntBits and then encoded in little-endian format.
 */
public class AvroFloatSchema extends AvroSchema<Float> {

    public AvroFloatSchema(AvroParserState state, Consumer<Float> onResult) {
        super(state, onResult);
    }

    @Override
    public void add() {
        this.state.push(this);
    }

    @Override
    public void progress() {
        /* Get 4 bytes from the cache. */
        List<ByteBuffer> buffers = this.state.consume(AvroConstants.FLOAT_SIZE);
        byte[] floatBytes = AvroUtils.getBytes(buffers);

        /* Integer encoded in little endian format. */
        int floatInt = ByteBuffer.wrap(floatBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

        /* Encoded using a method equivalent to floatToIntBits. */
        Float result = Float.intBitsToFloat(floatInt);

        this.done = true;
        this.result = result;
    }

    @Override
    public boolean canProgress() {
        return this.state.contains(AvroConstants.FLOAT_SIZE);
    }
}
