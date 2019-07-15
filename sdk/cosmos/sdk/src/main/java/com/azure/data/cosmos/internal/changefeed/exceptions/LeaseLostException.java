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
package com.azure.data.cosmos.internal.changefeed.exceptions;

import com.azure.data.cosmos.internal.changefeed.Lease;

/**
 * Exception occurred when lease is lost, that would typically happen when it is taken by another host.
 *   Other cases: communication failure, number of retries reached, lease not found.
 */
public class LeaseLostException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "The lease was lost.";

    private Lease lease;
    private boolean isGone;

    /**
     * Initializes a new instance of the @link LeaseLostException} class.
     */
    public LeaseLostException()
    {
    }

    /**
     * Initializes a new instance of the @link LeaseLostException} class using the specified lease.
     *
     * @param lease an instance of a lost lease.
     */
    public LeaseLostException(Lease lease)
    {
        super(DEFAULT_MESSAGE);
        this.lease = lease;
    }

    /**
     * Initializes a new instance of the @link LeaseLostException} class using error message.
     *
     * @param message the exception error message.
     */
    public LeaseLostException(String message)
    {
        super(message);
    }

    /**
     * Initializes a new instance of the @link LeaseLostException} class using error message and inner exception.
     *
     * @param message the exception error message.
     * @param innerException the inner exception.
     *
     */
    public LeaseLostException(String message, Exception innerException)
    {
        super(message, innerException.getCause());
    }

    /**
     * Initializes a new instance of the @link LeaseLostException} class using the specified lease, inner exception,
     *   and a flag indicating whether lease is gone..
     *
     * @param lease an instance of a lost lease.
     * @param innerException the inner exception.
     * @param isGone true if lease doesn't exist.
     */
    public LeaseLostException(Lease lease, Exception innerException, boolean isGone)
    {
        super(DEFAULT_MESSAGE, innerException.getCause());
        this.lease = lease;
        this.isGone = isGone;
    }

    /**
     * Gets the lost lease.
     *
     * @return the lost lease.
     */
    public Lease getLease() {
        return this.lease;
    }

    /**
     * Gets a value indicating whether lease doesn't exist.
     *
     * @return true if lease is gone.
     */
    public boolean isGone() {
        return this.isGone;
    }
}
