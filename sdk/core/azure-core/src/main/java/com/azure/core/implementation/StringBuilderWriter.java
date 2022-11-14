// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * Implementation of {@link Writer} that write content to a {@link StringBuilder}.
 */
public final class StringBuilderWriter extends Writer {
    private final StringBuilder builder;

    private volatile boolean closed = false;

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
        if (closed) {
            throw new IOException("Writer has been closed.");
        }

        builder.append(c);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        if (closed) {
            throw new IOException("Writer has been closed.");
        }

        builder.append(cbuf);
    }

    @Override
    public void write(String str) throws IOException {
        if (closed) {
            throw new IOException("Writer has been closed.");
        }

        builder.append(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Writer has been closed.");
        }

        builder.append(str, off, len);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        if (closed) {
            throw new IOException("Writer has been closed.");
        }

        builder.append(csq);

        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        if (closed) {
            throw new IOException("Writer has been closed.");
        }

        builder.append(csq, start, end);

        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        if (closed) {
            throw new IOException("Writer has been closed.");
        }

        builder.append(c);

        return this;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Writer has been closed.");
        }

        builder.append(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        if (closed) {
            throw new IOException("Writer has been closed.");
        }
    }

    @Override
    public void close() {
        closed = true;
    }
}
