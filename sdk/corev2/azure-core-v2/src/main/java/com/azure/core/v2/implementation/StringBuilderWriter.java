// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.implementation;

import io.clientcore.core.util.ClientLogger;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * Implementation of {@link Writer} that write content to a {@link StringBuilder}.
 * <p>
 * Given the backing store of this {@link Writer} is a {@link StringBuilder} this is not thread-safe.
 */
public final class StringBuilderWriter extends Writer {
    private static final ClientLogger LOGGER = new ClientLogger(StringBuilderWriter.class);
    private final StringBuilder builder;

    // This can be non-volatile as StringBuilder itself isn't thread-safe.
    private boolean closed = false;

    /**
     * Creates an instance of {@link StringBuilderWriter}.
     *
     * @param builder The {@link StringBuilder} being written to.
     * @throws NullPointerException If {@code builder} is null.
     */
    public StringBuilderWriter(StringBuilder builder) {
        this.builder = Objects.requireNonNull(builder, "'builder' cannot be null.");
    }

    @Override
    public void write(int c) throws IOException {
        ensureOpen();

        builder.append(c);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        ensureOpen();

        builder.append(cbuf);
    }

    @Override
    public void write(String str) throws IOException {
        ensureOpen();

        builder.append(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        ensureOpen();

        builder.append(str, off, len);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        ensureOpen();

        builder.append(csq);

        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        ensureOpen();

        builder.append(csq, start, end);

        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        ensureOpen();

        builder.append(c);

        return this;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        ensureOpen();

        builder.append(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        ensureOpen();
    }

    @Override
    public void close() {
        closed = true;
    }

    /**
     * Returns the string held in the {@link StringBuilder} backing this {@link Writer}
     * for consistency with other Writers.
     * @return builder.toString()
     */
    @Override
    public String toString() {
        return builder.toString();
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw LOGGER.logThrowableAsError(new IOException("Writer has been closed."));
        }
    }
}
