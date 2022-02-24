// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * An {@link InputStream} decorator that tracks the number of bytes read from an inner {@link InputStream} and throws
 * an exception if the number of bytes read doesn't match what was expected.
 */
public final class LengthValidatingInputStream extends InputStream {
    private final InputStream inner;
    private final long expectedReadSize;

    private long currentReadSize;
    private byte lastValidationRead = -2;

    /**
     * Creates a new {@link LengthValidatingInputStream}.
     *
     * @param inputStream The {@link InputStream} being decorated.
     * @param expectedReadSize The expected number of bytes to be read from the inner {@code inputStream}.
     */
    public LengthValidatingInputStream(InputStream inputStream, long expectedReadSize) {
        this.inner = Objects.requireNonNull(inputStream, "'inputStream' cannot be null.");

        if (expectedReadSize < 0) {
            throw new ClientLogger(LengthValidatingInputStream.class)
                .logExceptionAsError(new IllegalArgumentException("'expectedReadSize' cannot be less than 0."));
        }

        this.expectedReadSize = expectedReadSize;
    }

    @Override
    public synchronized int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int readSize = inner.read(b, off, len);
        validateLength(readSize);

        return readSize;
    }

    @Override
    public long skip(long n) throws IOException {
        return inner.skip(n);
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
    }

    @Override
    public synchronized void reset() throws IOException {
        inner.reset();
    }

    @Override
    public boolean markSupported() {
        return inner.markSupported();
    }

    @Override
    public synchronized int read() throws IOException {
        // After each read there is a check for if additional content could be read to determine if an
        // UnexpectedLengthException should be thrown. The check read byte is retained as it will move the read position
        // of the inner InputStream, so when this API is called it needs to be checked.
        //
        // A value of -2 is used to disambiguate between the special -1 returned by InputStream when it has reached its
        // termination. If the retained validation byte isn't -2 return it, otherwise initiate a read from the inner
        // InputStream. Then pass either -1 or 1, based on the read being -1 or not, into the length validation.
        int read = (lastValidationRead != -2) ? lastValidationRead : inner.read();
        validateLength((read == -1) ? -1 : 1);

        return read;
    }

    private void validateLength(int readSize) throws IOException {
        if (readSize == -1) {
            // If the inner InputStream has reached termination validate that the read bytes matches what was expected.
            if (currentReadSize != expectedReadSize) {
                throw new UnexpectedLengthException("Request body emitted " + currentReadSize
                    + " bytes, less than the expected" + expectedReadSize + "bytes.",
                    currentReadSize, expectedReadSize);
            }
        } else {
            currentReadSize += readSize;
            lastValidationRead = (byte) inner.read();

            if (currentReadSize >= expectedReadSize && lastValidationRead != -1) {
                throw new UnexpectedLengthException("Request body emitted " + currentReadSize + 1
                    + " bytes, more than the expected " + expectedReadSize + " bytes.",
                    currentReadSize + 1, expectedReadSize);
            } else if (currentReadSize < expectedReadSize && lastValidationRead == -1) {
                throw new UnexpectedLengthException("Request body emitted " + currentReadSize
                    + " bytes, less than the expected" + expectedReadSize + "bytes.",
                    currentReadSize, expectedReadSize);
            }
        }
    }
}
