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
 *
 * Integer FixedBytes
 */
public class AvroBytesSchema extends AvroSchema<List<ByteBuffer>> {

    /**
     * Constructs a new AvroBytesSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroBytesSchema(AvroParserState state, Consumer<List<ByteBuffer>> onResult) {
        super(state, onResult);
    }

    /**
     * Push parent to the stack.
     * Read the length, call onLength
     */
    @Override
    public void add() {
        this.state.push(this);
        AvroLongSchema lengthSchema = new AvroLongSchema(
            this.state,
            this::onLength
        );
        lengthSchema.add();
    }

    /**
     * Length handler.
     *
     * @param length The number of bytes to read.
     */
    private void onLength(Long length) {
        /* Read length number of bytes, call onBytes. */
        AvroFixedSchema bytesSchema = new AvroFixedSchema(
            length,
            this.state,
            this::onBytes
        );
        bytesSchema.add();
    }

    /**
     * Bytes handler
     *
     * @param bytes The bytes.
     */
    private void onBytes(List<ByteBuffer> bytes) {
        /* We're done. */
        this.result = bytes;
        this.done = true;
    }

    @Override
    public void progress() {
        /* Progress is defined by progress on the sub-type schemas. */
    }

    @Override
    public boolean canProgress() {
        /* Can always make progress since it is defined by the progress on the sub-type schemas. */
        return true;
    }
}
