// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.sendrecv;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.impl.IteratorUtil;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ReceiveParallelManualTest extends ApiTestBase {
    private static final String CONSUMER_GROUP_NAME = TestContext.getConsumerGroupName();

    private static EventHubClient[] ehClient;

    @BeforeClass
    public static void initializeEventHub() throws Exception {
        FileHandler fhc = new FileHandler("c:\\proton-sb-sendbatch-1100.log", false);
        Logger lc1 = Logger.getLogger("servicebus.trace");
        fhc.setFormatter(new SimpleFormatter());
        lc1.addHandler(fhc);
        lc1.setLevel(Level.FINE);

        final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        ehClient = new EventHubClient[4];
        ehClient[0] = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient[1] = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient[2] = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient[3] = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
    }

    @AfterClass()
    public static void cleanup() throws EventHubException {
        for (int i = 0; i < 4; i++) {
            if (ehClient[i] != null) {
                ehClient[i].closeSync();
            }
        }
    }

    // Run this test manually and introduce network failures to test
    // send/receive code is resilient to n/w failures
    // and continues to run once the n/w is back online
    // @Test()
    public void testReceiverStartOfStreamFilters() throws Exception {
        new Thread(new PRunnable("0")).start();
        new Thread(new PRunnable("1")).start();
        new Thread(new PRunnable("2")).start();
        new Thread(new PRunnable("3")).start();
        System.out.println("scheduled receivers");
        System.in.read();
    }

    class PRunnable implements Runnable {
        final String sPartitionId;

        PRunnable(final String sPartitionId) {
            this.sPartitionId = sPartitionId;
        }

        @Override
        public void run() {

            int partitionIdInt = Integer.parseInt(sPartitionId);
            try {
                TestBase.pushEventsToPartition(ehClient[partitionIdInt], sPartitionId, 100).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (EventHubException e) {
                e.printStackTrace();
            }

            PartitionReceiver offsetReceiver1 = null;
            try {
                offsetReceiver1 =
                        ehClient[partitionIdInt].createReceiverSync(CONSUMER_GROUP_NAME, sPartitionId, EventPosition.fromStartOfStream());
            } catch (EventHubException e) {
                e.printStackTrace();
            }

            Iterable<EventData> receivedEvents;
            long totalEvents = 0L;
            while (true) {
                try {
                    receivedEvents = offsetReceiver1.receiveSync(10);
                    if (receivedEvents != null && !IteratorUtil.sizeEquals(receivedEvents, 0)) {

                        long batchSize = (1 + IteratorUtil.getLast(receivedEvents.iterator()).getSystemProperties().getSequenceNumber())
                            - (IteratorUtil.getFirst(receivedEvents).getSystemProperties().getSequenceNumber());
                        totalEvents += batchSize;
                        System.out.println(String.format("[partitionId: %s] received %s events; total sofar: %s, begin: %s, end: %s",
                                sPartitionId,
                                batchSize,
                                totalEvents,
                                IteratorUtil.getLast(receivedEvents.iterator()).getSystemProperties().getSequenceNumber(),
                                IteratorUtil.getFirst(receivedEvents).getSystemProperties().getSequenceNumber()));
                    } else {
                        System.out.println(String.format("received null on partition %s", sPartitionId));
                    }
                } catch (Exception exp) {
                    System.out.println(exp.getMessage() + exp.toString());
                }

                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
