// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Stream operations with implementations selected by the Java runtime.
 */
public final class InputStreamUtils {
    private InputStreamUtils() {
    }

    /**
     * Reads the remaining stream content without closing the stream.
     *
     * @param inputStream The stream to read.
     * @return The remaining stream content.
     * @throws IOException If reading fails.
     * @throws NullPointerException If the stream is null.
     */
    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "'inputStream' cannot be null.");
        return StreamUtil.readAllBytes(inputStream);
    }

    /**
     * Transfers the remaining content without closing either stream.
     *
     * @param inputStream The source stream.
     * @param outputStream The destination stream.
     * @throws IOException If reading or writing fails.
     * @throws NullPointerException If either stream is null.
     */
    public static void transferTo(InputStream inputStream, OutputStream outputStream) throws IOException {
        Objects.requireNonNull(inputStream, "'inputStream' cannot be null.");
        Objects.requireNonNull(outputStream, "'outputStream' cannot be null.");
        StreamUtil.transferTo(inputStream, outputStream);
    }

    /**
     * Creates a stream that reads the buffers without changing their positions.
     *
     * @param buffers The buffers to read.
     * @return A stream over the buffers.
     * @throws NullPointerException If the iterable is null.
     */
    public static InputStream createInputStream(Iterable<ByteBuffer> buffers) {
        return new IterableOfByteBuffersInputStream(buffers);
    }
}
