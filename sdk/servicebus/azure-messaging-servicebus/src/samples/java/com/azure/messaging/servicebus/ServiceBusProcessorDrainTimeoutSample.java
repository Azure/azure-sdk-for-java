// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates how to allow an in-flight message handler to finish before closing a processor.
 * Use an empty test queue that is not session-enabled. The sample sends its own message.
 */
public class ServiceBusProcessorDrainTimeoutSample {
    /**
     * Starts a queue processor and closes it while a message is being processed.
     *
     * @param args Ignored arguments.
     * @throws InterruptedException If the wait for a message is interrupted.
     */
    public static void main(String[] args) throws InterruptedException {
        String connectionString = requiredEnvironment("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
        String queueName = requiredEnvironment("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");
        String sampleMessageId = UUID.randomUUID().toString();
        CountDownLatch messageStarted = new CountDownLatch(1);
        CountDownLatch messageCompleted = new CountDownLatch(1);

        ServiceBusProcessorClient processor = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .processor()
            .queueName(queueName)
            .disableAutoComplete()
            // Include all handler work, including retries and delays, plus roughly 25% margin.
            .drainTimeout(Duration.ofSeconds(10))
            .processMessage(context -> {
                System.out.printf("Processing message %s%n", context.getMessage().getMessageId());
                if (sampleMessageId.equals(context.getMessage().getMessageId())) {
                    messageStarted.countDown();
                }
                try {
                    // Simulate work that should finish (including settlement) before close() returns.
                    TimeUnit.SECONDS.sleep(2);
                    // Lock loss can cause redelivery. Make real work idempotent with deduplication checks or upserts:
                    // https://learn.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement
                    context.complete();
                    System.out.println("Message completed.");
                    if (sampleMessageId.equals(context.getMessage().getMessageId())) {
                        messageCompleted.countDown();
                    }
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
            try (ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildClient()) {
                sender.sendMessage(new ServiceBusMessage("Work to finish before shutdown").setMessageId(sampleMessageId));
            }

            processor.start();
            if (!messageStarted.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The sample message handler did not start within 30 seconds.");
            }
        } finally {
            // close() waits up to the configured drain timeout for active handlers.
            // If the timeout expires, shutdown proceeds even if a handler is still running.
            processor.close();
            System.out.println("Processor closed.");
        }
        if (messageCompleted.getCount() != 0) {
            throw new IllegalStateException("The sample message was not settled before processor shutdown.");
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Set " + name + " before running this sample.");
        }
        return value;
    }
}
