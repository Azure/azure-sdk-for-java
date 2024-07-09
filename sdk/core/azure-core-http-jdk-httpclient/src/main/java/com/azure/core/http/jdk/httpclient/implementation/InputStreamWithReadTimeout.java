// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.SharedExecutorService;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * An {@link InputStream} that wraps an existing {@link InputStream} and provides read operations with a timeout.
 */
public final class InputStreamWithReadTimeout extends InputStream {
    private final Duration readTimeout;
    private final boolean hasTimeout;
    private final InputStream delegate;

    /**
     * Creates an {@link InputStream} that wraps an existing {@link InputStream} and provides read operations with a
     * timeout.
     *
     * @param delegate The {@link InputStream} to wrap.
     * @param readTimeout The read timeout.
     */
    public InputStreamWithReadTimeout(InputStream delegate, Duration readTimeout) {
        this.delegate = delegate;
        this.readTimeout = readTimeout;
        this.hasTimeout = !readTimeout.isNegative() && !readTimeout.isZero();
    }

    @Override
    public int read() throws IOException {
        if (hasTimeout) {
            Future<Integer> readOp = SharedExecutorService.getInstance().submit(() -> delegate.read());
            return getResultWithTimeout(readOp, readTimeout);
        } else {
            return delegate.read();
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (hasTimeout) {
            Future<Integer> readOp = SharedExecutorService.getInstance().submit(() -> delegate.read(b));
            return getResultWithTimeout(readOp, readTimeout);
        } else {
            return delegate.read(b);
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (hasTimeout) {
            Future<Integer> readOp = SharedExecutorService.getInstance().submit(() -> delegate.read(b, off, len));
            return getResultWithTimeout(readOp, readTimeout);
        } else {
            return delegate.read(b, off, len);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        if (hasTimeout) {
            Future<Long> readOp = SharedExecutorService.getInstance().submit(() -> delegate.skip(n));
            return getResultWithTimeout(readOp, readTimeout);
        } else {
            return delegate.skip(n);
        }
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    private static <T> T getResultWithTimeout(Future<T> future, Duration timeout) throws IOException {
        try {
            return CoreUtils.getResultWithTimeout(future, timeout);
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        } catch (TimeoutException e) {
            throw new HttpTimeoutException("Timeout reading response body.");
        }
    }
}
