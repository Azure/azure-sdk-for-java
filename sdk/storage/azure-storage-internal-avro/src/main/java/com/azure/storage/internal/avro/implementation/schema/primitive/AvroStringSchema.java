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
 */
public class AvroStringSchema extends AvroSchema<String> {

    public AvroStringSchema(AvroParserState state, Consumer<String> onResult) {
        super(state, onResult);
    }

    /**
     * Read the bytes.
     */
    @Override
    public void add() {
        this.state.push(this);
        AvroBytesSchema bytesSchema = new AvroBytesSchema(
            this.state,
            this::onBytes
        );
        bytesSchema.add();
    }

    /**
     * Once we read the bytes, we can UTF-8 decode them and we're done.
     * @param bytes The bytes.
     */
    private void onBytes(List<ByteBuffer> bytes) {
        byte[] str = AvroUtils.getBytes(bytes);
        this.done = true;
        this.result = new String(str, StandardCharsets.UTF_8);
    }

    @Override
    public void progress() {

    }

    @Override
    public boolean canProgress() {
        return true;
    }
}
