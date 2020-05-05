package com.azure.storage.internal.avro.implementation;

public class AvroObject {

    /* TODO (gapra) : With this change we can get rid of AvroNull */
    private final long blockOffset;
    private final long objectBlockIndex;
    private final Object object;

    AvroObject(long blockOffset, long objectBlockIndex, Object object) {
        this.blockOffset = blockOffset;
        this.objectBlockIndex = objectBlockIndex;
        this.object = object;
    }

    public long getBlockOffset() {
        return blockOffset;
    }

    public long getObjectBlockIndex() {
        return objectBlockIndex;
    }

    public Object getObject() {
        return object;
    }
}
