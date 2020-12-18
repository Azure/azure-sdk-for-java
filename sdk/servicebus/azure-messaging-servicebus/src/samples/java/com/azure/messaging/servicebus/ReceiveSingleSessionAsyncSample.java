// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Demonstrates how to receive from the first available session.
 */
public class ReceiveSingleSessionAsyncSample {
    private boolean sampleWorks = true;

    /**
     * Main method to invoke this demo on how to receive messages from the first available session in a Service Bus
     * topic subscription.
     *
     * @param args Unused arguments to the program.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        ReceiveSingleSessionAsyncSample sample = new ReceiveSingleSessionAsyncSample();
        sample.run();
    }

    /**
     * Method to invoke this demo on how to receive messages from the first available session in a Service Bus
     * topic subscription.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    @Test
    public void run() throws InterruptedException {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.

        // We are reading 'connectionString/queueName' from environment variable. Your application could read it from
        // some other source. The 'connectionString' format is shown below.
        // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // 3. "topicName" will be the name of the Service Bus topic instance you created in the Service Bus namespace.
        // 4."subscriptionName" will be the name of the session-enabled subscription.

        String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
        String topicName = System.getenv("AZURE_SERVICEBUS_SAMPLE_TOPIC_NAME");
        String subscriptionName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SESSION_SUBSCRIPTION_NAME");

        // Create a receiver.
        ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionReceiver()
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildAsyncClient();

        // Receiving messages from the first available sessions. It waits up to the AmqpRetryOptions.getTryTimeout().
        // If no session is available within that operation timeout, it completes with an error. Otherwise, a receiver
        // is returned when a lock on the session is acquired.
        Mono<ServiceBusReceiverAsyncClient> receiverMono = sessionReceiver.acceptNextSession();

        // If the session is successfully accepted, begin receiving messages from it.
        // Flux.usingWhen is used to dispose of the receiver after consuming messages completes.
        Disposable subscription = Flux.usingWhen(receiverMono,
            receiver -> receiver.receiveMessages(),
            receiver -> Mono.fromRunnable(() -> receiver.close()))
            .subscribe(message -> {
                // Process message.
                System.out.printf("Session: %s. Sequence #: %s. Contents: %s%n", message.getSessionId(),
                    message.getSequenceNumber(), message.getBody());

                // When this message function completes, the message is automatically completed. If an exception is
                // thrown in here, the message is abandoned.
                // To disable this behaviour, toggle ServiceBusSessionReceiverClientBuilder.disableAutoComplete()
                // when building the session receiver.
            }, error -> {
                System.err.println("Error occurred: " + error);
                sampleWorks = false;
            });

        // Subscribe is not a blocking call so we sleep here so the program does not end.
        TimeUnit.SECONDS.sleep(30);

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        sessionReceiver.close();

        // Following assert is for making sure this sample run properly in our automated system.
        // User do not need this assert, you can comment this line
        assertTrue(sampleWorks);
    }
}
