// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.utils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Stream operations with implementations selected by the Java runtime.
 */
public final class InputStreamUtils {
    // On Java 18 and later, FileInputStream.transferTo(FileOutputStream) treats Windows pipes as regular files, so it can
    // stop at the content buffered in the pipe or fail.
    private static final boolean WINDOWS = System.getProperty("os.name", "").startsWith("Windows");

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
        // JDK byte-array and file bulk reads can bypass read overrides in subclasses.
        // Keep generic buffering: the JDK default allocates more than ClientCore's existing implementation.
        return inputStream.getClass() == ByteArrayInputStream.class
            || inputStream instanceof ByteBufferInputStream
            || isSeekableFileInputStream(inputStream)
                ? inputStream.readAllBytes()
                : StreamUtil.readAllBytes(inputStream);
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
        boolean fileStreamSubclass
            = (inputStream instanceof FileInputStream && inputStream.getClass() != FileInputStream.class)
                || (outputStream instanceof FileOutputStream && outputStream.getClass() != FileOutputStream.class);
        if (!fileStreamSubclass
            && (inputStream instanceof ByteBufferInputStream || (!WINDOWS && isSeekableFileInputStream(inputStream)))) {
            inputStream.transferTo(outputStream);
        } else {
            // Avoid monitor-held writes and file-channel transfers that bypass subclass overrides.
            StreamUtil.transferTo(inputStream, outputStream);
        }
    }

    /**
     * Creates a stream that reads the buffers without changing their positions.
     *
     * @param buffers The buffers to read.
     * @return A stream over the buffers.
     * @throws NullPointerException If the iterable is null.
     */
    public static InputStream createInputStream(Iterable<ByteBuffer> buffers) {
        return new ByteBufferInputStream(buffers);
    }

    // FileInputStream.readAllBytes() and transferTo(FileOutputStream) seek unconditionally before JDK 24
    // (JDK-8341666), failing for pipes, FIFOs, sockets, and terminals. On those versions skip(0) performs the same seek
    // without consuming data or opening an interruptible FileChannel.
    private static boolean isSeekableFileInputStream(InputStream inputStream) {
        if (inputStream.getClass() != FileInputStream.class) {
            return false;
        }
        try {
            return inputStream.skip(0) == 0;
        } catch (IOException ignored) {
            return false;
        }
    }
}
