/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.sendrecv;

import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.IteratorUtil;
import com.microsoft.azure.servicebus.ServiceBusException;

public class ReceiveParallelManualTest extends ApiTestBase
{
	static final String cgName = TestContext.getConsumerGroupName();
	static final String partitionId = "0";
	
	static EventHubClient ehClient;
	
	@BeforeClass
	public static void initializeEventHub()  throws Exception
	{
		FileHandler fhc = new FileHandler("c:\\proton-sb-sendbatch-1100.log", false);
		Logger lc1 = Logger.getLogger("servicebus.trace");
		fhc.setFormatter(new SimpleFormatter());
		lc1.addHandler(fhc);
		lc1.setLevel(Level.FINE);

		final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
		ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString());

	}

	class PRunnable implements Runnable{
        final String sPartitionId;

	    PRunnable(final String sPartitionId) {
	        this.sPartitionId = sPartitionId;
        }
        @Override
        public void run() {

            try {
                TestBase.pushEventsToPartition(ehClient, sPartitionId, 25000).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (ServiceBusException e) {
                e.printStackTrace();
            }

            PartitionReceiver offsetReceiver1 = null;
            try {
                offsetReceiver1 = ehClient.createReceiverSync(cgName, sPartitionId, PartitionReceiver.START_OF_STREAM, false);
            } catch (ServiceBusException e) {
                e.printStackTrace();
            }

            Iterable<EventData> receivedEvents;
            long totalEvents = 0L;
            while (true) {
                try {
                    if ((receivedEvents = offsetReceiver1.receiveSync(10)) != null && !IteratorUtil.sizeEquals(receivedEvents, 0)) {

                        long batchSize = (1 + IteratorUtil.getLast(receivedEvents.iterator()).getSystemProperties().getSequenceNumber()) -
                                (IteratorUtil.getFirst(receivedEvents).getSystemProperties().getSequenceNumber());
                        totalEvents += batchSize;
                        System.out.println(String.format("[partitionId: %s] received %s events; total sofar: %s, begin: %s, end: %s",
                                sPartitionId,
                                batchSize,
                                totalEvents,
                                IteratorUtil.getLast(receivedEvents.iterator()).getSystemProperties().getSequenceNumber(),
                                IteratorUtil.getFirst(receivedEvents).getSystemProperties().getSequenceNumber()));
                    }
                    else {
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

	// Run this test manually and introduce network failures to test
	// send/receive code is resilient to n/w failures 
	// and continues to run once the n/w is back online
	// @Test()
	public void testReceiverStartOfStreamFilters() throws Exception
	{
        new Thread(new PRunnable("0")).start();
        new Thread(new PRunnable("1")).start();
        new Thread(new PRunnable("2")).start();
        new Thread(new PRunnable("3")).start();
		System.in.read();
	}
	
	@AfterClass()
	public static void cleanup() throws ServiceBusException
	{
		if (ehClient != null)
		{
			ehClient.closeSync();
		}
	}
}
