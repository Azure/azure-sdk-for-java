// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates how to asynchronously list sessions using both supported modes. Default listing returns sessions with
 * active messages or stored session state and excludes sessions with neither. A cutoff returns only sessions whose
 * stored state was set or updated after that time.
 */
public class ListSessionsAsyncSample {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String topicName = System.getenv("AZURE_SERVICEBUS_SAMPLE_TOPIC_NAME");
    String subscriptionName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SESSION_SUBSCRIPTION_NAME");

    /**
     * Main method to invoke this demo on how to list session IDs in a Service Bus topic subscription.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is interrupted while waiting for the operation to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        ListSessionsAsyncSample sample = new ListSessionsAsyncSample();
        sample.run();
    }

    /**
     * Lists sessions using both supported modes.
     *
     * @throws InterruptedException If the program is interrupted while waiting for the operation to complete.
     */
    @Test
    public void run() throws InterruptedException {
        CountDownLatch countdownLatch = new CountDownLatch(1);
        ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionReceiver()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildAsyncClient();

        try {
            OffsetDateTime sessionStateUpdatedAfter = OffsetDateTime.now(ZoneOffset.UTC).minusDays(7);

            sessionReceiver.listSessions()
                .doOnNext(sessionId -> System.out.println("Session ID: " + sessionId))
                .thenMany(sessionReceiver.listSessions(sessionStateUpdatedAfter))
                .subscribe(
                    sessionId -> System.out.println("Recently updated session ID: " + sessionId),
                    error -> {
                        System.err.println("Error occurred: " + error);
                        countdownLatch.countDown();
                    },
                    countdownLatch::countDown);

            countdownLatch.await(30, TimeUnit.SECONDS);
        } finally {
            sessionReceiver.close();
        }
    }
}
