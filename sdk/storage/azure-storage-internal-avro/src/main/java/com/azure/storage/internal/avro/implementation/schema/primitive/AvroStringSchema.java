// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroCompositeSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

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
public class AvroStringSchema extends AvroCompositeSchema {

    /**
     * Constructs a new AvroStringSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroStringSchema(AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);

        /* Read the byte, call onBytes. */
        AvroBytesSchema bytesSchema = new AvroBytesSchema(
            this.state,
            this::onBytes
        );
        bytesSchema.pushToStack();
    }

    /**
     * Bytes handler.
     *
     * @param bytes The bytes.
     */
    private void onBytes(Object bytes) {
        checkType("bytes", bytes, List.class);
        /* UTF_8 decode the bytes, then we're done. */
        byte[] str = AvroSchema.getBytes((List<?>) bytes);
        this.result = new String(str, StandardCharsets.UTF_8);
        this.done = true;
    }
}
