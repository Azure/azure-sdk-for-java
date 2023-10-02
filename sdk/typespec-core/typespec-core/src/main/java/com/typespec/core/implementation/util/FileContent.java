// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.util;

import com.typespec.core.util.FluxUtil;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.serializer.ObjectSerializer;
import com.typespec.core.util.serializer.TypeReference;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A {@link BinaryDataContent} backed by a file.
 */
public class FileContent extends BinaryDataContent {
    private static final ClientLogger LOGGER = new ClientLogger(FileContent.class);
    private final Path file;
    private final int chunkSize;
    private final long position;
    private final long length;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<FileContent, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(FileContent.class, byte[].class, "bytes");

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
        this(validateFile(file), validateChunkSize(chunkSize), validatePosition(position),
            validateLength(length, file.toFile().length(), validatePosition(position)));
    }

    FileContent(Path file, int chunkSize, long position, long length) {
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
        return BYTES_UPDATER.updateAndGet(this, bytes -> bytes == null ? getBytes() : bytes);
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return serializer.deserialize(toStream(), typeReference);
    }

    @Override
    public InputStream toStream() {
        try {
            return new SliceInputStream(new BufferedInputStream(getFileInputStream(), chunkSize), position, length);
        } catch (FileNotFoundException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException("File not found " + file, e));
        }
    }

    protected FileInputStream getFileInputStream() throws FileNotFoundException {
        return new FileInputStream(file.toFile());
    }

    @Override
    public ByteBuffer toByteBuffer() {
        if (length > Integer.MAX_VALUE) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(TOO_LARGE_FOR_BYTE_ARRAY + length));
        }

        return toByteBufferInternal();
    }

    protected ByteBuffer toByteBufferInternal() {
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
        return Flux.using(this::openAsynchronousFileChannel,
            channel -> FluxUtil.readFile(channel, chunkSize, position, length),
            channel -> {
                try {
                    channel.close();
                } catch (IOException ex) {
                    throw LOGGER.logExceptionAsError(Exceptions.propagate(ex));
                }
            });
    }

    protected AsynchronousFileChannel openAsynchronousFileChannel() throws IOException {
        return AsynchronousFileChannel.open(file, StandardOpenOption.READ);
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

    @Override
    public BinaryDataContent toReplayableContent() {
        return this;
    }

    @Override
    public Mono<BinaryDataContent> toReplayableContentAsync() {
        return Mono.just(this);
    }

    @Override
    public BinaryDataContentType getContentType() {
        return BinaryDataContentType.BINARY;
    }

    private byte[] getBytes() {
        if (length > MAX_ARRAY_SIZE) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(TOO_LARGE_FOR_BYTE_ARRAY + length));
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

