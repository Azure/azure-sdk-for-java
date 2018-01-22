/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.exceptioncontracts;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.eventhubs.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.eventhubs.EventHubException;

public class SendLargeMessageTest extends ApiTestBase
{
	static ConnectionStringBuilder connStr;
	static String partitionId = "0";
	
	static EventHubClient ehClient;
	static PartitionSender sender;
	
	static EventHubClient receiverHub;
	static PartitionReceiver receiver;
	
	@BeforeClass
	public static void initializeEventHub()  throws Exception
	{
		connStr = TestContext.getConnectionString();
	
		ehClient = EventHubClient.createFromConnectionString(connStr.toString(), TestContext.EXECUTOR_SERVICE).get();
		sender = ehClient.createPartitionSender(partitionId).get();
		
		receiverHub = EventHubClient.createFromConnectionString(connStr.toString(), TestContext.EXECUTOR_SERVICE).get();
		receiver = receiverHub.createReceiver(TestContext.getConsumerGroupName(), partitionId, EventPosition.fromEnqueuedTime(Instant.now())).get();
	}
	
	@Test()
	public void sendMsgLargerThan64k() throws EventHubException, InterruptedException, ExecutionException, IOException
	{
		this.sendLargeMessageTest(100 * 1024);			
	}
	
	@Test(expected = PayloadSizeExceededException.class)
	public void sendMsgLargerThan256K() throws EventHubException, InterruptedException, ExecutionException, IOException
	{
		int msgSize = 256 * 1024;
		byte[] body = new byte[msgSize];
		for(int i=0; i< msgSize; i++)
		{
			body[i] = 1;
		}
		
		EventData largeMsg = new EventData(body);
		sender.sendSync(largeMsg);
	}
	
	@Test()
	public void sendMsgLargerThan128k() throws EventHubException, InterruptedException, ExecutionException, IOException
	{
		this.sendLargeMessageTest(129 * 1024);
	}
	
	public void sendLargeMessageTest(int msgSize) throws InterruptedException, ExecutionException, EventHubException
	{
		byte[] body = new byte[msgSize];
		for(int i=0; i< msgSize; i++)
		{
			body[i] = 1;
		}
		
		EventData largeMsg = new EventData(body);
		sender.sendSync(largeMsg);
		
		Iterable<EventData> messages = receiver.receiveSync(100);
		Assert.assertTrue(messages != null && messages.iterator().hasNext());

		EventData recdMessage = messages.iterator().next();

		Assert.assertTrue(
				String.format("sent msg size: %s, recvd msg size: %s", msgSize, recdMessage.getBytes().length),
				 recdMessage.getBytes().length == msgSize);
	}
	
	@AfterClass()
	public static void cleanup() throws EventHubException
	{
		if (receiverHub != null)
		{
			receiverHub.close();
		}
		
		if (ehClient != null)
		{
			ehClient.close();
		}
	}
}
