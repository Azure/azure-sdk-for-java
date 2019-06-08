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
package com.microsoft.azure.cosmos.changefeed.exceptions;

/**
 * Exception occurred during partition split.
 */
public class PartitionSplitException extends PartitionException {
    /**
     * Initializes a new instance of the {@link PartitionSplitException} class using error message and last continuation token.
     * @param message the exception error message.
     * @param lastContinuation the request continuation token.
     */
    public PartitionSplitException(String message, String lastContinuation) {
        super(message, lastContinuation);
    }

    /**
     * Initializes a new instance of the {@link PartitionSplitException} class using error message, the last continuation
     *   token and the inner exception.
     *
     * @param message the exception error message.
     * @param lastContinuation the request continuation token.
     * @param innerException the inner exception.
     */
    public PartitionSplitException(String message, String lastContinuation, Exception innerException) {
        super(message, lastContinuation, innerException);
    }
}
