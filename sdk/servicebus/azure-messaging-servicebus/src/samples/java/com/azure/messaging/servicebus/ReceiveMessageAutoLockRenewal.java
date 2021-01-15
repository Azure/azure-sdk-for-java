// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import reactor.core.Disposable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates how to enable automatic lock renewal for a message when receiving from Service Bus.
 */
public class ReceiveMessageAutoLockRenewal {
    /**
     * Main method to invoke this demo on how to receive an {@link ServiceBusReceivedMessage} from Service Bus and
     * automatically renew the message lock.
     *
     * @param args Unused arguments to the program.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    public static void main(String[] args) throws InterruptedException {

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
            + "SharedAccessKey={key}";

        // Create a receiver.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        // At most, the receiver will automatically renew the message lock until 120 seconds have elapsed.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName("<<queue-name>>")
            .maxAutoLockRenewDuration(Duration.ofMinutes(2))
            .buildAsyncClient();

        Disposable subscription = receiver.receiveMessages()
            .subscribe(message -> {
                // Process message. The message lock is renewed for up to 2 minutes.
                // If an exception is thrown from this consumer, the message is abandoned. Otherwise, it is completed.
                // Automatic message settlement can be disabled via disableAutoComplete() when creating the receiver
                // client. Consequently, messages have to be manually settled.
                System.out.printf("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(),
                    message.getBody());
            });

        // Subscribe is not a blocking call so we sleep here so the program does not end.
        TimeUnit.SECONDS.sleep(60);

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiver.close();
    }
}
