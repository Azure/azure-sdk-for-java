package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.util.AvroUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

/**
 * a string is encoded as a long followed by that many bytes of UTF-8 encoded character data.
 *
 * The BytesSchema does most of the same work, so add a BytesSchema to read the bytes, then
 * decode them.
 *
 * Bytes
 */
public class AvroStringSchema extends AvroSchema<String> {

    /**
     * Constructs a new AvroStringSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroStringSchema(AvroParserState state, Consumer<String> onResult) {
        super(state, onResult);
    }

    @Override
    public void add() {
        this.state.push(this);

        /* Read the byte, call onBytes. */
        AvroBytesSchema bytesSchema = new AvroBytesSchema(
            this.state,
            this::onBytes
        );
        bytesSchema.add();
    }

    /**
     * Bytes handler.
     *
     * @param bytes The bytes.
     */
    private void onBytes(List<ByteBuffer> bytes) {
        /* UTF_8 decode the bytes, then we're done. */
        byte[] str = AvroUtils.getBytes(bytes);
        this.result = new String(str, StandardCharsets.UTF_8);
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
