// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.http.rest;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.azure.core.implementation.http.rest.RestProxyUtils.BODY_TOO_LARGE;
import static com.azure.core.implementation.http.rest.RestProxyUtils.BODY_TOO_SMALL;

/**
 * An {@link InputStream} decorator that tracks the number of bytes read from an inner {@link InputStream} and throws
 * an exception if the number of bytes read doesn't match what was expected.
 */
final class LengthValidatingInputStream extends InputStream {
    private final InputStream inner;
    private final long expectedReadSize;

    private long position;
    private long mark = -1;

    /**
     * Creates a new {@link LengthValidatingInputStream}.
     *
     * @param inputStream The {@link InputStream} being decorated.
     * @param expectedReadSize The expected number of bytes to be read from the inner {@code inputStream}.
     */
    LengthValidatingInputStream(InputStream inputStream, long expectedReadSize) {
        this.inner = Objects.requireNonNull(inputStream, "'inputStream' cannot be null.");

        if (expectedReadSize < 0) {
            throw new ClientLogger(LengthValidatingInputStream.class)
                .logExceptionAsError(new IllegalArgumentException("'expectedReadSize' cannot be less than 0."));
        }

        this.expectedReadSize = expectedReadSize;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int readSize = inner.read(b, off, len);
        validateLength(readSize);

        return readSize;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        long skipped = inner.skip(n);
        position += skipped;
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return inner.available();
    }

    @Override
    public void close() throws IOException {
        inner.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        inner.mark(readlimit);
        mark = position;
    }

    @Override
    public synchronized void reset() throws IOException {
        inner.reset();
        position = mark;
    }

    @Override
    public boolean markSupported() {
        return inner.markSupported();
    }

    @Override
    public synchronized int read() throws IOException {
        int read = inner.read();
        validateLength(read == -1 ? -1 : 1);

        return read;
    }

    private void validateLength(int readSize) {
        if (readSize == -1) {
            // If the inner InputStream has reached termination validate that the read bytes matches what was expected.
            if (position > expectedReadSize) {
                throw new UnexpectedLengthException(String.format(BODY_TOO_LARGE,
                    position, expectedReadSize), position, expectedReadSize);
            } else if (position < expectedReadSize) {
                throw new UnexpectedLengthException(String.format(BODY_TOO_SMALL,
                    position, expectedReadSize), position, expectedReadSize);
            }
        } else {
            position += readSize;
        }
    }
}
