// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.binarydata;

import io.clientcore.core.models.MockPath;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

/**
 * Implementation of {@link FileBinaryData} used for mocking.
 */
public class MockFileBinaryData extends FileBinaryData {
    private final MockPath mockPath;

    public MockFileBinaryData(MockPath file) {
        this(file, 8192, null, null);
    }

    public MockFileBinaryData(MockPath file, int chunkSize, Long position, Long length) {
        super(file, chunkSize, position, length);

        this.mockPath = file;
    }

    @Override
    protected FileInputStream getFileInputStream() throws FileNotFoundException {
        return new MockFileInputStream(mockPath.toFile());
    }

    @Override
    protected ByteBuffer toByteBufferInternal() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getLength().intValue());
        byte[] buffer = new byte[4096];
        try (InputStream inputStream = getFileInputStream()) {
            int readCount;
            while ((readCount = inputStream.read(buffer)) != -1) {
                byteBuffer.put(buffer, 0, readCount);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        byteBuffer.flip();
        return byteBuffer;
    }
}
