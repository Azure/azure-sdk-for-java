/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.samples.Basic;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class ReceiveUsingOffset {
    public static void main(String[] args)
            throws ServiceBusException, ExecutionException, InterruptedException, IOException {

        final String namespaceName = "----ServiceBusNamespaceName-----";
        final String eventHubName = "----EventHubName-----";
        final String sasKeyName = "-----SharedAccessSignatureKeyName-----";
        final String sasKey = "---SharedAccessSignatureKey----";
        final ConnectionStringBuilder connStr = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);

        final EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(connStr.toString());

        // receiver
        final String partitionId = "0"; // API to get PartitionIds will be released as part of V0.2
        final PartitionReceiver receiver = ehClient.createEpochReceiverSync(
                EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
                partitionId,
                PartitionReceiver.START_OF_STREAM,
                false,
                1);

        try {
            Iterable<EventData> receivedEvents = receiver.receiveSync(100);

            while (true) {
                int batchSize = 0;
                if (receivedEvents != null) {
                    for (EventData receivedEvent : receivedEvents) {
                        if (receivedEvent.getBytes() != null)
                            System.out.println(String.format("Message Payload: %s", new String(receivedEvent.getBytes(), Charset.defaultCharset())));

                        System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s",
                                receivedEvent.getSystemProperties().getOffset(),
                                receivedEvent.getSystemProperties().getSequenceNumber(),
                                receivedEvent.getSystemProperties().getEnqueuedTime()));
                        batchSize++;
                    }
                }

                System.out.println(String.format("ReceivedBatch Size: %s", batchSize));
                receivedEvents = receiver.receiveSync(100);
            }
        } finally {
            // this is paramount; max number of concurrent receiver per consumergroup per partition is 5
            receiver.close().whenComplete(new BiConsumer<Void, Throwable>() {
                public void accept(Void t, Throwable u) {
                    if (u != null) {
                        // wire-up this error to diagnostics infrastructure
                        System.out.println(String.format("closing failed with error: %s", u.toString()));
                    }
                    try {
                        ehClient.closeSync();
                    } catch (ServiceBusException sbException) {
                        // wire-up this error to diagnostics infrastructure
                        System.out.println(String.format("closing failed with error: %s", sbException.toString()));
                    }
                }
            }).get();
        }
    }

    /**
     * actual application-payload, ex: a telemetry event
     */
    static final class PayloadEvent {
        PayloadEvent() {
        }

        public String strProperty;
        public long longProperty;
        public int intProperty;
    }

}
