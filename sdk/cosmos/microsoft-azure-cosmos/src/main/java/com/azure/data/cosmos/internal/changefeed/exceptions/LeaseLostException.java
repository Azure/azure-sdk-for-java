// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
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
    public LeaseLostException() {
    }

    /**
     * Initializes a new instance of the @link LeaseLostException} class using the specified lease.
     *
     * @param lease an instance of a lost lease.
     */
    public LeaseLostException(Lease lease) {
        super(DEFAULT_MESSAGE);
        this.lease = lease;
    }

    /**
     * Initializes a new instance of the @link LeaseLostException} class using error message.
     *
     * @param message the exception error message.
     */
    public LeaseLostException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the @link LeaseLostException} class using error message and inner exception.
     *
     * @param message the exception error message.
     * @param innerException the inner exception.
     *
     */
    public LeaseLostException(String message, Exception innerException) {
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
    public LeaseLostException(Lease lease, Exception innerException, boolean isGone) {
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
