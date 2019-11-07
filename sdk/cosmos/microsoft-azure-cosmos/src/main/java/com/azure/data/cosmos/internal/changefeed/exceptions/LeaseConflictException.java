// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.exceptions;

import com.azure.data.cosmos.internal.changefeed.Lease;

/**
 * Exception occurred when the lease was updated by a different thread or worker while current thread is trying to update it as well.
 */
public class LeaseConflictException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "The lease was updated by a different worker.";
    private Lease lease;

    /**
     * Initializes a new instance of the @link LeaseConflictException} class.
     */
    public LeaseConflictException() {
    }

    /**
     * Initializes a new instance of the @link LeaseConflictException} class using the specified lease.
     *
     * @param lease an instance of a lost lease.
     */
    public LeaseConflictException(Lease lease) {
        super(DEFAULT_MESSAGE);
        this.lease = lease;
    }

    /**
     * Initializes a new instance of the @link LeaseConflictException} class using the specified lease.
     *
     * @param lease an instance of a lost lease.
     * @param message the exception error message.
     */
    public LeaseConflictException(Lease lease, String message) {
        super(message);
        this.lease = lease;
    }

    /**
     * Initializes a new instance of the @link LeaseConflictException} class using error message.
     *
     * @param message the exception error message.
     */
    public LeaseConflictException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the @link LeaseConflictException} class using error message and inner exception.
     *
     * @param message the exception error message.
     * @param innerException the inner exception.
     *
     */
    public LeaseConflictException(String message, Exception innerException) {
        super(message, innerException.getCause());
    }

    /**
     * Gets the lost lease.
     *
     * @return the lost lease.
     */
    public Lease getLease() {
        return this.lease;
    }
}
