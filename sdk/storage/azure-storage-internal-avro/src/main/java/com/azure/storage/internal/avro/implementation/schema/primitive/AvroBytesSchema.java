package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.schema.complex.AvroFixedSchema;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;

/**
 * bytes are encoded as a long followed by that many bytes of data.
 *
 * Add a LongSchema to read the length, then add a FixedSchema to read that many bytes.
 * Note: We return a List of ByteBuffer since the number of bytes requested can be long and a single ByteBuffer can
 * only hold Integer.MAX bytes.
 */
public class AvroBytesSchema extends AvroSchema<List<ByteBuffer>> {

    public AvroBytesSchema(AvroParserState state, Consumer<List<ByteBuffer>> onResult) {
        super(state, onResult);
    }

    /**
     * Read the length.
     */
    @Override
    public void add() {
        state.push(this);
        AvroLongSchema lengthSchema = new AvroLongSchema(
            state,
            this::onLength
        );
        lengthSchema.add();
    }

    /**
     * Once we read the length of the bytes, we can read that many bytes.
     * @param length The number of bytes to read.
     */
    private void onLength(Long length) {
        AvroFixedSchema bytesSchema = new AvroFixedSchema(
            length,
            this.state,
            this::onBytes
        );
        bytesSchema.add();
    }

    /**
     * Once we read the bytes, we're done.
     * @param bytes The bytes.
     */
    private void onBytes(List<ByteBuffer> bytes) {
        this.done = true;
        this.result = bytes;
    }

    @Override
    public void progress() {
    }

    @Override
    public boolean canProgress() {
        return true;
    }
}
