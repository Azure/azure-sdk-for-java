package com.azure.storage.internal.avro.implementation.schema.complex;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroLongSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroStringSchema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Maps are encoded as a series of blocks. Each block consists of a long count value, followed by that many
 * key/value pairs. A block with count zero indicates the end of the map. Each item is encoded per the map's value
 * schema.
 * If a block's count is negative, its absolute value is used, and the count is followed immediately by a long block
 * size indicating the number of bytes in the block.
 */
/* TODO : Look into possibly reducing duplicate code between MapSchema and ArraySchema.
    This may add a little more complexity to the AvroTypes. */
public class AvroMapSchema extends AvroSchema<Map<String, Object>> {

    private final AvroType value;
    private Long blockCount;
    private String key;

    public AvroMapSchema(AvroType value, AvroParserState state, Consumer<Map<String, Object>> onResult) {
        super(state, onResult);
        this.result = new LinkedHashMap<>();
        this.value = value;
    }

    /**
     * Read the block size.
     */
    @Override
    public void add() {
        this.state.push(this);
        AvroLongSchema blockSchema = new AvroLongSchema(
            this.state,
            this::onBlockCount
        );
        blockSchema.add();
    }

    /**
     * Once we read the blockCount,
     * If the block count is zero, that indicates we are done.
     * If the block count is positive, read the key by adding a StringSchema.
     * If the block count is negative, use the absolute value and read the number of bytes in the block.
     * @param blockCount The number of elements in the block.
     */
    private void onBlockCount(Long blockCount) {
        if (blockCount == 0) {
            this.done = true;
        } else if (blockCount > 0) {
            this.blockCount = blockCount;
            AvroStringSchema keySchema = new AvroStringSchema(
                this.state,
                this::onKey
            );
            keySchema.add();
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
     * Read the key by adding a StringSchema.
     */
    private void onByteCount(Long ignore) {
        /* No-op. */
        AvroStringSchema keySchema = new AvroStringSchema(
            this.state,
            this::onKey
        );
        keySchema.add();
    }

    /**
     * Once we read the key, we can store it and read the value by adding its schema.
     * @param key The key.
     */
    private void onKey(String key) {
        this.key = key;
        AvroSchema valueSchema = getSchema(
            this.value,
            this.state,
            this::onValue
        );
        valueSchema.add();
    }

    /**
     * Once we read the value, add the key value pair into the map, decrement the blockCount.
     * If the blockCount is 0, read the blockCount,
     * otherwise read another key.
     * @param value
     */
    private void onValue(Object value) {
        this.result.put(this.key, value);
        this.blockCount--;
        if (this.blockCount == 0) {
            AvroLongSchema blockCountSchema = new AvroLongSchema(
                this.state,
                this::onBlockCount
            );
            blockCountSchema.add();
        } else {
            AvroStringSchema keySchema = new AvroStringSchema(
                this.state,
                this::onKey
            );
            keySchema.add();
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
