// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sample example showing how peek would work.
 */
public class PeekMessageAsyncSample {
    private boolean sampleWorks = false;

    /**
     * Main method to invoke this demo on how to peek at a message within a Service Bus Queue.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        PeekMessageAsyncSample sample = new PeekMessageAsyncSample();
        sample.run();
    }

    /**
     * run method to invoke this demo on how to peek at a message within a Service Bus Queue.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    @Test
    public void run() throws InterruptedException {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        //
        // We are reading 'connectionString/queueName' from environment variable. Your application could read it from
        // some other source. The 'connectionString' format is shown below.
        // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // 3. "<<queue-name>>" will be the name of the Service Bus queue instance you created
        //    inside the Service Bus namespace.

        String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
        String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

        // Create a receiver using connection string.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
            .buildAsyncClient();

        receiver.peekMessage().subscribe(
            message -> {
                System.out.println("Received Message Id: " + message.getMessageId());
                System.out.println("Received Message: " + message.getBody().toString());
            },
            error -> System.err.println("Error occurred while receiving message: " + error),
            () -> {
                System.out.println("Receiving complete.");
                sampleWorks = true;
            });

        // Subscribe is not a blocking call so we sleep here so the program does not end while finishing
        // the peek operation.
        TimeUnit.SECONDS.sleep(10);

        // Close the receiver.
        receiver.close();

        // Following assert is for making sure this sample run properly in our automated system.
        // User do not need this assert, you can comment this line
        assertTrue(sampleWorks);
    }
}
