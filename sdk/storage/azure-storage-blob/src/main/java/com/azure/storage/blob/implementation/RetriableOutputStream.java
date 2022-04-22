// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation;

import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.BiFunction;

// TODO (kasobol-msft) find right name and place this should live
public class RetriableOutputStream extends OutputStream {
    private static final ClientLogger LOGGER = new ClientLogger(RetriableOutputStream.class);

    private final int maxRetries;
    private final BiFunction<Throwable, Long, StreamResponse> onDownloadErrorResume;
    private final OutputStream target;
    private long position;
    private int retryCount;
    private StreamResponse currentResponse;

    public RetriableOutputStream(
        OutputStream target,
        StreamResponse initialResponse,
        BiFunction<Throwable, Long, StreamResponse> onDownloadErrorResume,
        int maxRetries,
        long position) {
        this.target = target;
        this.currentResponse = initialResponse;
        this.onDownloadErrorResume = onDownloadErrorResume;
        this.maxRetries = maxRetries;
        this.position = position;
    }

    // TODO (kasobol-msft) figure out if all of this should be one class.
    public void transfer() throws IOException {
        while (true) {
            try {
                currentResponse.writeBodyTo(this);
                return;
            } catch (RuntimeException | IOException e) {
                reacquireResponse(e);
            }
        }
    }

    private void reacquireResponse(Exception e) throws IOException {
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

        currentResponse = onDownloadErrorResume.apply(e, position);
    }

    // TODO (kasobol-msft) check if other outputstream methods should be overriden.
    @Override
    public void write(int b) throws IOException {
        target.write(b);
        position += 1;
    }

    @Override
    public void write(byte[] b) throws IOException {
        target.write(b);
        position += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        target.write(b, off, len);
        position += len;
    }

    @Override
    public void close() throws IOException {
        currentResponse.close();
    }
}
