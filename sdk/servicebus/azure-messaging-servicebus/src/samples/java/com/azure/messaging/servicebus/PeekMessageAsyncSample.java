// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.time.Duration;

/**
 * Sample example showing how peek would work.
 */
public class PeekMessageAsyncSample {
    /**
     * Main method to invoke this demo on how to peek at a message within a Service Bus Queue.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
            + "SharedAccessKey={key}";

        // Create a receiver using connection string.
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName("<<queue-name>>")
            .buildAsyncClient();

        receiver.peek().subscribe(
            message -> {
                System.out.println("Received Message Id: " + message.getMessageId());
                System.out.println("Received Message: " + new String(message.getBody()));
            },
            error -> System.err.println("Error occurred while receiving message: " + error),
            () -> System.out.println("Receiving complete."));

        // Subscribe is not a blocking call so we sleep here so the program does not end while finishing
        // the peek operation.
        try {
            Thread.sleep(Duration.ofSeconds(20).toMillis());
        } catch (InterruptedException ignored) {
        }

        // Close the receiver.
        receiver.close();
    }
}
