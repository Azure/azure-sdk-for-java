// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sending message async.
 */
public class MessageSendAsyncSample {
    /**
     * Main method to invoke this demo on how to send a message to an Azure Service Bus.
     */
    @Test
    public void sendMessage() {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderAsyncClient senderAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName("<queue-name>")
            .buildAsyncClient();

        // Create an message to send.
        ServiceBusMessage message = new ServiceBusMessage("Hello world!".getBytes(UTF_8));

        // Send that message. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // message has been delivered to the Service Bus. It completes with an error if an exception occurred while
        // sending the message.

        senderAsyncClient.send(message).subscribe();

        // Subscribe is not a blocking call so we sleep here so the program does not end while finishing
        // the operation.
        try {
            Thread.sleep(Duration.ofSeconds(20).toMillis());
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Main method to invoke this demo on how to send a message to an Azure Service Bus.
     */
    @Test
    public void scheduleMessage() {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderAsyncClient senderAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName("<queue-name")
            .buildAsyncClient();

        // Create an message to send.
        ServiceBusMessage message = new ServiceBusMessage("Hello World!!".getBytes(UTF_8));

        // Send that message. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // message has been delivered to the Service Bus. It completes with an error if an exception occurred while sending
        // the message.

        senderAsyncClient.scheduleMessage(message, Instant.now().plusSeconds(1 * 60L))
            .subscribe(aLong -> {
                System.out.println("!! After schedule message  sequence : " + aLong);
            });

        // Subscribe is not a blocking call so we sleep here so the program does not end while finishing
        // the operation.
        try {
            Thread.sleep(Duration.ofSeconds(20).toMillis());
        } catch (InterruptedException ignored) {
        }

    }


    /**
     * Main method to invoke this demo on how to cancle a scheduled  message to an Azure Service Bus.
     */
    @Test
    public void cancelScheduleMessage() {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderAsyncClient senderAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName("<queue-name>")
            .buildAsyncClient();

        // Create an message to send.
        ServiceBusMessage message = new ServiceBusMessage("Hello Track2!! 1 Min, should be cancelled.".getBytes(UTF_8));

        // Send that message. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // message has been delivered to the Service Bus. It completes with an error if an exception occurred while sending
        // the message.

        senderAsyncClient.scheduleMessage(message, Instant.now().plusSeconds(1 * 60L))
            .onErrorContinue((throwable, o) -> {
                System.out.println("Message Scheduled message failed : " + throwable);
            })
            .subscribe(aLong -> {
                System.out.println("Message Scheduled seq Number = " + aLong + " wait small time  .....");
                try {
                    Thread.sleep(Duration.ofSeconds(10).toMillis());
                } catch (InterruptedException ignored) {
                }

                System.out.println("Message Scheduled seq Number = " + aLong + " Now we will cancel it.");
                senderAsyncClient.cancelScheduledMessage(aLong)
                    .doOnSuccess(aVoid -> {
                        System.out.println("Message Scheduled and cancel schedule is done ");
                    })
                    .subscribe();

            });

        // Subscribe is not a blocking call so we sleep here so the program does not end while finishing
        // the operation.
        try {
            Thread.sleep(Duration.ofSeconds(20).toMillis());
        } catch (InterruptedException ignored) {
        }

    }

}
