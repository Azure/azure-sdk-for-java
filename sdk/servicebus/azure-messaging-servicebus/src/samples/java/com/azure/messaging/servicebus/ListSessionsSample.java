// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Demonstrates how to list sessions using both supported modes. Default listing returns sessions with active messages
 * or stored session state and excludes sessions with neither. A cutoff returns only sessions whose stored state was set
 * or updated after that time.
 */
public class ListSessionsSample {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SESSION_QUEUE_NAME");

    /**
     * Main method to invoke this demo on how to list session IDs in a Service Bus queue.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        ListSessionsSample sample = new ListSessionsSample();
        sample.run();
    }

    /**
     * Lists sessions using both supported modes.
     */
    @Test
    public void run() {
        ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionReceiver()
            .queueName(queueName)
            .buildClient();

        try {
            listSessionsWithMessagesOrState(sessionReceiver);
            listSessionsWithRecentlyUpdatedState(sessionReceiver);
        } finally {
            sessionReceiver.close();
        }
    }

    private static void listSessionsWithMessagesOrState(ServiceBusSessionReceiverClient sessionReceiver) {
        // Omitting the cutoff returns sessions with active messages or stored session state.
        sessionReceiver.listSessions()
            .forEach(sessionId -> System.out.println("Session ID: " + sessionId));
    }

    private static void listSessionsWithRecentlyUpdatedState(ServiceBusSessionReceiverClient sessionReceiver) {
        // Supplying a cutoff returns only sessions whose stored state was set or updated after that time.
        OffsetDateTime sessionStateUpdatedAfter = OffsetDateTime.now(ZoneOffset.UTC).minusDays(7);
        sessionReceiver.listSessions(sessionStateUpdatedAfter)
            .forEach(sessionId -> System.out.println("Recently updated session ID: " + sessionId));
    }
}
