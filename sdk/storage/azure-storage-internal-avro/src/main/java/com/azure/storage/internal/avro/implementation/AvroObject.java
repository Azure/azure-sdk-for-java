// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

/**
 * A class that represents an Avro Object.
 * A wrapper for the object, the block offset and the object's block index.
 */
public class AvroObject {

    private final long blockOffset;
    private final long eventIndex;
    private final long nextBlockOffset;
    private final long nextEventIndex;
    private final Object object;

    /**
     * Creates an AvroObject.
     * @param blockOffset The offset of the block the object is in.
     * @param eventIndex The index of the object in the block.
     * @param nextBlockOffset The offset of the block the next object is in.
     * @param nextEventIndex The index of the next object in the block.
     * @param object The object.
     */
    public AvroObject(long blockOffset, long eventIndex, long nextBlockOffset, long nextEventIndex,
        Object object) {
        this.blockOffset = blockOffset;
        this.eventIndex = eventIndex;
        this.nextBlockOffset = nextBlockOffset;
        this.nextEventIndex = nextEventIndex;
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
    public long getEventIndex() {
        return eventIndex;
    }

    /**
     * @return The object.
     */
    public Object getObject() {
        return object;
    }

    /**
     * @return The offset of the block the next object is in.
     */
    public long getNextBlockOffset() {
        return nextBlockOffset;
    }

    /**
     * @return The index of the next object in the block.
     */
    public long getNextEventIndex() {
        return nextEventIndex;
    }
}
