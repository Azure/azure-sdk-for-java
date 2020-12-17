// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sample demonstrates how to send an {@link ServiceBusMessage} to an Azure Service Bus queue.
 */
public class SendMessageAsyncSample {
    private boolean sampleWorks = false;

    /**
     * Main method to invoke this demo on how to send an {@link ServiceBusMessageBatch} to an Azure Service Bus.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        SendMessageAsyncSample sample = new SendMessageAsyncSample();
        sample.run();
    }

    /**
     * Method to invoke this demo on how to send an {@link ServiceBusMessageBatch} to an Azure Service Bus.
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
        // 3. "queueName" will be the name of the Service Bus queue instance you created
        //    inside the Service Bus namespace.

        String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
        String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        // Create a message to send.
        ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromString("Hello world!"));

        // Send that message. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Service queue or topic. It completes with an error if an exception occurred
        // while sending the message.
        sender.sendMessage(message).subscribe(
            unused -> System.out.println("Sent."),
            error -> System.err.println("Error occurred while publishing message: " + error),
            () -> {
                System.out.println("Send complete.");
                sampleWorks = true;
            });

        // subscribe() is not a blocking call. We sleep here so the program does not end before the send is complete.
        TimeUnit.SECONDS.sleep(5);

        // Close the sender.
        sender.close();

        // Following assert is for making sure this sample run properly in our automated system.
        // User do not need this assert, you can comment this line
        assertTrue(sampleWorks);
    }
}
