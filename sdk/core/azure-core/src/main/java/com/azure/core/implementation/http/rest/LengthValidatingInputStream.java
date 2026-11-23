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
 * <p>
 * Bulk reads within the expected length are capped at its remaining bytes; excess bytes are never returned.
 * Once the expected length has been read, excess data is detected without blocking only when the inner
 * stream reports it as {@link InputStream#available() available}, or when the reader reads past the expected length.
 * A reader that stops at the expected length without reading to EOF, as HTTP clients sending a fixed Content-Length
 * may do, can therefore leave unread excess data in a stream that cannot report it without blocking. Probing for it
 * with a blocking read could stall valid streaming bodies whose source isn't closed promptly. The reader must read
 * until EOF to detect a body shorter than the expected length.
 */
final class LengthValidatingInputStream extends InputStream {

    private static final ClientLogger LOGGER = new ClientLogger(LengthValidatingInputStream.class);

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
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'expectedReadSize' cannot be less than 0."));
        }

        this.expectedReadSize = expectedReadSize;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        Objects.requireNonNull(b, "'b' cannot be null.");
        if (off < 0 || len < 0 || off > b.length - len) {
            throw LOGGER.logExceptionAsError(new IndexOutOfBoundsException("Invalid offset or length."));
        }
        if (len == 0) {
            return 0;
        }

        if (position >= expectedReadSize) {
            // The caller requested data at or past the declared boundary (the latter after a previous failure), so a
            // single byte read here is the caller's own read and must observe EOF.
            int read = inner.read();
            validateLength(read == -1 ? -1 : 1);
            return -1;
        }

        // Never request bytes beyond the declared length, so excess bytes never reach the caller's buffer and the read
        // never waits for data past the declared boundary.
        int toRead = (int) Math.min(len, expectedReadSize - position);
        int totalRead = 0;
        int readSize;
        do {
            // Attempt to read until the requested range is filled or the inner stream ends.
            // This results in finishing validation faster and prevents scenarios such as with JDK HttpClient where
            // a large buffer will be requested and it validates read length before we can finish validation.
            readSize = inner.read(b, off + totalRead, toRead - totalRead);
            validateLength(readSize);

            if (readSize != -1) {
                totalRead += readSize;
            } else if (totalRead == 0) {
                // If the inner stream was already read to completion the first read will return -1, need to set
                // total read to -1 to prevent any infinite read loops by returning 0.
                totalRead = -1;
            }
        } while (readSize != -1 && totalRead != toRead);

        if (position == expectedReadSize && inner.available() > 0) {
            // Readers such as HTTP clients may stop at the declared length without reading to EOF. Only probe for
            // excess data when it can be read without blocking, as a blocking probe could stall valid streaming bodies.
            int read = inner.read();
            validateLength(read == -1 ? -1 : 1);
        }

        return totalRead;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        long skipped = inner.skip(n);
        validateLength(skipped);
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return inner.available();
    }

    @Override
    public void close() {
        // No-op. RestProxy length validation can wrap caller-owned InputStreamContent. Closing this wrapper must not
        // close the underlying stream, including between retry attempts where the caller-owned stream needs to be reset.
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

    private void validateLength(long readSize) {
        if (readSize != -1) {
            position = Math.addExact(position, readSize);
        }
        if (position > expectedReadSize) {
            throw LOGGER.logExceptionAsError(new UnexpectedLengthException(
                String.format(BODY_TOO_LARGE, position, expectedReadSize), position, expectedReadSize));
        } else if (readSize == -1 && position < expectedReadSize) {
            throw LOGGER.logExceptionAsError(new UnexpectedLengthException(
                String.format(BODY_TOO_SMALL, position, expectedReadSize), position, expectedReadSize));
        }
    }
}
