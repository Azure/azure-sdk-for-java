// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;

/**
 * Code snippets demonstrating various {@link ServiceBusReceiverClient} scenarios.
 */
public class ServiceBusReceiverClientJavaDocCodeSample {
    ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
        .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
            + "SharedAccessKey={key}")
        .receiver()
        .queueName("<< QUEUE NAME >>")
        .buildClient();

    /**
     * Code snippet for creating an ServiceBusReceiverClient
     */
    public void instantiate() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString(
                "Endpoint={servicebus-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildClient();

        // Use the receiver and finally close it.
        receiver.close();
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation
    }

    /**
     * Demonstrates how to create a session receiver for a single, first available session.
     */
    public void sessionReceiverSingleInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession
        ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString(
                "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}")
            .sessionReceiver()
            .queueName("<< QUEUE NAME >>")
            .buildClient();
        ServiceBusReceiverClient receiver = sessionReceiver.acceptNextSession();

        // Use the receiver and finally close it along with the sessionReceiver.
        receiver.close();
        sessionReceiver.close();
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession
    }

    /**
     * Demonstrates how to create a session receiver for a single know session id.
     */
    public void sessionReceiverSessionIdInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId
        ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString(
                "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}")
            .sessionReceiver()
            .queueName("<< QUEUE NAME >>")
            .buildClient();
        ServiceBusReceiverClient receiver = sessionReceiver.acceptSession("<< my-session-id >>");

        // Use the receiver and finally close it along with the sessionReceiver.
        receiver.close();
        sessionReceiver.close();
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId
    }

    /**
     * Demonstrates how to use a transaction.
     */
    public void transactionsSnippet() {
        // Some random sequenceNumber.
        long sequenceNumber = 1000L;
        ServiceBusReceivedMessage receivedMessage = new ServiceBusReceivedMessage(null);

        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext
        ServiceBusTransactionContext transaction = receiver.createTransaction();

        // Process messages and associate operations with the transaction.
        ServiceBusReceivedMessage deferredMessage = receiver.receiveDeferredMessage(sequenceNumber);
        receiver.complete(deferredMessage, new CompleteOptions().setTransactionContext(transaction));
        receiver.abandon(receivedMessage, new AbandonOptions().setTransactionContext(transaction));
        receiver.commitTransaction(transaction);
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext

        receiver.close();
    }
}
