// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.file;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroCompositeSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;
import com.azure.storage.internal.avro.implementation.schema.complex.AvroFixedSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroLongSchema;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A file data block consists of:
 * A long indicating the count of objects in this block.
 * A long indicating the size in bytes of the serialized objects in the current block.
 * The serialized objects. If a codec is specified, this is compressed by that codec.
 * The file's 16-byte sync marker.
 *
 * Long Long Object Object Object .... SyncMarker
 */
public class AvroBlockSchema extends AvroCompositeSchema {

    private final ClientLogger logger = new ClientLogger(AvroBlockSchema.class);

    private final Consumer<Object> onAvroObject;
    private final AvroType objectType;
    private Long blockCount;
    private final byte[] syncMarker;

    /**
     * Constructs a new AvroBlockSchema.
     *
     * @param objectType The type of object to parse in the block.
     * @param onAvroObject The handler to add the object to the AvroParser's list.
     * @param syncMarker The sync marker to use to validate.
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroBlockSchema(AvroType objectType, Consumer<Object> onAvroObject, byte[] syncMarker, AvroParserState state,
        Consumer<Object> onResult) {
        super(state, onResult);
        this.objectType = objectType;
        this.onAvroObject = onAvroObject;
        this.syncMarker = syncMarker.clone();
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);

        /* Read the block count, call onBlockCount. */
        AvroLongSchema blockCountSchema = new AvroLongSchema(
            this.state,
            this::onBlockCount
        );
        blockCountSchema.pushToStack();
    }

    /**
     * Block count handler.
     *
     * @param blockCount The number of elements in the block.
     */
    private void onBlockCount(Object blockCount) {
        checkType("blockCount", blockCount, Long.class);
        this.blockCount = (Long) blockCount;
        /* Read the block size, call onBlockSize. */
        AvroLongSchema blockSizeSchema = new AvroLongSchema(
            this.state,
            this::onBlockSize
        );
        blockSizeSchema.pushToStack();
    }

    /**
     * Block size handler.
     * On reading the block size, ignore it and read an object.
     * @param blockSize The block size.
     */
    private void onBlockSize(Object blockSize) {
        /* Read the object, call onObject. */
        AvroSchema objectSchema = AvroSchema.getSchema(
            this.objectType,
            this.state,
            this::onObject
        );
        objectSchema.pushToStack();
    }

    /**
     * Object handler.
     *
     * @param schema The object.
     */
    private void onObject(Object schema) {
        /* Call the object handler to store this object in the AvroParser. */
        this.onAvroObject.accept(schema);

        /* Decrement the block count. */
        this.blockCount--;
        /* If blockCount = 0, there are no more items in the block, read the sync marker, call validateSync */
        if (this.blockCount == 0) {
            AvroFixedSchema syncSchema = new AvroFixedSchema(
                AvroConstants.SYNC_MARKER_SIZE,
                this.state,
                this::validateSync
            );
            syncSchema.pushToStack();
            /* If block count != 0, there are more objects in the block, read another object and call onObject. */
        } else {
            AvroSchema objectSchema = AvroSchema.getSchema(
                this.objectType,
                this.state,
                this::onObject
            );
            objectSchema.pushToStack();
        }
    }

    /**
     * Sync marker handler.
     *
     * @param sync The sync marker.
     */
    private void validateSync(Object sync) {
        checkType("sync", sync, List.class);
        /* Validate the sync marker, then we're done. */
        byte[] syncBytes = AvroSchema.getBytes((List<?>) sync);
        if (Arrays.equals(syncBytes, syncMarker)) {
            this.done = true;
            this.result = -1L;
        } else {
            throw logger.logExceptionAsError(new IllegalStateException("Sync marker validation failed."));
        }
    }
}
