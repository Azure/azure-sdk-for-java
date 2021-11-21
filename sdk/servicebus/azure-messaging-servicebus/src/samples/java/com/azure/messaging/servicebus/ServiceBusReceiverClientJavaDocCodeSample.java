// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import org.junit.jupiter.api.Test;

/**
 * Code snippets demonstrating various {@link ServiceBusReceiverClient} scenarios.
 */
public class ServiceBusReceiverClientJavaDocCodeSample {
    // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
    // The connectionString/queueName must be set by the application. The 'connectionString' format is shown below.
    // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
    // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
    // 3. "queueName" will be the name of the Service Bus queue instance you created
    //    inside the Service Bus namespace.
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");
    String sessionQueueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SESSION_QUEUE_NAME");

    ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
        .connectionString(connectionString)
        .receiver()
        .queueName(queueName)
        .buildClient();

    /**
     * Code snippet for creating an ServiceBusReceiverClient
     */
    @Test
    public void instantiate() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        // The connectionString/queueName must be set by the application. The 'connectionString' format is shown below.
        // "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
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
        // The connectionString/sessionQueueName must be set by the application. The 'connectionString' format is shown below.
        // "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
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
    @Test
    public void sessionReceiverSessionIdInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId
        // The connectionString/sessionQueueName must be set by the application. The 'connectionString' format is shown below.
        // "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionReceiver()
            .queueName(sessionQueueName)
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
        ServiceBusReceivedMessage receivedMessage = new ServiceBusReceivedMessage((BinaryData) null);

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
