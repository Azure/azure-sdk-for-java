/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.internal.changefeed;

import java.io.Closeable;
import java.io.IOException;

/**
 * Signals to a {@link CancellationToken} that it should be canceled..
 */
public class CancellationTokenSource implements Closeable {

    private volatile boolean tokenSourceClosed;
    private volatile boolean cancellationRequested;

    public CancellationTokenSource() {
        this.tokenSourceClosed = false;
        this.cancellationRequested = false;
    }

    public synchronized boolean isCancellationRequested() {
        if (tokenSourceClosed) {
            throw new IllegalStateException("Object already closed");
        }

        return this.cancellationRequested;
    }

    public CancellationToken getToken() {
        return new CancellationToken(this);
    }

    public synchronized void cancel() {
        this.cancellationRequested = true;
    }

    @Override
    public synchronized void close() throws IOException {
        if (tokenSourceClosed) return;
    }
}
