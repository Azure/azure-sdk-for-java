// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.annotation.Immutable;

/**
 * An option bag to provide configuration required to create an AMQP session.
 */
@Immutable
final public class CreateSessionOptions {
    private final boolean distributedTransactionsSupport;

    /**
     * Constructor to create {@link CreateSessionOptions}.
     * @param distributedTransactionsSupport if AMQP session supports distributed transaction across different entities.
     */
    public CreateSessionOptions(boolean distributedTransactionsSupport) {
        this.distributedTransactionsSupport = distributedTransactionsSupport;
    }

    /**
     * Determine is distributed transactions are supported across different entities.
     * @return true if distributed transactions across different entities are supported.
     */
    public boolean isDistributedTransactionsSupported() {
        return this.distributedTransactionsSupport;
    }
}
