// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.io;

import com.azure.core.http.rest.StreamResponse;
import com.azure.core.implementation.AsynchronousFileChannelAdapter;
import com.azure.core.implementation.ByteCountingAsynchronousByteChannel;
import com.azure.core.implementation.logging.LoggingKeys;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Utilities related to IO operations that involve channels, streams, byte transfers.
 */
public final class IOUtils {

    private static final ClientLogger LOGGER = new ClientLogger(IOUtils.class);

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Adapts {@link AsynchronousFileChannel} to {@link AsynchronousByteChannel}.
     * @param fileChannel The {@link AsynchronousFileChannel}.
     * @param position The position in the file to begin writing or reading the {@code content}.
     * @return A {@link AsynchronousByteChannel} that delegates to {@code fileChannel}.
     * @throws NullPointerException When {@code fileChannel} is null.
     * @throws IllegalArgumentException When {@code position} is negative.
     */
    public static AsynchronousByteChannel toAsynchronousByteChannel(
        AsynchronousFileChannel fileChannel, long position) {
        Objects.requireNonNull(fileChannel, "'fileChannel' must not be null");
        if (position < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'position' cannot be less than 0."));
        }
        return new AsynchronousFileChannelAdapter(fileChannel, position);
    }

    /**
     * Transfers bytes from {@link ReadableByteChannel} to {@link WritableByteChannel}.
     * @param source A source {@link ReadableByteChannel}.
     * @param destination A destination {@link WritableByteChannel}.
     * @throws IOException When I/O operation fails.
     * @throws NullPointerException When {@code source} is null.
     * @throws NullPointerException When {@code destination} is null.
     */
    public static void transfer(ReadableByteChannel source, WritableByteChannel destination) throws IOException {
        Objects.requireNonNull(source, "'source' must not be null");
        Objects.requireNonNull(source, "'destination' must not be null");
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
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
     * @param source A source {@link ReadableByteChannel}.
     * @param destination A destination {@link AsynchronousByteChannel}.
     * @return A {@link Mono} that completes when transfer is finished.
     * @throws NullPointerException When {@code source} is null.
     * @throws NullPointerException When {@code destination} is null.
     */
    public static Mono<Void> transferAsync(ReadableByteChannel source, AsynchronousByteChannel destination) {
        Objects.requireNonNull(source, "'source' must not be null");
        Objects.requireNonNull(source, "'destination' must not be null");
        return Mono.create(sink -> sink.onRequest(value -> {
            ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            try {
                transferAsynchronously(source, destination, buffer, sink);
            } catch (IOException e) {
                sink.error(e);
            }
        }));
    }

    private static void transferAsynchronously(
        ReadableByteChannel source, AsynchronousByteChannel destination,
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

                int updatedRetryCount = retryCount + 1;

                if (updatedRetryCount > maxRetries) {
                    LOGGER.atError()
                        .addKeyValue(LoggingKeys.TRY_COUNT_KEY, retryCount)
                        .log(() -> "Retry attempts have been exhausted.", exception);
                    return Mono.error(exception);
                }

                return onErrorResume.apply(exception, targetChannel.getBytesWritten())
                    .flatMap(newResponse -> transferStreamResponseToAsynchronousByteChannelHelper(
                        targetChannel, newResponse,
                        onErrorResume, maxRetries, updatedRetryCount));
            });
    }
}
