// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.io;

import com.typespec.core.http.rest.StreamResponse;
import com.typespec.core.implementation.AsynchronousFileChannelAdapter;
import com.typespec.core.implementation.ByteCountingAsynchronousByteChannel;
import com.typespec.core.implementation.logging.LoggingKeys;
import com.typespec.core.util.ProgressReporter;
import com.typespec.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Utilities related to IO operations that involve channels, streams, byte transfers.
 */
public final class IOUtils {

    private static final ClientLogger LOGGER = new ClientLogger(IOUtils.class);

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int SIXTY_FOUR_KB = 64 * 1024;
    private static final int THIRTY_TWO_KB = 32 * 1024;
    private static final int MB = 1024 * 1024;
    private static final int GB = 1024 * MB;

    /**
     * Adapts {@link AsynchronousFileChannel} to {@link AsynchronousByteChannel}.
     * @param fileChannel The {@link AsynchronousFileChannel}.
     * @param position The position in the file to begin writing or reading the {@code content}.
     * @return A {@link AsynchronousByteChannel} that delegates to {@code fileChannel}.
     * @throws NullPointerException When {@code fileChannel} is null.
     * @throws IllegalArgumentException When {@code position} is negative.
     */
    public static AsynchronousByteChannel toAsynchronousByteChannel(AsynchronousFileChannel fileChannel,
        long position) {
        Objects.requireNonNull(fileChannel, "'fileChannel' must not be null");
        if (position < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'position' cannot be less than 0."));
        }
        return new AsynchronousFileChannelAdapter(fileChannel, position);
    }

    /**
     * Transfers bytes from {@link ReadableByteChannel} to {@link WritableByteChannel}.
     *
     * @param source A source {@link ReadableByteChannel}.
     * @param destination A destination {@link WritableByteChannel}.
     * @throws IOException When I/O operation fails.
     * @throws NullPointerException When {@code source} or {@code destination} is null.
     */
    public static void transfer(ReadableByteChannel source, WritableByteChannel destination) throws IOException {
        transfer(source, destination, null);
    }

    /**
     * Transfers bytes from {@link ReadableByteChannel} to {@link WritableByteChannel}.
     *
     * @param source A source {@link ReadableByteChannel}.
     * @param destination A destination {@link WritableByteChannel}.
     * @param estimatedSourceSize An estimated size of the source channel, may be null. Used to better determine the
     * size of the buffer used to transfer data in an attempt to reduce read and write calls.
     * @throws IOException When I/O operation fails.
     * @throws NullPointerException When {@code source} or {@code destination} is null.
     */
    public static void transfer(ReadableByteChannel source, WritableByteChannel destination, Long estimatedSourceSize)
        throws IOException {
        if (source == null && destination == null) {
            throw new NullPointerException("'source' and 'destination' cannot be null.");
        } else if (source == null) {
            throw new NullPointerException("'source' cannot be null.");
        } else if (destination == null) {
            throw new NullPointerException("'destination' cannot be null.");
        }

        int bufferSize = (estimatedSourceSize == null) ? getBufferSize(source) : getBufferSize(estimatedSourceSize);
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        int read;
        do {
            buffer.clear();
            read = source.read(buffer);
            buffer.flip();
            while (buffer.hasRemaining()) {
                destination.write(buffer);
            }
        } while (read >= 0);
    }

    /**
     * Transfers bytes from {@link ReadableByteChannel} to {@link AsynchronousByteChannel}.
     *
     * @param source A source {@link ReadableByteChannel}.
     * @param destination A destination {@link AsynchronousByteChannel}.
     * @return A {@link Mono} that completes when transfer is finished.
     * @throws NullPointerException When {@code source} or {@code destination} is null.
     */
    public static Mono<Void> transferAsync(ReadableByteChannel source, AsynchronousByteChannel destination) {
        return transferAsync(source, destination, null);
    }

    /**
     * Transfers bytes from {@link ReadableByteChannel} to {@link AsynchronousByteChannel}.
     *
     * @param source A source {@link ReadableByteChannel}.
     * @param destination A destination {@link AsynchronousByteChannel}.
     * @param estimatedSourceSize An estimated size of the source channel, may be null. Used to better determine the
     * size of the buffer used to transfer data in an attempt to reduce read and write calls.
     * @return A {@link Mono} that completes when transfer is finished.
     * @throws NullPointerException When {@code source} or {@code destination} is null.
     */
    public static Mono<Void> transferAsync(ReadableByteChannel source, AsynchronousByteChannel destination,
        Long estimatedSourceSize) {
        if (source == null && destination == null) {
            return Mono.error(new NullPointerException("'source' and 'destination' cannot be null."));
        } else if (source == null) {
            return Mono.error(new NullPointerException("'source' cannot be null."));
        } else if (destination == null) {
            return Mono.error(new NullPointerException("'destination' cannot be null."));
        }

        int bufferSize = (estimatedSourceSize == null) ? getBufferSize(source) : getBufferSize(estimatedSourceSize);
        return Mono.create(sink -> sink.onRequest(value -> {
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            try {
                transferAsynchronously(source, destination, buffer, sink);
            } catch (IOException e) {
                sink.error(e);
            }
        }));
    }

    private static void transferAsynchronously(ReadableByteChannel source, AsynchronousByteChannel destination,
        ByteBuffer buffer, MonoSink<Void> sink) throws IOException {
        buffer.clear();
        int read = source.read(buffer);
        if (read >= 0) {
            buffer.flip();
            destination.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    try {
                        // This is not a classic recursion.
                        // I.e. it happens in completion handler not on call stack.
                        if (buffer.hasRemaining()) {
                            destination.write(buffer, buffer, this);
                        } else {
                            transferAsynchronously(source, destination, buffer, sink);
                        }
                    } catch (IOException e) {
                        sink.error(e);
                    }
                }

                @Override
                public void failed(Throwable e, ByteBuffer attachment) {
                    sink.error(e);
                }
            });
        } else {
            sink.success();
        }
    }

    /**
     * Transfers the {@link StreamResponse} content to {@link AsynchronousByteChannel}.
     * Resumes the transfer in case of errors.
     *
     * @param targetChannel The destination {@link AsynchronousByteChannel}.
     * @param sourceResponse The initial {@link StreamResponse}.
     * @param onErrorResume A {@link BiFunction} of {@link Throwable} and {@link Long} which is used to resume
     * downloading when an error occurs. The function accepts a {@link Throwable} and offset at the destination
     * from beginning of writing at which the error occurred.
     * @param progressReporter The {@link ProgressReporter}.
     * @param maxRetries The maximum number of times a download can be resumed when an error occurs.
     * @return A {@link Mono} which completion indicates successful transfer.
     */
    public static Mono<Void> transferStreamResponseToAsynchronousByteChannel(
        AsynchronousByteChannel targetChannel,
        StreamResponse sourceResponse,
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume,
        ProgressReporter progressReporter, int maxRetries) {

        return transferStreamResponseToAsynchronousByteChannelHelper(
            new ByteCountingAsynchronousByteChannel(targetChannel, null, progressReporter),
            sourceResponse, onErrorResume, maxRetries, 0);
    }

    private static Mono<Void> transferStreamResponseToAsynchronousByteChannelHelper(
        ByteCountingAsynchronousByteChannel targetChannel,
        StreamResponse response,
        BiFunction<Throwable, Long, Mono<StreamResponse>> onErrorResume,
        int maxRetries, int retryCount) {

        return response.writeValueToAsync(targetChannel)
            .doFinally(ignored -> response.close())
            .onErrorResume(Exception.class, exception -> {
                response.close();

                int updatedRetryCount = retryCount + 1;

                if (updatedRetryCount > maxRetries) {
                    LOGGER.atError()
                        .addKeyValue(LoggingKeys.TRY_COUNT_KEY, retryCount)
                        .log(() -> "Retry attempts have been exhausted.", exception);
                    return Mono.error(exception);
                }

                LOGGER.atInfo().addKeyValue(LoggingKeys.TRY_COUNT_KEY, retryCount)
                    .log(() -> String.format("Using retry attempt %d of %d.", updatedRetryCount, maxRetries),
                        exception);
                return onErrorResume.apply(exception, targetChannel.getBytesWritten())
                    .flatMap(newResponse -> transferStreamResponseToAsynchronousByteChannelHelper(
                        targetChannel, newResponse, onErrorResume, maxRetries, updatedRetryCount));
            });
    }

    /*
     * Helper method to optimize the size of the read buffer to reduce the number of reads and writes that have to be
     * performed. If the source and/or target is IO-based, file, network connection, etc, this reduces the number of
     * calls that may have to be handled by the system.
     */
    private static int getBufferSize(ReadableByteChannel source) {
        if (!(source instanceof SeekableByteChannel)) {
            return DEFAULT_BUFFER_SIZE;
        }

        SeekableByteChannel seekableSource = (SeekableByteChannel) source;
        try {
            long size = seekableSource.size();
            long position = seekableSource.position();

            return getBufferSize(size - position);
        } catch (IOException ex) {
            // Don't let an IOException prevent transfer when we are only trying to gain information.
            return DEFAULT_BUFFER_SIZE;
        }
    }

    private static int getBufferSize(long dataSize) {
        if (dataSize > GB) {
            return SIXTY_FOUR_KB;
        } else if (dataSize > MB) {
            return THIRTY_TWO_KB;
        } else {
            return DEFAULT_BUFFER_SIZE;
        }
    }

    private IOUtils() {
    }
}
