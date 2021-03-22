package com.azure.messaging.servicebus.implementation;

import com.azure.core.annotation.Immutable;

/**
 * An option bag to provide configuration required to create an {@link ServiceBusReactorSession}.
 */
@Immutable
final public class ServiceBusCreateSessionOptions {
    private final boolean distributedTransactionsSupport;

    /**
     * Constructor to create {@link ServiceBusCreateSessionOptions}.
     * @param distributedTransactionsSupport if session supports distributed transaction across different entities.
     */
    public ServiceBusCreateSessionOptions(boolean distributedTransactionsSupport) {
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
