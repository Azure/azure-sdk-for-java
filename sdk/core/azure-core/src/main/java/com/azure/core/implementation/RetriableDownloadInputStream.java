// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation;

import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiFunction;

public class RetriableDownloadInputStream extends InputStream {

    private static final ClientLogger LOGGER = new ClientLogger(RetriableDownloadInputStream.class);

    private final int maxRetries;
    private final BiFunction<Throwable, Long, StreamResponse> onDownloadErrorResume;
    private long position;
    private int retryCount;
    private InputStream currentStream;
    private StreamResponse currentResponse;

    public RetriableDownloadInputStream(StreamResponse initialResponse,
                                        BiFunction<Throwable, Long, StreamResponse> onDownloadErrorResume,
                                        int maxRetries,
                                        long position) {
        this.currentResponse = initialResponse;
        this.currentStream = initialResponse.getValueAsBinaryData().toStream();
        this.onDownloadErrorResume = onDownloadErrorResume;
        this.maxRetries = maxRetries;
        this.position = position;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        while (true) {
            try {
                int readSize = currentStream.read(b, off, len);
                position += readSize;
                return readSize;
            } catch (RuntimeException | IOException e) {
                reacquireStream(e);
            }
        }
    }

    @Override
    public int read() throws IOException {
        while (true) {
            try {
                int read = currentStream.read();
                position += read == -1 ? -1 : 1;
                return read;
            } catch (RuntimeException | IOException e) {
                reacquireStream(e);
            }
        }
    }

    private void reacquireStream(Exception e) throws IOException {
        currentStream.close();
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
        currentStream = currentResponse.getValueAsBinaryData().toStream();
    }

    @Override
    public long skip(long n) throws IOException {
        while (true) {
            try {
                long skipped = currentStream.skip(n);
                position += skipped;
                return skipped;
            } catch (RuntimeException | IOException e) {
                reacquireStream(e);
            }
        }
    }

    @Override
    public int available() throws IOException {
        return currentStream.available();
    }

    @Override
    public void close() throws IOException {
        currentStream.close();
        currentResponse.close();
    }
}
