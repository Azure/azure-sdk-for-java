package com.microsoft.azure.eventhubs.exceptioncontracts;

import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;

import org.junit.*;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestEventHubInfo;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.PayloadSizeExceededException;
import com.microsoft.azure.servicebus.ServiceBusException;

import junit.framework.Assert;

public class SendLargeMessageTest
{
	static TestEventHubInfo eventHubInfo;
	static ConnectionStringBuilder connStr;
	static String partitionId = "0";
	
	static EventHubClient ehClient;
	static PartitionSender sender;
	
	static EventHubClient receiverHub;
	static PartitionReceiver receiver;
	
	@BeforeClass
	public static void initializeEventHub()  throws Exception
	{
		Assume.assumeTrue(TestBase.isServiceRun());
		
		eventHubInfo = TestBase.checkoutTestEventHub();
		connStr = new ConnectionStringBuilder(
				eventHubInfo.getNamespaceName(), eventHubInfo.getName(), eventHubInfo.getSasRule().getKey(), eventHubInfo.getSasRule().getValue());
	
		ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
		sender = ehClient.createPartitionSender(partitionId).get();
		
		receiverHub = EventHubClient.createFromConnectionString(connStr.toString()).get();
		receiver = receiverHub.createReceiver(eventHubInfo.getRandomConsumerGroup(), partitionId, Instant.now()).get();
	}
	
	@Test()
	public void sendMsgLargerThan64k() throws ServiceBusException, InterruptedException, ExecutionException, IOException
	{
		this.sendLargeMessageTest(100 * 1024);			
	}
	
	@Test(expected = PayloadSizeExceededException.class)
	public void sendMsgLargerThan256K() throws ServiceBusException, InterruptedException, ExecutionException, IOException
	{
		int msgSize = 256 * 1024;
		byte[] body = new byte[msgSize];
		for(int i=0; i< msgSize; i++)
		{
			body[i] = 1;
		}
		
		EventData largeMsg = new EventData(body);
		sender.send(largeMsg).get();
	}
	
	@Test()
	public void sendMsgLargerThan128k() throws ServiceBusException, InterruptedException, ExecutionException, IOException
	{
		this.sendLargeMessageTest(129 * 1024);
	}
	
	public void sendLargeMessageTest(int msgSize) throws InterruptedException, ExecutionException, ServiceBusException
	{
		byte[] body = new byte[msgSize];
		for(int i=0; i< msgSize; i++)
		{
			body[i] = 1;
		}
		
		EventData largeMsg = new EventData(body);
		for (int i = 0; i< 5; i++)
		{
			sender.send(largeMsg).get();
		}
		
		Iterable<EventData> messages = receiver.receive(100).get();
		Assert.assertTrue(messages != null && messages.iterator().hasNext());
		Iterator<EventData> miterator = messages.iterator();
		EventData recdMessage = null;
		while(miterator.hasNext())
		{
			recdMessage = miterator.next();
		}		
		
		Assert.assertTrue(recdMessage.getBody().length == msgSize);
	}
	
	@AfterClass()
	public static void cleanup() throws ServiceBusException
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
