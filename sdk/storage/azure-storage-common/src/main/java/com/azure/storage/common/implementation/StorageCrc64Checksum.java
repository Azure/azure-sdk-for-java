package com.azure.storage.common.implementation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StorageCrc64Checksum implements Checksum {
    private long _uCrc;

    private StorageCrc64Checksum(long uCrc) {
        _uCrc = uCrc;
    }

    public static StorageCrc64Checksum create() {
        return new StorageCrc64Checksum(0L);
    }

    @Override
    public void update(byte[] b, int off, int len) {
        _uCrc = StorageCrc64Calculator.ComputeSlicedSafe(b, off, len, _uCrc);
    }

    @Override
    public byte[] getValue() {
        return getCrcBytes(_uCrc);
    }

    @Override
    public void reset() {
        _uCrc = 0L;
    }

    public static byte[] getCrcBytes(long crc) {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(crc);
        return bb.array();
    }
}
