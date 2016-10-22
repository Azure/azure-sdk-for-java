/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.Arrays;

import com.microsoft.azure.eventhubs.EventData;

public class PrefabEventProcessor implements IEventProcessor
{
	private PrefabProcessorFactory factory;
	private byte[] telltaleBytes;
	private boolean doCheckpoint;
	private boolean doMarker;
	private boolean logEveryMessage;
	
	private int eventCount = 0;
	
	PrefabEventProcessor(PrefabProcessorFactory factory, String telltale, boolean doCheckpoint, boolean doMarker, boolean logEveryMessage)
	{
		this.factory = factory;
		this.telltaleBytes = telltale.getBytes();
		this.doCheckpoint = doCheckpoint;
		this.doMarker = doMarker;
		this.logEveryMessage = logEveryMessage;
	}
	
	@Override
	public void onOpen(PartitionContext context) throws Exception
	{
	}

	@Override
	public void onClose(PartitionContext context, CloseReason reason) throws Exception
	{
	}

	@Override
	public void onEvents(PartitionContext context, Iterable<EventData> messages) throws Exception
	{
		int batchSize = 0;
		EventData lastEvent = null;
		for (EventData event : messages)
		{
			this.eventCount++;
			batchSize++;
			if (((this.eventCount % 100) == 0) && this.doMarker)
			{
				TestUtilities.console(context.getPartitionId());
			}
			if (this.logEveryMessage)
			{
				TestUtilities.log("P" + context.getPartitionId() + " " + new String(event.getBody()) + "\n");
			}
			if (Arrays.equals(event.getBody(), this.telltaleBytes))
			{
				this.factory.setTelltaleFound(context.getPartitionId());
			}
			lastEvent = event;
		}
		this.factory.addBatch(batchSize);
		if (doCheckpoint)
		{
			context.checkpoint(lastEvent);
		}
	}

	@Override
	public void onError(PartitionContext context, Throwable error)
	{
		this.factory.putError(context.getPartitionId() + ": " + error.toString() + " " + error.getMessage());
	}
}
