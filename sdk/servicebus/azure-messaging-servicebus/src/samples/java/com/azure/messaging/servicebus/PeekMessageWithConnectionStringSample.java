// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import reactor.core.Disposable;

import java.time.Duration;

/**
 * Sample example showing how peek would work.
 */
public class PeekMessageWithConnectionStringSample {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(20);

    /**
     * Main method to invoke this demo on how to send an {@link ServiceBusMessage} to an Azure Service Bus
     * Queue or Topic.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Creating an Queue instance.
        // 3. Creating a "Shared access policy" for your Queue instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS QUEUE or TOPIC >>";

        // Create a receiver using connection string.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .buildAsyncReceiverClient();

        Disposable disposable = receiver
            .peek()
            .doOnNext(message -> {
                log(" Received Message Id :" + message.getMessageId());
                log(" Received Message :" + new String(message.getBody()));
            })
            .subscribe();

        //wait for receiver to finish processing.
        try {
            Thread.sleep(OPERATION_TIMEOUT.toMillis());
        } catch (InterruptedException ignored) {

        }
        log("Closing the receiver.");
        disposable.dispose();
        log("End!! ");
    }

    private static void log(String message) {
        System.out.println(message);
    }
}
