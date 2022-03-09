// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.complex;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroCompositeSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroLongSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Arrays are encoded as a series of blocks. Each block consists of a long count value, followed by that many array
 * items. A block with count zero indicates the end of the array. Each item is encoded per the array's item schema.
 * If a block's count is negative, its absolute value is used, and the count is followed immediately by a long block
 * size indicating the number of bytes in the block.
 *
 * Long Item Item Item .... Long Item Item Item .... Long(0)
 * If initial Long parsed is negative, it can look like
 * Long(negative) Long Item Item Item ....
 */
public class AvroArraySchema extends AvroCompositeSchema {
    /* TODO (gapra) : Look into possibly reducing duplicate code between MapSchema and ArraySchema.
    This may add a little more complexity to the AvroTypes, so I'm putting it off for now. */

    private final AvroType itemType;
    private Long blockCount;
    private List<Object> ret;

    /**
     * Constructs a new AvroArraySchema.
     *
     * @param itemType The type of items in the array.
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroArraySchema(AvroType itemType, AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
        this.ret = new ArrayList<>();
        this.itemType = itemType;
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);
        /* Read the block size, call onBlockCount. */
        AvroLongSchema blockCountSchema = new AvroLongSchema(
            this.state,
            this::onBlockCount
        );
        blockCountSchema.pushToStack();
    }

    /**
     * Block count handler
     *
     * @param blockCount The number of elements in the block.
     */
    private void onBlockCount(Object blockCount) {
        checkType("blockCount", blockCount, Long.class);
        Long bc = (Long) blockCount;
        /* If blockCount = 0, then we're done.*/
        if (bc == 0) {
            this.result = this.ret;
            this.done = true;
            /* If blockCount > 0, read the item, call onItem. */
        } else if (bc > 0) {
            this.blockCount = bc;
            AvroSchema itemSchema = getSchema(
                this.itemType,
                this.state,
                this::onItem
            );
            itemSchema.pushToStack();
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
        /* Read the item, call onItem. */
        AvroSchema itemSchema = getSchema(
            this.itemType,
            this.state,
            this::onItem
        );
        itemSchema.pushToStack();
    }

    /**
     * Item handler.
     *
     * @param item The item.
     */
    private void onItem(Object item) {
        /* Add the item to the list. */
        this.ret.add(item);

        /* Decrement the block count. */
        this.blockCount--;

        /* If blockCount = 0, there are no more items in the block, read another blockCount and call onBlockCount. */
        if (this.blockCount == 0) {
            AvroLongSchema blockCountSchema = new AvroLongSchema(
                this.state,
                this::onBlockCount
            );
            blockCountSchema.pushToStack();
            /* If blockCount != 0, there are more items in the block, read another item and call onItem. */
        } else {
            AvroSchema itemSchema = getSchema(
                this.itemType,
                this.state,
                this::onItem
            );
            itemSchema.pushToStack();
        }
    }
}
