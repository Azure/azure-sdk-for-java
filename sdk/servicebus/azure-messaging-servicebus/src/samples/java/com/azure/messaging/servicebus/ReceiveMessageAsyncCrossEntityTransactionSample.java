// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.CompleteOptions;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * This sample demo how to achieve cross entity transaction with multiple Service Bus entities.
 */
public class ReceiveMessageAsyncCrossEntityTransactionSample {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");

    /**
     * This method is to invoke the demo how to achieve cross entity transaction with multiple Service Bus entities.
     *
     * @param args Unused arguments to the program.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        ReceiveMessageAsyncCrossEntityTransactionSample sample = new ReceiveMessageAsyncCrossEntityTransactionSample();
        sample.run();
    }

    /**
     * This method is to invoke the demo how to achieve cross entity transaction with multiple Service Bus entities.
     * The user scenario is explained below.
     * 1. Send a message to "topic-a". This will trigger the processor and process the message.
     * 2. ProcessorA, which receive from 'topic-a, subscription' , will receive above message and start a transaction
     *    which span multiple entities.
     *    2.a processor client will receive the message from "topic-a" and complete it.
     *    2.c send message to "topic-b".
     * 3. ProcessorA will commit the transaction which ensures that 2.a and 2.b are one unit of work.
     *
     * @throws InterruptedException If the program is unable to sleep while processing message.
     */
    @Test
    public void run() throws InterruptedException {

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        // The 'connectionString' format is shown below.
        // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // 3. "queueName" will be the name of the Service Bus queue instance you created
        //    inside the Service Bus namespace.

        final String topicA = "topic-a";
        final String topicASubscription = "subscription";
        final String topicB = "topic-b";

        final CountDownLatch countdownLatch = new CountDownLatch(1);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage("Received Order#1", messageId);

        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .enableCrossEntityTransactions()
            .connectionString(connectionString);

        // Initialize sender
        final ServiceBusSenderAsyncClient senderAsyncA = builder.sender().topicName(topicA).buildAsyncClient();
        final ServiceBusSenderClient senderSyncB = builder.sender()
            .topicName(topicB).buildClient();

        Consumer<ServiceBusReceivedMessageContext> processMessage = (context) -> {
            ServiceBusReceivedMessage orderMessage = context.getMessage();
            System.out.printf("Processing message. MessageId: %s, Sequence #: %s. Contents: %s %n",
                orderMessage.getMessageId(), orderMessage.getSequenceNumber(), orderMessage.getBody());

            //Start a transaction
            ServiceBusTransactionContext transactionId = senderSyncB.createTransaction();
            context.complete(new CompleteOptions().setTransactionContext(transactionId));
            senderSyncB.sendMessage(new ServiceBusMessage("Ship order#1"), transactionId);
            senderSyncB.commitTransaction(transactionId);
            System.out.printf("Completed transaction for message id %s %n", orderMessage.getMessageId());
            countdownLatch.countDown();
        };

        Consumer<ServiceBusErrorContext> processError = context -> {
            System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'. Error Source: '%s' %n",
                context.getFullyQualifiedNamespace(), context.getEntityPath(), context.getErrorSource());

            if (!(context.getException() instanceof ServiceBusException)) {
                System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
            }
        };

        // Initialize processor client
        final ServiceBusProcessorClient processorA = builder.processor()
            .disableAutoComplete()
            .topicName(topicA)
            .subscriptionName(topicASubscription)
            .processMessage(processMessage)
            .processError(processError)
            .buildProcessorClient();

        // Send a message for processorA to process
        senderAsyncA.sendMessage(message).subscribe();

        System.out.println("Starting the processor");
        processorA.start();

        System.out.println("Listening for 30 seconds...");
        if (countdownLatch.await(30, TimeUnit.SECONDS)) {
            System.out.println("Completed processing successfully.");
        } else {
            System.out.println("Closing clients.");
        }

        // Close all the clients
        processorA.close();
        senderAsyncA.close();
        senderSyncB.close();
    }
}
