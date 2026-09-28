// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates how to allow an in-flight message handler to finish before closing a processor.
 * Add a message to the queue before running this sample.
 */
public class ServiceBusProcessorDrainTimeoutSample {
    /**
     * Starts a queue processor and closes it while a message is being processed.
     *
     * @param args Ignored arguments.
     * @throws InterruptedException If the wait for a message is interrupted.
     */
    public static void main(String[] args) throws InterruptedException {
        String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
        String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");
        CountDownLatch messageStarted = new CountDownLatch(1);

        ServiceBusProcessorClient processor = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .processor()
            .queueName(queueName)
            .disableAutoComplete()
            .drainTimeout(Duration.ofSeconds(10))
            .processMessage(context -> {
                System.out.printf("Processing message %s%n", context.getMessage().getMessageId());
                messageStarted.countDown();
                try {
                    // Simulate work that should finish (including settlement) before close() returns.
                    TimeUnit.SECONDS.sleep(2);
                    context.complete();
                    System.out.println("Message completed.");
                } catch (InterruptedException e) {
                    try {
                        context.abandon();
                    } finally {
                        Thread.currentThread().interrupt();
                    }
                }
            })
            .processError(context -> System.err.printf("Processor error: %s%n", context.getException()))
            .buildProcessorClient();

        try {
            processor.start();
            if (!messageStarted.await(30, TimeUnit.SECONDS)) {
                System.out.println("No message arrived within 30 seconds.");
            }
        } finally {
            // Unlike stop(), close() waits up to the configured drain timeout for active handlers.
            // If the timeout expires, shutdown proceeds even if a handler is still running.
            processor.close();
            System.out.println("Processor closed.");
        }
    }
}
