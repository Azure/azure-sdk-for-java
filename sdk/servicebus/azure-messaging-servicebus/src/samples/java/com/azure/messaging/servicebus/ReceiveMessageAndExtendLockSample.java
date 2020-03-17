// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public class ReceiveMessageAndExtendLockSample {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(20);

    /**
     * Main method to invoke this demo on how to send an {@link ServiceBusMessage} to an Azure Service Bus
     * Queue or Topic.
     *
     * @param args Unused arguments to the program.
     */
    public static void main1(String[] args) {

        // Arrange
        final int numberOfEvents = 1;

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Creating an Queue instance.
        // 3. Creating a "Shared access policy" for your Queue instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS QUEUE or TOPIC >>";
        ServiceBusReceiverAsyncClient consumer = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .scheduler(Schedulers.elastic())
            .buildAsyncReceiverClient();

        // Act & Assert
        StepVerifier.create(
            consumer.receive()
                .take(numberOfEvents))
            .assertNext(receivedMessage -> {
                AtomicReference<Instant> timeToRefresh = new AtomicReference<>(receivedMessage.getLockedUntil());
                log(" Got message time to refresh in " + receivedMessage.getLockedUntil());
                Disposable renewDisposable = consumer.renewMessageLock(receivedMessage)
                    .repeat(() -> true)
                    .delayElements(Duration.ofSeconds(1))
                    .subscribe(instant -> {
                        log(" New time instant:" + instant);
                        timeToRefresh.set(instant);
                    });

                // processing the messaging
                int count = 0;
                while (count < 15) {
                    ++count;
                    log(count + ". processing message ");
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {

                    }
                }
                log("processing done");
                renewDisposable.dispose();
            }).verifyComplete();

        //wait for receiver to finish processing.
        try {
            Thread.sleep(OPERATION_TIMEOUT.toMillis());
        } catch (InterruptedException ignored) {

        }
    }


    public static void main(String[] args) {

        // Arrange
        final int numberOfEvents = 1;

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Creating an Queue instance.
        // 3. Creating a "Shared access policy" for your Queue instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS QUEUE or TOPIC >>";
        connectionString = "Endpoint=sb://sbtrack2-hemanttest-prototype.servicebus.windows.net/;SharedAccessKeyName=manage;SharedAccessKey=T3wSc5Zp91BC1kw2bnLlNJYiBogrKRe+eBO0ST9ejCY=;EntityPath=hemant-test1";
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .scheduler(Schedulers.elastic())
            .buildAsyncReceiverClient();

        Disposable disposable = receiver
            .receive()
            .doOnNext(message -> {
                log(" Received Message Id :" + message.getMessageId());
                log(" Received Message :" + new String(message.getBody()));

                log(" Got message time to refresh in " + message.getLockedUntil());
                Disposable renewDisposable = receiver.renewMessageLock(message)
                    .repeat(() -> true)
                    .delayElements(Duration.ofSeconds(2))
                    .subscribe(instant -> {
                        log(" New time instant:" + instant);

                    });

                // processing the messaging
                int count = 0;
                while (count < 15) {
                    ++count;
                    log(count + ". processing message ");
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {

                    }
                }
                log("processing done");
               // renewDisposable.dispose();
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
