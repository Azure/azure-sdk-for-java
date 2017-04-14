/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.samples.Benchmarks;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/*
 * Performance BenchMark is specific to customers load pattern!!
 *
 * This sample is intended to highlight various variables available to tune latencies
 *
 * Three variables are not exercised in the below Code - are
 * 1) Network proximity
 *      (make sure to run in same region/AzureDataCenter as your target scenario - to get identical results)
 * 2) Throughput units configured on the EventHubs namespace
 *      Latency numbers can only be measured when the target load is <= allowed load
 *      if the Target load (no. of messages per sec or no. of bytes transferred per sec) exceeds the Throughput unit configuration
 *      then you might see a long tail in 999'ile of ~4 secs - which service intentionally delays to slow down the sender
 * 3) Number of Senders per Connection
 *      if you are interested in high throughput scenarios keep this 1 Sender per Connection (that's what the below sample does)
 *
 * If you are running this against an EventHubs Namespace in a shared public instance
 * - results might slightly vary across runs; if you want more predictable results - use Dedicated EventHubs.
 *
 * This program generates 50000 samples of "latency per send call in milliseconds"
 * After the completion of this program - copy the output to an excel sheet & crunch the numbers (like avg, %'s etc)
 */
public class IngressBenchmark {

    public static void main(String[] args)
            throws ServiceBusException, ExecutionException, InterruptedException, IOException {

        final String namespaceName = "----ServiceBusNamespaceName-----";
        final String eventHubName = "----EventHubName-----";
        final String sasKeyName = "-----SharedAccessSignatureKeyName-----";
        final String sasKey = "---SharedAccessSignatureKey----";
        final ConnectionStringBuilder connStr = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);


        // ***************************************************************************************************************
        // List of variables involved
        // 1 - EVENT SIZE
        // 2 - NO OF CONCURRENT SENDS
        // 3 - BATCH SIZE  - aka NO OF EVENTS CLIENTS CAN BATCH & SEND <-- and there by optimize on ACKs returned from the Service (typically, this number is supposed to help bring 2 down)
        // 4 - NO. OF CONNECTIONS <-- This value directly impacts the degree of parallelism while sending
        // ***************************************************************************************************************
        final int EVENT_SIZE = 1024; // 1 kb <-- Change these knobs to determine target throughput; default is set to 5 * 5 * 1024 = 100 KB per SendLatency
        final int NO_OF_CONCURRENT_SENDS = 5;
        final int BATCH_SIZE = 5;

        final int NO_OF_CONNECTIONS = 10;

        // each EventHubClient reserves its own **PHYSICAL SOCKET**
        final EventHubClientPool ehClientPool = new EventHubClientPool(NO_OF_CONNECTIONS, connStr.toString());
        ehClientPool.initialize().get();


        final CompletableFuture<Void>[] sendTasks = new CompletableFuture[NO_OF_CONCURRENT_SENDS];
        for (int perfSample = 0; perfSample < 50000 - NO_OF_CONCURRENT_SENDS + 1; perfSample++) {
            final List<EventData> eventDataList = new LinkedList<>();

            for (int batchSize = 0; batchSize < BATCH_SIZE; batchSize++) {
                final byte[] payload = new byte[EVENT_SIZE];
                Arrays.fill(payload, (byte) 32);
                final EventData eventData = new EventData(payload);
                eventDataList.add(eventData);
            }

            // if a SendTask is complete - replace it with new Sender Task to maintain NO of concurrent sends
            for (int concurrentSends = 0; concurrentSends < NO_OF_CONCURRENT_SENDS; concurrentSends++) {
                boolean isInitializing = (sendTasks[concurrentSends] == null);
                if (isInitializing || sendTasks[concurrentSends].isDone()) {
                    final Instant beforeSend = Instant.now();
                    sendTasks[concurrentSends] = ehClientPool.send(eventDataList).whenComplete(new BiConsumer<Void, Throwable>() {
                        @Override
                        public void accept(Void aVoid, Throwable throwable) {
                            System.out.println(String.format("%s,%s", throwable == null ? "success" : "failure", Duration.between(beforeSend, Instant.now()).toMillis()));
                        }
                    });

                    if (!isInitializing)
                        break;
                }
            }

            // wait for one send to return and proceed to replace it with new SendTask
            // - to control the send-pipe line speed & degree of parallelism
            CompletableFuture.anyOf(sendTasks).get();
        }

        ehClientPool.close().get();
    }
}