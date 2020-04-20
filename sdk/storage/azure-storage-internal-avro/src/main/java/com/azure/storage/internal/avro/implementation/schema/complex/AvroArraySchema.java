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
 */
/* TODO : Look into possibly reducing duplicate code between MapSchema and ArraySchema.
    This may add a little more complexity to the AvroTypes. */
public class AvroArraySchema extends AvroSchema<List<Object>> {

    private final AvroType item;
    private Long blockCount;

    public AvroArraySchema(AvroType item, AvroParserState state, Consumer<List<Object>> onResult) {
        super(state, onResult);
        this.result = new ArrayList<>();
        this.item = item;
    }

    /**
     * Read the block size.
     */
    @Override
    public void add() {
        this.state.push(this);
        AvroLongSchema blockCountSchema = new AvroLongSchema(
            this.state,
            this::onBlockCount
        );
        blockCountSchema.add();
    }

    /**
     * Once we read the blockCount,
     * If the block count is zero, that indicates we are done.
     * If the block count is positive, read the item by adding its schema.
     * If the block count is negative, use the absolute value and read the number of bytes in the block.
     * @param blockCount The number of elements in the block.
     */
    private void onBlockCount(Long blockCount) {
        if (blockCount == 0) {
            this.done = true;
        } else if (blockCount > 0) {
            this.blockCount = blockCount;
            AvroSchema itemSchema = getSchema(
                this.item,
                this.state,
                this::onItem
            );
            itemSchema.add();
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
     * Once we read the byteCount
     * ignore the number of bytes in this block for QQ and CF.
     * Read the item by adding its schema.
     */
    private void onByteCount(Long ignore) {
        /* No-op. */
        AvroSchema itemSchema = getSchema(
            this.item,
            this.state,
            this::onItem
        );
        itemSchema.add();
    }

    /**
     * Once we read the item, decrement the blockCount.
     * If the blockCount is 0, read the blockCount,
     * otherwise read another item.
     * @param item The item.
     */
    private void onItem(Object item) {
        this.result.add(item);

        this.blockCount--;

        if (blockCount == 0) {
            AvroLongSchema blockCountSchema = new AvroLongSchema(
                this.state,
                this::onBlockCount
            );
            blockCountSchema.add();
        } else {
            AvroSchema itemSchema = getSchema(
                this.item,
                this.state,
                this::onItem
            );
            itemSchema.add();
        }
    }

    @Override
    public void progress() {
    }

    @Override
    public boolean canProgress() {
        return true;
    }
}
