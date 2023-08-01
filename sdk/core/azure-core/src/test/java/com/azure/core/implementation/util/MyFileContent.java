// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;

/**
 * A file content used for mocking in tests.
 */
public class MyFileContent extends FileContent {
    public MyFileContent(Path file, int chunkSize, long position, long length) {
        super(file, chunkSize, position, length);
    }

    @Override
    public AsynchronousFileChannel openAsynchronousFileChannel() throws IOException {
        return super.openAsynchronousFileChannel();
    }
}
