// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class StorageCrc64Checksum implements Checksum {
    private long crc;

    private StorageCrc64Checksum(long uCrc) {
        crc = uCrc;
    }

    public static StorageCrc64Checksum create() {
        return new StorageCrc64Checksum(0L);
    }

    @Override
    public void update(byte[] b, int off, int len) {
        crc = StorageCrc64Calculator.computeSlicedSafe(b, off, len, crc);
    }

    @Override
    public byte[] getValue() {
        return getCrcBytes(crc);
    }

    @Override
    public void reset() {
        crc = 0L;
    }

    public static byte[] getCrcBytes(long crc) {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(crc);
        return bb.array();
    }
}
