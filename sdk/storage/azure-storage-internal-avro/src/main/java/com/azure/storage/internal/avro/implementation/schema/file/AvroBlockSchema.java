package com.azure.storage.internal.avro.implementation.schema.file;

import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.complex.AvroFixedSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroLongSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;
import com.azure.storage.internal.avro.implementation.util.AvroUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A file data block consists of:
 * A long indicating the count of objects in this block.
 * A long indicating the size in bytes of the serialized objects in the current block, after any codec is applied
 * The serialized objects. If a codec is specified, this is compressed by that codec.
 * The file's 16-byte sync marker.
 */
public class AvroBlockSchema extends AvroSchema<Object> {

    private final Consumer<Object> onSchemaObject;
    private final AvroType object;
    private Long blockCount;
    private final byte[] syncMarker;

    public AvroBlockSchema(AvroType object, Consumer<Object> onSchemaObject, byte[] syncMarker, AvroParserState state,
        Consumer<Object> onResult) {
        super(state, onResult);
        this.object = object;
        this.onSchemaObject = onSchemaObject;
        this.syncMarker = syncMarker;
    }

    /**
     * Add LongSchema to read the block count.
     */
    @Override
    public void add() {
        state.push(this);
        AvroLongSchema longSchema = new AvroLongSchema(
            state,
            this::onBlockCount
        );
        longSchema.add();
    }

    /**
     * On reading the block count, store it and read the block size
     * @param blockCount The block count.
     */
    private void onBlockCount(Long blockCount) {
        this.blockCount = blockCount;
        AvroLongSchema longSchema = new AvroLongSchema(
            state,
            this::onBlockSize
        );
        longSchema.add();
    }

    /* Block COunt, Block Size, Obj, Obj, Obj...... Sync marker*/

    /**
     * On reading the block size, ignore it and read an object.
     * @param ignore The block size.
     */
    private void onBlockSize(Long ignore) {
        /* No-op */
        AvroSchema schema = AvroSchema.getSchema(
            this.object,
            this.state,
            this::onObject
        );
        schema.add();
    }

    /**
     * On reading the object, call the object handler and decrement the block count.
     * If the block count is 0, read the sync marker,
     * otherwise read another object.
     * @param schema The object.
     */
    private void onObject(Object schema) {
        this.onSchemaObject.accept(schema);
        this.blockCount--;
        if (this.blockCount == 0) {
            AvroFixedSchema fixedSchema = new AvroFixedSchema(
                AvroConstants.SYNC_MARKER_SIZE,
                this.state,
                this::validateSync
            );
            fixedSchema.add();
        } else {
            AvroSchema record = AvroSchema.getSchema(
                this.object,
                this.state,
                this::onObject
            );
            record.add();
        }
    }

    /**
     * On reading the sync marker, validate it and then we're done.
     * @param sync The sync marker.
     */
    private void validateSync(List<ByteBuffer> sync) {
        byte[] syncBytes = AvroUtils.getBytes(sync);
        if (Arrays.equals(syncBytes, syncMarker)) {
            this.done = true;
            this.result = null;
        } else {
            throw new IllegalArgumentException("Sync marker validation failed.");
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
