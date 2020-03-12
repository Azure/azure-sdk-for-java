// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sending message async.
 */
public class MessageSendAsyncSample {
    /**
     * Main method to invoke this demo on how to send a message to an Azure Event Hub.
     */
    @Test
    public void sendMessage() {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING")
            + ";EntityPath=hemant-test1";
        // Instantiate a client that will be used to call the service.
        ServiceBusSenderAsyncClient senderAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .buildAsyncSenderClient();

        // Create an event to send.
        ServiceBusMessage message = new ServiceBusMessage("Hello world!".getBytes(UTF_8));

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.

        senderAsyncClient.send(message).subscribe();
        try {
            Thread.sleep(1000);
        } catch (Exception ee) {

        }
    }

    /**
     * Main method to invoke this demo on how to send a message to an Azure Event Hub.
     */
    @Test
    public void scheduleMessage() {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Service Bus instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING")
            + ";EntityPath=hemant-test1";
        // Instantiate a client that will be used to call the service.
        ServiceBusSenderAsyncClient senderAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .buildAsyncSenderClient();

        // Create an event to send.
        ServiceBusMessage message = new ServiceBusMessage("RealThing-Scheduled Hello world Track2!! 300 Sec".getBytes(UTF_8));

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.

        senderAsyncClient.scheduleMessage(message, Instant.now().plusSeconds(5*60L)).subscribe();
        try {
            Thread.sleep(10000);
        } catch (Exception ee) {

        }
    }


    /**
     * Main method to invoke this demo on how to cancle a scheduled  message to an Azure Event Hub.
     */
    @Test
    public void cancelScheduleMessage() throws Exception{
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Service Bus instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING")
            + ";EntityPath=hemant-test1";
        // Instantiate a client that will be used to call the service.
        ServiceBusSenderAsyncClient senderAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .buildAsyncSenderClient();

        // Create an event to send.
        ServiceBusMessage message = new ServiceBusMessage(Instant.now().toString()
            + "Hello Track2!! 5 Min, should be cancelled.".getBytes(UTF_8));

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.

        senderAsyncClient.scheduleMessage(message, Instant.now().plusSeconds(5*60L))
            .onErrorContinue((throwable, o) -> {
                System.out.println("Message Scheduled message failed : " + throwable);
            })
            .subscribe(aLong -> {
                System.out.println("Message Scheduled seq Number = " + aLong + " wait small time  .....");
                try{
                    Thread.sleep(30 * 1000); // after  1  minute
                }catch(Exception e) {

                }
                System.out.println("Message Scheduled seq Number = " + aLong + " Now we will cancel it.");
                senderAsyncClient.cancelScheduledMessage(aLong)
                    .doOnSuccess(aVoid -> {
                        System.out.println("Message Scheduled and cancel schedule is done ");
                    })
                    .subscribe();

            });

        try {
            Thread.sleep( 40 * 1000);
        } catch (Exception ee) {

        }
    }

}
