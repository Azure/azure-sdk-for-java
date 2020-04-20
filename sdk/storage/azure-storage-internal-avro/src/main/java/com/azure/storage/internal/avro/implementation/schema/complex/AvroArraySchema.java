package com.azure.storage.internal.avro.implementation.schema.complex;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroLongSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;

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
/* TODO (gapra) : Look into possibly reducing duplicate code between MapSchema and ArraySchema.
    This may add a little more complexity to the AvroTypes, so I'm putting it off for now. */
public class AvroArraySchema extends AvroSchema<List<Object>> {

    private final AvroType itemType;
    private Long blockCount;

    /**
     * Constructs a new AvroArraySchema.
     *
     * @param itemType The type of items in the array.
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroArraySchema(AvroType itemType, AvroParserState state, Consumer<List<Object>> onResult) {
        super(state, onResult);
        this.result = new ArrayList<>();
        this.itemType = itemType;
    }

    @Override
    public void add() {
        this.state.push(this);
        /* Read the block size, call onBlockCount. */
        AvroLongSchema blockCountSchema = new AvroLongSchema(
            this.state,
            this::onBlockCount
        );
        blockCountSchema.add();
    }

    /**
     * Block count handler
     *
     * @param blockCount The number of elements in the block.
     */
    private void onBlockCount(Long blockCount) {
        /* If blockCount = 0, then we're done.*/
        if (blockCount == 0) {
            this.done = true;
        /* If blockCount > 0, read the item, call onItem. */
        } else if (blockCount > 0) {
            this.blockCount = blockCount;
            AvroSchema itemSchema = getSchema(
                this.itemType,
                this.state,
                this::onItem
            );
            itemSchema.add();
        /* If blockCount < 0, use absolute value, read the byteCount, call onByteCount. */
        } else {
            this.blockCount = -blockCount;
            AvroLongSchema byteCountSchema = new AvroLongSchema(
                this.state,
                this::onByteCount
            );
            byteCountSchema.add();
        }
    }

    /**
     * Byte count handler.
     *
     * @param ignore The number of bytes in the block.
     */
    private void onByteCount(Long ignore) {
        /* TODO (gapra) : Use this in case we want to skip through the array in the future. Not required for now. */
        /* Read the item, call onItem. */
        AvroSchema itemSchema = getSchema(
            this.itemType,
            this.state,
            this::onItem
        );
        itemSchema.add();
    }

    /**
     * Item handler.
     *
     * @param item The item.
     */
    private void onItem(Object item) {
        /* Add the item to the list. */
        this.result.add(item);

        /* Decrement the block count. */
        this.blockCount--;

        /* If blockCount = 0, there are no more items in the block, read another blockCount and call onBlockCount. */
        if (blockCount == 0) {
            AvroLongSchema blockCountSchema = new AvroLongSchema(
                this.state,
                this::onBlockCount
            );
            blockCountSchema.add();
        /* If blockCount != 0, there are more items in the block, read another item and call onItem. */
        } else {
            AvroSchema itemSchema = getSchema(
                this.itemType,
                this.state,
                this::onItem
            );
            itemSchema.add();
        }
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
