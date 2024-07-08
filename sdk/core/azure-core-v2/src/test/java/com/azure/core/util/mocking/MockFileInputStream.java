// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.mocking;

import java.io.FileDescriptor;
import java.io.FileInputStream;

/**
 * Implementation of {@link FileInputStream} used for mocking.
 */
public final class MockFileInputStream extends FileInputStream {
    private final byte[] mockData;
    private final int dataLength;
    private final long fileLength;

    private long position = 0L;

    public MockFileInputStream(MockFile mockFile) {
        super(new FileDescriptor());
        this.mockData = mockFile.getData();
        this.dataLength = mockData.length;
        this.fileLength = mockFile.length();
    }

    @Override
    public long skip(long n) {
        if (position >= fileLength) {
            return 0;
        }

        long skipable = Math.min(fileLength - position, n);
        position += skipable;

        return skipable;
    }

    @Override
    public int available() {
        if (fileLength - position > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) Math.max(0, fileLength - position);
        }
    }

    @Override
    public int read() {
        if (position >= fileLength) {
            return -1;
        }

        int value = mockData[(int) (position % dataLength)];
        position++;

        return value;
    }

    @Override
    public int read(byte[] buffer) {
        return read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        if (position >= fileLength) {
            return -1;
        }

        int actualLength = (int) Math.min(length, fileLength - position);
        int bytesOffset = (int) (position % dataLength);

        // Check if we need to wrap back around to the beginning of the bytes.
        if (bytesOffset + actualLength > dataLength) {
            int initial = dataLength - bytesOffset;
            int count = (actualLength - initial) / dataLength;
            int remainder = (actualLength - initial) % dataLength;

            System.arraycopy(mockData, bytesOffset, buffer, offset, initial);

            for (int i = 0; i < count; i++) {
                System.arraycopy(mockData, 0, buffer, offset + initial + (i * dataLength), dataLength);
            }

            if (remainder > 0) {
                System.arraycopy(mockData, 0, buffer, offset + initial + count * dataLength, dataLength);
            }
        } else {
            System.arraycopy(mockData, bytesOffset, buffer, offset, actualLength);
        }

        position += actualLength;
        return actualLength;
    }
}
