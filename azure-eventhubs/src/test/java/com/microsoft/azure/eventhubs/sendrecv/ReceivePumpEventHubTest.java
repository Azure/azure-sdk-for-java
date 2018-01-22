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

import com.microsoft.azure.eventhubs.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.eventhubs.EventHubException;

public class ReceivePumpEventHubTest extends ApiTestBase
{
	static final String cgName = TestContext.getConsumerGroupName();
	static final String partitionId = "0";
	
	static EventHubClient ehClient;
	
	PartitionReceiver receiver;
	
	@BeforeClass
	public static void initializeEventHub()  throws EventHubException, IOException
	{
		final ConnectionStringBuilder connectionString = TestContext.getConnectionString();
		ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
	}
	
	@Before
	public void initializeTest() throws EventHubException
	{
		receiver = ehClient.createReceiverSync(cgName, partitionId, EventPosition.fromEnqueuedTime(Instant.now()));
	}
	
	@Test(expected = TimeoutException.class)
	public void testInvokeOnTimeoutKnobDefault() throws EventHubException, InterruptedException, ExecutionException, TimeoutException
	{
		CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
		receiver.setReceiveTimeout(Duration.ofSeconds(1));
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal));
		invokeSignal.get(3, TimeUnit.SECONDS);
	}
	
	@Test(expected = TimeoutException.class)
	public void testInvokeOnTimeoutKnobFalse() throws EventHubException, InterruptedException, ExecutionException, TimeoutException
	{
		CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
		receiver.setReceiveTimeout(Duration.ofSeconds(1));
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), false);
		invokeSignal.get(3, TimeUnit.SECONDS);
	}
	
	@Test()
	public void testInvokeOnTimeoutKnobTrue() throws EventHubException, InterruptedException, ExecutionException, TimeoutException
	{
		CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
		receiver.setReceiveTimeout(Duration.ofSeconds(1));
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);
		invokeSignal.get(3, TimeUnit.SECONDS);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSetReceiveHandlerMultipleTimes() throws EventHubException, InterruptedException, ExecutionException, TimeoutException
	{
		CompletableFuture<Void> invokeSignal = new CompletableFuture<Void>();
		receiver.setReceiveTimeout(Duration.ofSeconds(1));
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);
		
		receiver.setReceiveHandler(new InvokeOnReceiveEventValidator(invokeSignal), true);
	}
	
	@Test()
	public void testGraceFullCloseReceivePump() throws EventHubException, InterruptedException, ExecutionException, TimeoutException
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
	public void cleanupTest() throws EventHubException
	{
		if (receiver != null)
			receiver.closeSync();
	}
	
	@AfterClass
	public static void cleanup() throws EventHubException
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
