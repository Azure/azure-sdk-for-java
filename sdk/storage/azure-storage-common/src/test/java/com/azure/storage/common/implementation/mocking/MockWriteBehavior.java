// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation.mocking;

import com.azure.storage.common.implementation.StorageSeekableByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MockWriteBehavior implements StorageSeekableByteChannel.WriteBehavior {
    @Override
    public void write(ByteBuffer src, long destOffset) throws IOException {

    }

    @Override
    public void commit(long totalLength) {

    }

    @Override
    public void assertCanSeek(long position) {

    }

    @Override
    public void resize(long newSize) {

    }
}
