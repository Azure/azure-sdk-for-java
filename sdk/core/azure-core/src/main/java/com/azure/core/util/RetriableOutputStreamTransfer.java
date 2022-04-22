// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.rest.StreamResponse;
import com.azure.core.implementation.ByteCountingOutputStream;
import com.azure.core.util.logging.ClientLogger;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.BiFunction;

/**
 * A helper class to transfer bytes from response to {@link OutputStream} with ability to resume transfer
 * on transient errors.
 */
public class RetriableOutputStreamTransfer implements Closeable {
    private static final ClientLogger LOGGER = new ClientLogger(RetriableOutputStreamTransfer.class);

    private final int maxRetries;
    private final BiFunction<Throwable, Long, StreamResponse> onErrorResume;
    private final OutputStream target;
    private final long offset;
    private int retryCount;
    private StreamResponse currentResponse;

    /**
     * Creates {@link RetriableOutputStreamTransfer}
     * @param target The stream data is transfered to.
     * @param initialResponse Initial response.
     * @param onErrorResume A response provider that accepts {@link Throwable} and file offset used to resume transfer.
     * @param maxRetries Max number of retries during the transfer.
     * @param offset Initial offset used to calculate retry position.
     */
    public RetriableOutputStreamTransfer(
        OutputStream target,
        StreamResponse initialResponse,
        BiFunction<Throwable, Long, StreamResponse> onErrorResume,
        int maxRetries,
        long offset) {
        this.target = target;
        this.currentResponse = initialResponse;
        this.onErrorResume = onErrorResume;
        this.maxRetries = maxRetries;
        this.offset = offset;
    }

    /**
     * Executes the transfer.
     * @throws IOException when I/O error happens.
     */
    public void execute() throws IOException {
        ByteCountingOutputStream byteCountingOutputStream = new ByteCountingOutputStream(target);
        while (true) {
            try {
                currentResponse.writeBodyTo(byteCountingOutputStream);
                return;
            } catch (RuntimeException | IOException e) {
                reacquireResponse(e, byteCountingOutputStream.getBytesWritten());
            }
        }
    }

    @Override
    public void close() throws IOException {
        currentResponse.close();
    }

    private void reacquireResponse(Exception e, long position) throws IOException {
        currentResponse.close();

        retryCount++;
        if (retryCount > maxRetries) {
            if (e instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) e);
            } else if (e instanceof IOException) {
                throw LOGGER.logIOExceptionAsError((IOException) e);
            } else {
                // This should never happen.
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        currentResponse = onErrorResume.apply(e, offset + position);
    }
}
