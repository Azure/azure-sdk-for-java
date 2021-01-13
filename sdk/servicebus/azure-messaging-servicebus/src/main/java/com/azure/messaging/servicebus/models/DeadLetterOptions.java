// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;

import java.util.Map;

/**
 * Options to specify while putting message in dead-letter queue.
 *
 * @see ServiceBusReceiverAsyncClient#deadLetter(ServiceBusReceivedMessage, DeadLetterOptions)
 * @see ServiceBusReceiverClient#deadLetter(ServiceBusReceivedMessage, DeadLetterOptions)
 */
public final class DeadLetterOptions extends SettlementOptions {
    private String deadLetterReason;
    private String deadLetterErrorDescription;
    private Map<String, Object> propertiesToModify;

    /**
     * Sets the reason while putting message in dead letter sub-queue.
     *
     * @param deadLetterReason while putting message in dead letter sub-queue.
     *
     * @return The updated {@link DeadLetterOptions} object.
     */
    public DeadLetterOptions setDeadLetterReason(String deadLetterReason) {
        this.deadLetterReason = deadLetterReason;
        return this;
    }

    /**
     * Sets the error description while putting message in dead letter sub-queue.
     *
     * @param deadLetterErrorDescription while putting message in dead letter sub-queue.
     *
     * @return The updated {@link DeadLetterOptions} object.
     */
    public DeadLetterOptions setDeadLetterErrorDescription(String deadLetterErrorDescription) {
        this.deadLetterErrorDescription = deadLetterErrorDescription;
        return this;
    }

    /**
     * Sets the message properties to modify while putting message in dead letter sub-queue.
     *
     * @param propertiesToModify Message properties to modify.
     *
     * @return The updated {@link DeadLetterOptions} object.
     */
    public DeadLetterOptions setPropertiesToModify(Map<String, Object> propertiesToModify) {
        this.propertiesToModify = propertiesToModify;
        return this;
    }

    /**
     * Gets the reason for putting put message in dead letter sub-queue.
     *
     * @return The reason for putting put message in dead letter sub-queue.
     */
    public String getDeadLetterReason() {
        return deadLetterReason;
    }

    /**
     * Gets the error description for putting put message in dead letter sub-queue.
     *
     * @return The error description to for putting message in dead letter sub-queue.
     */
    public String getDeadLetterErrorDescription() {
        return deadLetterErrorDescription;
    }

    /**
     * Gets the message properties to modify while putting put message in dead letter sub-queue.
     *
     * @return The message properties to modify while putting message in dead letter sub-queue.
     */
    public Map<String, Object> getPropertiesToModify() {
        return propertiesToModify;
    }

    /**
     * Sets the {@link ServiceBusTransactionContext} to the options.
     *
     * @param transactionContext The {@link ServiceBusTransactionContext} that will be used to dead letter a message.
     *
     * @return The updated {@link DeadLetterOptions} object.
     *
     * @see ServiceBusSenderClient#createTransaction()
     * @see ServiceBusSenderAsyncClient#createTransaction()
     * @see ServiceBusReceiverClient#createTransaction()
     * @see ServiceBusReceiverAsyncClient#createTransaction()
     */
    @Override
    public DeadLetterOptions setTransactionContext(ServiceBusTransactionContext transactionContext) {
        super.setTransactionContext(transactionContext);
        return this;
    }
}
