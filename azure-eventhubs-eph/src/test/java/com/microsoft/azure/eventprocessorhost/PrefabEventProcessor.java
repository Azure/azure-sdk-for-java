/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.Arrays;

import com.microsoft.azure.eventhubs.EventData;

public class PrefabEventProcessor implements IEventProcessor
{
	public enum CheckpointChoices { CKP_NONE, CKP_EXPLICIT, CKP_NOARGS };
	private PrefabProcessorFactory factory;
	private byte[] telltaleBytes;
	private CheckpointChoices doCheckpoint;
	private boolean doMarker;
	private boolean logEveryMessage;
	private boolean telltaleOnTimeout;
	
	private int eventCount = 0;
	
	PrefabEventProcessor(PrefabProcessorFactory factory, String telltale, CheckpointChoices doCheckpoint, boolean doMarker, boolean logEveryMessage)
	{
		this.factory = factory;
		this.telltaleBytes = telltale.getBytes();
		this.doCheckpoint = doCheckpoint;
		this.doMarker = doMarker;
		this.logEveryMessage = logEveryMessage;
		this.telltaleOnTimeout = telltale.isEmpty();
	}
	
	@Override
	public void onOpen(PartitionContext context) throws Exception
	{
		TestUtilities.log(context.getOwner() + " opening " + context.getPartitionId());
	}

	@Override
	public void onClose(PartitionContext context, CloseReason reason) throws Exception
	{
		TestUtilities.log(context.getOwner() + " closing " + context.getPartitionId());
	}

	@Override
	public void onEvents(PartitionContext context, Iterable<EventData> messages) throws Exception
	{
		int batchSize = 0;
		EventData lastEvent = null;
                if (messages != null && messages.iterator().hasNext())
                    this.factory.setOnEventsContext(context);
                
		for (EventData event : messages)
		{
			this.eventCount++;
			batchSize++;
			if (((this.eventCount % 10) == 0) && this.doMarker)
			{
				TestUtilities.log("P" + context.getPartitionId() + ": " + this.eventCount + "\n");
			}
			if (this.logEveryMessage)
			{
				TestUtilities.log("P" + context.getPartitionId() + " " + new String(event.getBytes()) + " @ " + event.getSystemProperties().getOffset() + "\n");
			}
			if (Arrays.equals(event.getBytes(), this.telltaleBytes))
			{
				this.factory.setTelltaleFound(context.getPartitionId());
			}
			lastEvent = event;
		}
		if (batchSize == 0)
		{
			if (this.telltaleOnTimeout)
			{
				TestUtilities.log("P" + context.getPartitionId() + " got expected timeout");
				this.factory.setTelltaleFound(context.getPartitionId());
			}
			else
			{
				TestUtilities.log("P" + context.getPartitionId() + " got UNEXPECTED timeout");
				this.factory.putError("P" + context.getPartitionId() + " got UNEXPECTED timeout");
			}
		}
		this.factory.addBatch(batchSize);
		switch (doCheckpoint)
		{
		case CKP_NONE:
			break;
			
		case CKP_EXPLICIT:
			context.checkpoint(lastEvent).get(); // do a get so that errors will throw
			TestUtilities.log("P" + context.getPartitionId() + " checkpointed at " + lastEvent.getSystemProperties().getOffset() + "\n");
			break;
			
		case CKP_NOARGS:
			context.checkpoint().get(); // do a get so errors will throw
			TestUtilities.log("P" + context.getPartitionId() + " checkpointed without arguments\n");
			break;
		}
	}

	@Override
	public void onError(PartitionContext context, Throwable error)
	{
		this.factory.putError(context.getPartitionId() + ": " + error.toString() + " " + error.getMessage());
	}
}
