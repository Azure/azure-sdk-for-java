// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link BinaryDataContent} backed by a file.
 */
public final class FileContent extends BinaryDataContent {
    private static final ClientLogger LOGGER = new ClientLogger(FileContent.class);
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private final Path file;
    private final int chunkSize;
    private final long position;
    private final long length;
    private final AtomicReference<byte[]> bytes = new AtomicReference<>();

    /**
     * Creates a new instance of {@link FileContent}.
     *
     * @param file The {@link Path} content.
     * @param chunkSize The requested size for each read of the path.
     * @param position Position, or offset, within the path where reading begins.
     * @param length Total number of bytes to be read from the path.
     * @throws NullPointerException if {@code file} is null.
     * @throws IllegalArgumentException if {@code chunkSize} is less than or equal to zero.
     * @throws IllegalArgumentException if {@code position} is less than zero.
     * @throws IllegalArgumentException if {@code length} is less than zero.
     * @throws UncheckedIOException if file doesn't exist.
     */
    public FileContent(Path file, int chunkSize, Long position, Long length) {
        this.file = validateFile(file);
        this.chunkSize = validateChunkSize(chunkSize);
        long fileLength = file.toFile().length();
        this.position = validatePosition(position);
        this.length = validateLength(length, fileLength, this.position);
    }

    private static Path validateFile(Path file) {
        Objects.requireNonNull(file, "'file' cannot be null.");

        if (!file.toFile().exists()) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(
                new FileNotFoundException("File does not exist " + file)));
        }

        return file;
    }

    private static int validateChunkSize(int chunkSize) {
        if (chunkSize <= 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'chunkSize' cannot be less than or equal to 0."));
        }

        return chunkSize;
    }

    private static long validatePosition(Long position) {
        if (position != null && position < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'position' cannot be negative."));
        }

        return (position != null) ? position : 0;
    }

    private static long validateLength(Long length, long fileLength, long position) {
        if (length != null && length < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'length' cannot be negative."));
        }

        long maxAvailableLength = fileLength - position;

        // If a size has been set use the minimum of the remaining file size and size to determine the length.
        return (length == null) ? maxAvailableLength : Math.min(length, maxAvailableLength);
    }

    @Override
    public Long getLength() {
        return this.length;
    }

    public long getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toBytes() {
        byte[] data = this.bytes.get();
        if (data == null) {
            bytes.set(getBytes());
            data = this.bytes.get();
        }
        return data;
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return serializer.deserialize(toStream(), typeReference);
    }

    @Override
    public InputStream toStream() {
        try {
            return new SliceInputStream(
                new BufferedInputStream(new FileInputStream(file.toFile()), chunkSize),
                position, length);
        } catch (FileNotFoundException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException("File not found " + file, e));
        }
    }

    @Override
    public ByteBuffer toByteBuffer() {
        if (length > Integer.MAX_VALUE) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                String.format("'length' cannot be greater than %d when mapping file to ByteBuffer.",
                    Integer.MAX_VALUE)));
        }
        /*
         * A mapping, once established, is not dependent upon the file channel that was used to create it.
         * Closing the channel, in particular, has no effect upon the validity of the mapping.
         */
        try (FileChannel fileChannel = FileChannel.open(file)) {
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, position, length);
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(exception));
        }
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return Flux.using(
            () -> AsynchronousFileChannel.open(file, StandardOpenOption.READ),
            channel -> FluxUtil.readFile(channel, chunkSize, position, length),
            channel -> {
                try {
                    channel.close();
                } catch (IOException ex) {
                    throw LOGGER.logExceptionAsError(Exceptions.propagate(ex));
                }
            });
    }

    /**
     * Gets the file that this content represents.
     *
     * @return The file that this content represents.
     */
    public Path getFile() {
        return file;
    }

    /**
     * Gets the requested size for each read of the path.
     *
     * @return The requested size for each read of the path.
     */
    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public boolean isReplayable() {
        return true;
    }

    private byte[] getBytes() {
        if (length > MAX_ARRAY_SIZE) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                String.format("'length' cannot be greater than %d when buffering content.",
                    MAX_ARRAY_SIZE)));
        }
        try (InputStream is = this.toStream()) {
            byte[] bytes = new byte[(int) length];
            int pendingBytes = bytes.length;
            int offset = 0;
            do {
                // This usually reads in one shot.
                int read = is.read(bytes, offset, pendingBytes);
                if (read >= 0) {
                    pendingBytes -= read;
                    offset += read;
                } else {
                    throw LOGGER.logExceptionAsError(
                        new IllegalStateException("Premature EOF. File was modified concurrently."));
                }
            } while (pendingBytes > 0);
            return bytes;
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(exception));
        }
    }
}

