/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.sendrecv;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.PartitionReceiveHandler;
import com.microsoft.azure.eventhubs.ReceivePump;
import com.microsoft.azure.servicebus.IteratorUtil;
import com.microsoft.azure.servicebus.ServiceBusException;

public class ReceivePumpTest
{
	private final String exceptionMessage = "receive Exception";
	private boolean assertion = false;
	
	@Before
	public void initializeValidation()
	{
		assertion = false;
	}
	
	@Test()
	public void testPumpOnReceiveEventFlow()
	{
		final ReceivePump receivePump = new ReceivePump(
				new ReceivePump.IPartitionReceiver()
				{
					@Override public Iterable<EventData> receive(int maxBatchSize) throws ServiceBusException
					{
						LinkedList<EventData> events = new LinkedList<EventData>();
						events.add(new EventData("some".getBytes()));
						return events;
					}
					@Override public String getPartitionId()
					{
						return "0";
					}
				},
				new PartitionReceiveHandler(10) {
					@Override public void onReceive(Iterable<EventData> events)
					{
						assertion = IteratorUtil.sizeEquals(events, 1); 
						
						// stop-pump
						throw new PumpClosedException();
					}
					@Override public void onError(Throwable error)
					{
						Assert.assertTrue(error instanceof PumpClosedException);
					}
				},
				true);

		receivePump.run();
		Assert.assertTrue(assertion);
	}

	@Test()
	public void testPumpReceiveTransientErrorsPropagated() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException
	{
		final ReceivePump receivePump = new ReceivePump(
				new ReceivePump.IPartitionReceiver()
				{
					@Override public Iterable<EventData> receive(int maxBatchSize) throws ServiceBusException
					{
						throw new ServiceBusException(true, exceptionMessage);
					}
					@Override public String getPartitionId()
					{
						return "0";
					}
				},
				new PartitionReceiveHandler(10) {
					@Override public void onReceive(Iterable<EventData> events)
					{						
					}
					@Override public void onError(Throwable error)
					{
						assertion = error.getMessage().equals(exceptionMessage);
					}
				},
				true);
		
		receivePump.run();
		Assert.assertTrue(assertion);
	}
	
	@Test()
	public void testPumpReceiveExceptionsPropagated() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException
	{
		final ReceivePump receivePump = new ReceivePump(
				new ReceivePump.IPartitionReceiver()
				{
					@Override public Iterable<EventData> receive(int maxBatchSize) throws ServiceBusException
					{
						throw new ServiceBusException(false, exceptionMessage);
					}
					@Override public String getPartitionId()
					{
						return "0";
					}
				},
				new PartitionReceiveHandler(10) {
					@Override public void onReceive(Iterable<EventData> events)
					{						
					}
					@Override public void onError(Throwable error)
					{
						assertion = error.getMessage().equals(exceptionMessage);
					}
				},
				true);
		
		receivePump.run();
		Assert.assertTrue(assertion);
	}
	
	@Test()
	public void testPumpOnReceiveExceptionsPropagated() throws ServiceBusException, InterruptedException, ExecutionException, TimeoutException
	{
		final String runtimeExceptionMsg = "random exception";
		final ReceivePump receivePump = new ReceivePump(
				new ReceivePump.IPartitionReceiver()
				{
					@Override public Iterable<EventData> receive(int maxBatchSize) throws ServiceBusException
					{
						return null;
					}
					@Override public String getPartitionId()
					{
						return "0";
					}
				},
				new PartitionReceiveHandler(10) {
					@Override public void onReceive(Iterable<EventData> events)
					{
						throw new RuntimeException(runtimeExceptionMsg);
					}
					@Override public void onError(Throwable error)
					{
						assertion = error.getMessage().equals(runtimeExceptionMsg);
					}
				},
				true);
		
		receivePump.run();
		Assert.assertTrue(assertion);
	}
	
	public class PumpClosedException extends RuntimeException
	{
		private static final long serialVersionUID = -5050327636359966016L;
	}
}
