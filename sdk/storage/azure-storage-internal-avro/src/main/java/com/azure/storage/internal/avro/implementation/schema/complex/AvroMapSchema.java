// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.complex;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroCompositeSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroLongSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroStringSchema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Maps are encoded as a series of blocks. Each block consists of a long count value, followed by that many
 * key/value pairs. A block with count zero indicates the end of the map. Each item is encoded per the map's value
 * schema. Map keys are assumed to be strings.
 * If a block's count is negative, its absolute value is used, and the count is followed immediately by a long block
 * size indicating the number of bytes in the block.
 *
 * Long Key Value Key Value Key Value .... Long Key Value Key Value Key Value .... Long(0)
 * If initial Long parsed is negative, it can look like
 * Long(negative) Long Key Value Key Value Key Value ....
 */
public class AvroMapSchema extends AvroCompositeSchema {
    /* TODO (gapra): Look into possibly reducing duplicate code between MapSchema and ArraySchema.
    This may add a little more complexity to the AvroTypes. */

    private final AvroType valueType;
    private Long blockCount;
    private String key;
    private Map<String, Object> ret;

    /**
     * Constructs a new AvroMapSchema.
     *
     * @param valueType The type of values.
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroMapSchema(AvroType valueType, AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
        this.ret = new LinkedHashMap<>();
        this.valueType = valueType;
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);
        /* Read the block size, call onBlockCount. */
        AvroLongSchema blockSchema = new AvroLongSchema(
            this.state,
            this::onBlockCount
        );
        blockSchema.pushToStack();
    }

    /**
     * Block count handler
     *
     * @param blockCount The number of elements in the block.
     */
    private void onBlockCount(Object blockCount) {
        checkType("blockCount", blockCount, Long.class);
        Long bc = (Long) blockCount;
        /* If blockCount = 0 then we're done.*/
        if (bc == 0) {
            this.result = this.ret;
            this.done = true;
            /* If blockCount > 0, read the key, call onKey. */
        } else if (bc > 0) {
            this.blockCount = bc;
            AvroStringSchema keySchema = new AvroStringSchema(
                this.state,
                this::onKey
            );
            keySchema.pushToStack();
            /* If blockCount < 0, use absolute value, read the byteCount, call onByteCount. */
        } else {
            this.blockCount = -bc;
            AvroLongSchema byteCountSchema = new AvroLongSchema(
                this.state,
                this::onByteCount
            );
            byteCountSchema.pushToStack();
        }
    }

    /**
     * Byte count handler.
     *
     * @param byteCount The number of bytes in the block.
     */
    private void onByteCount(Object byteCount) {
        /* Read the key, call onKey. */
        AvroStringSchema keySchema = new AvroStringSchema(
            this.state,
            this::onKey
        );
        keySchema.pushToStack();
    }

    /**
     * Key handler.
     *
     * @param key The key.
     */
    private void onKey(Object key) {
        checkType("key", key, String.class);
        /* Store the key, read the value, call onValue. */
        this.key = (String) key;
        AvroSchema valueSchema = getSchema(
            this.valueType,
            this.state,
            this::onValue
        );
        valueSchema.pushToStack();
    }

    /**
     * Value handler
     *
     * @param value The value.
     */
    private void onValue(Object value) {
        /* Add the key value pair into the map. */
        this.ret.put(this.key, value);

        /* Decrement the block count. */
        this.blockCount--;

        /* If blockCount = 0, there are no more items in the block, read another blockCount and call onBlockCount. */
        if (this.blockCount == 0) {
            AvroLongSchema blockCountSchema = new AvroLongSchema(
                this.state,
                this::onBlockCount
            );
            blockCountSchema.pushToStack();
            /* If blockCount != 0, there are more key/value pairs in the block, read another key and call onKey. */
        } else {
            AvroStringSchema keySchema = new AvroStringSchema(
                this.state,
                this::onKey
            );
            keySchema.pushToStack();
        }
    }
}
