// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.AsynchronousFileChannelAdapter;

import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;

/**
 * Utilities related to IO operations that involve channels, streams, byte transfers.
 */
public final class IOUtils {

    /**
     * Adapts {@link AsynchronousFileChannel} to {@link AsynchronousByteChannel}.
     * @param fileChannel The {@link AsynchronousFileChannel}.
     * @param position The position in the file to begin writing or reading the {@code content}.
     * @return A {@link AsynchronousByteChannel} that delegates to {@code fileChannel}.
     */
    public static AsynchronousByteChannel toAsynchronousByteChannel(
        AsynchronousFileChannel fileChannel, long position) {
        return new AsynchronousFileChannelAdapter(fileChannel, position);
    }
}
