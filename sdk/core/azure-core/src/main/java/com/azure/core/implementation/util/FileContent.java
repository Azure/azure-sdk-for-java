// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

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
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link BinaryDataContent} backed by a file.
 */
public final class FileContent extends BinaryDataContent {
    private static final ClientLogger LOGGER = new ClientLogger(FileContent.class);
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
     * @param size Total number of bytes to be read from the path.
     * @throws NullPointerException if {@code file} is null.
     * @throws IllegalArgumentException If {@code chunkSize} is less than or equal to 0 or {@code position} or
     * {@code size} is non-null and negative.
     */
    public FileContent(Path file, int chunkSize, Long position, Long size) {
        this(validateFile(file), validateChunkSize(chunkSize), validatePosition(position), validateSize(size, file));
    }

    private FileContent(Path file, int chunkSize, long position, long length) {
        this.file = file;
        this.chunkSize = chunkSize;
        this.position = position;
        this.length = length;
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

    private static long validateSize(Long size, Path file) {
        if (size != null && size < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'size' cannot be negative."));
        }

        // If a size has been set use the minimum of the remaining file size and size to determine the length.
        return (size == null) ? file.toFile().length() : Math.min(size, file.toFile().length());
    }

    @Override
    public Long getLength() {
        return this.length;
    }

    public long getPosition() {
        return this.position;
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
            return new BufferedInputStream(new FileInputStream(file.toFile()), chunkSize);
        } catch (FileNotFoundException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException("File not found " + file, e));
        }
    }

    @Override
    public ByteBuffer toByteBuffer() {
        try {
            FileChannel fileChannel = FileChannel.open(file);
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, length);
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(exception));
        }
    }

    @Override
    public Flux<ByteBuffer> toFluxByteBuffer() {
        return Flux.using(() -> FileChannel.open(file), channel -> Flux.generate(() -> 0, (count, sink) -> {
            if (count == length) {
                sink.complete();
                return count;
            }

            int readCount = (int) Math.min(chunkSize, length - count);
            try {
                sink.next(channel.map(FileChannel.MapMode.READ_ONLY, position + count, readCount));
            } catch (IOException ex) {
                sink.error(ex);
            }

            return count + readCount;
        }), channel -> {
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

    private byte[] getBytes() {
        try (FileInputStream stream = new FileInputStream(file.toFile())) {
            int totalSkipCount = 0;
            long currentSkipCount;
            while (totalSkipCount != position) {
                currentSkipCount = stream.skip(position - totalSkipCount);
                if (currentSkipCount == -1) {
                    break;
                }

                totalSkipCount += currentSkipCount;
            }

            byte[] bytes = new byte[(int) length];
            int totalReadCount = 0;
            int currentReadCount;
            while ((currentReadCount = stream.read(bytes, totalReadCount, bytes.length - totalReadCount)) != -1) {
                totalReadCount += currentReadCount;

                // Expected read count has been met, return the byte array as it contains all the content.
                if (totalReadCount == bytes.length) {
                    return bytes;
                }
            }

            // Another check for read count matching expected read count just to be certain.
            if (totalReadCount == bytes.length) {
                return bytes;
            }

            // Otherwise, the bytes need to be resized to what was actually read.
            return Arrays.copyOfRange(bytes, 0, totalReadCount);
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(exception));
        }
    }

    @Override
    public BinaryDataContent copy() {
        // Content is durable and re-playable, return this BinaryDataContent as the copy.
        return new FileContent(file, chunkSize, position, length);
    }
}

