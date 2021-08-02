// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.implementation;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link BinaryDataContent} backed by a file.
 */
public final class FileContent extends BinaryDataContent {
    private static final ClientLogger LOGGER = new ClientLogger(FileContent.class);
    private final Path file;
    private final int chunkSize;
    private final long length;
    private final AtomicReference<byte[]> bytes = new AtomicReference<>();

    /**
     * Creates a new instance of {@link FileContent}.
     *
     * @param file The {@link Path} content.
     * @param chunkSize The requested size for each read of the path.
     * @throws NullPointerException if {@code file} is null.
     * @throws IllegalArgumentException if {@code chunkSize} is less than or equal to zero.
     */
    public FileContent(Path file, int chunkSize) {
        Objects.requireNonNull(file, "'file' cannot be null.");

        if (chunkSize <= 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "'chunkSize' cannot be less than or equal to 0."));
        }
        this.file = file;
        this.chunkSize = chunkSize;
        if (!file.toFile().exists()) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(
                    new FileNotFoundException("File does not exist " + file)));
        }

        this.length = file.toFile().length();
    }

    @Override
    public Long getLength() {
        return this.length;
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
                sink.next(channel.map(FileChannel.MapMode.READ_ONLY, count, readCount));
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

    private byte[] getBytes() {
        try {
            return Files.readAllBytes(file);
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(exception));
        }
    }
}

