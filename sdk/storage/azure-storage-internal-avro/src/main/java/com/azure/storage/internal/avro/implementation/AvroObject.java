// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

/**
 * A class that represents an Avro Object.
 * A wrapper for the object, the block offset and the object's block index.
 */
public class AvroObject {

    private final long blockOffset;
    private final long objectBlockIndex;
    private final Object object;

    /**
     * Creates an AvroObject.
     * @param blockOffset The offset of the block the object is in.
     * @param objectBlockIndex The index of the object in the block.
     * @param object The object.
     */
    AvroObject(long blockOffset, long objectBlockIndex, Object object) {
        this.blockOffset = blockOffset;
        this.objectBlockIndex = objectBlockIndex;
        this.object = object;
    }

    /**
     * @return The offset of the block the object is in.
     */
    public long getBlockOffset() {
        return blockOffset;
    }

    /**
     * @return The index of the object in the block.
     */
    public long getObjectBlockIndex() {
        return objectBlockIndex;
    }

    /**
     * @return The object.
     */
    public Object getObject() {
        return object;
    }
}
