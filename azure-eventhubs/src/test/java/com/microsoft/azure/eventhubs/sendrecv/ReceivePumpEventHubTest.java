/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.sendrecv;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

public class ReceivePumpEventHubTest extends ApiTestBase
{
	static final String cgName = TestContext.getConsumerGroupName();
	static final String partitionId = "0";
	
	static EventHubClient ehClient;
	
	PartitionReceiver receiver;
	
	@BeforeClass
	public static void initializeEventHub()  throws ServiceBusException, IOException
	{
		final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
		ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString());
	}
	
	@Before
	public void initializeTest() throws ServiceBusException
	{
		receiver = ehClient.createReceiverSync(cgName, partitionId, Instant.now());
	}
	
	@Test(expected = TimeoutException.class)
	public void testInvokeOnTimeoutKnobDefault() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException
	{
		CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
		receiver.setReceiveTimeout(Duration.ofSeconds(1));
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal));
		invokeSignal.get(3, TimeUnit.SECONDS);
	}
	
	@Test(expected = TimeoutException.class)
	public void testInvokeOnTimeoutKnobFalse() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException
	{
		CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
		receiver.setReceiveTimeout(Duration.ofSeconds(1));
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), false);
		invokeSignal.get(3, TimeUnit.SECONDS);
	}
	
	@Test()
	public void testInvokeOnTimeoutKnobTrue() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException
	{
		CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
		receiver.setReceiveTimeout(Duration.ofSeconds(1));
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);
		invokeSignal.get(3, TimeUnit.SECONDS);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSetReceiveHandlerMultipleTimes() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException
	{
		CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
		receiver.setReceiveTimeout(Duration.ofSeconds(1));
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);
		
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);
	}
	
	@Test()
	public void testGraceFullCloseReceivePump() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException
	{
		CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
		receiver.setReceiveTimeout(Duration.ofSeconds(1));
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);
		
		receiver.setReceiveHandler(null).get();
		
		invokeSignal = new CompletableFuture<Void>();
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);
		invokeSignal.get(3, TimeUnit.SECONDS);
	}
	
	@After
	public void cleanupTest() throws ServiceBusException
	{
		if (receiver != null)
			receiver.closeSync();
	}
	
	@AfterClass
	public static void cleanup() throws ServiceBusException
	{
		if (ehClient != null)
			ehClient.closeSync();
	}
	
	public static final class InvokeOnReceiveEventValidator extends PartitionReceiveHandler
	{
		final CompletableFuture<Void> signalInvoked;
		
		public InvokeOnReceiveEventValidator(final CompletableFuture<Void> signalInvoked)
		{
			super(50);
			this.signalInvoked = signalInvoked;
		}

		@Override
		public void onReceive(Iterable<EventData> events)
		{
			this.signalInvoked.complete(null);
		}

		@Override
		public void onError(Throwable error)
		{
			this.signalInvoked.completeExceptionally(error);
		}
	}
}
