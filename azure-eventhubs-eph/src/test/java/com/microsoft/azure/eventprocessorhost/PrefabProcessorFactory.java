/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.ArrayList;

public class PrefabProcessorFactory implements IEventProcessorFactory<IEventProcessor>
{
	private String telltale;
	private boolean doCheckpoint;
	private boolean doMarker;
	
	private ArrayList<String> errors = new ArrayList<String>();
	private boolean foundTelltale = false;
	private int eventsReceivedCount = 0;
	
	PrefabProcessorFactory(String telltale, boolean doCheckpoint, boolean doMarker)
	{
		this.telltale = telltale;
		this.doCheckpoint = doCheckpoint;
		this.doMarker = doMarker;
	}
	
	void putError(String error)
	{
		this.errors.add(error);
	}

	ArrayList<String> getErrors()
	{
		return this.errors;
	}
	
	int getErrorCount()
	{
		return this.errors.size();
	}
	
	boolean getTelltaleFound()
	{
		return this.foundTelltale;
	}
	
	void setTelltaleFound()
	{
		this.foundTelltale = true;
	}
	
	synchronized void addBatch(int batchSize)
	{
		this.eventsReceivedCount += batchSize;
	}
	
	int getEventsReceivedCount()
	{
		return this.eventsReceivedCount;
	}

	@Override
	public IEventProcessor createEventProcessor(PartitionContext context) throws Exception
	{
		return new PrefabEventProcessor(this, this.telltale, this.doCheckpoint, this.doMarker);
	}
}
