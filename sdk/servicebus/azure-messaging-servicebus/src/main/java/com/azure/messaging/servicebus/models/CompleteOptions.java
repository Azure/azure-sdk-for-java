// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;

/**
 * Options to specify when completing a {@link ServiceBusReceivedMessage message} received via
 * {@link ServiceBusReceiveMode#PEEK_LOCK}.
 *
 * @see ServiceBusReceiverAsyncClient#complete(ServiceBusReceivedMessage, CompleteOptions)
 * @see ServiceBusReceiverClient#complete(ServiceBusReceivedMessage, CompleteOptions)
 * @see <a href="https://learn.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement#peeklock">
 *     Settling messages</a>
 */
public final class CompleteOptions extends SettlementOptions {
    /**
     * Creates a new instance of options to specify when completing messages.
     */
    public CompleteOptions() {
    }

    /**
     * Sets the {@link ServiceBusTransactionContext} to the options.
     *
     * @param transactionContext The {@link ServiceBusTransactionContext} that will be used to complete a message.
     *
     * @return The updated {@link CompleteOptions} object.
     * @see ServiceBusSenderClient#createTransaction()
     * @see ServiceBusSenderAsyncClient#createTransaction()
     * @see ServiceBusReceiverClient#createTransaction()
     * @see ServiceBusReceiverAsyncClient#createTransaction()
     */
    @Override
    public CompleteOptions setTransactionContext(ServiceBusTransactionContext transactionContext) {
        super.setTransactionContext(transactionContext);
        return this;
    }
}
