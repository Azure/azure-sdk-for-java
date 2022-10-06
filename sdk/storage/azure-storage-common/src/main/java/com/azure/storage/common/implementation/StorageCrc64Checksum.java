package com.azure.storage.common.implementation;

import java.util.zip.Checksum;

public class StorageCrc64Checksum implements Checksum {
    private long _uCrc;

    private StorageCrc64Checksum(long uCrc) {
        _uCrc = uCrc;
    }

    public static StorageCrc64Checksum create() {
        return new StorageCrc64Checksum(0L);
    }

    @Override
    public void update(int b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(byte[] b, int off, int len) {
        _uCrc = StorageCrc64Calculator.ComputeSlicedSafe(b, off, len, _uCrc);
    }

    @Override
    public long getValue() {
        return _uCrc;
    }

    @Override
    public void reset() {
        _uCrc = 0L;
    }
}
