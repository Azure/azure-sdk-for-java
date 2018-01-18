/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.extensions.appender;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.core.appender.*;

import com.microsoft.azure.eventhubs.*;

public final class EventHubsManager extends AbstractManager
{
	private final String eventHubConnectionString;
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
	
	private EventHubClient eventHubSender;
	
	protected EventHubsManager(final String name, final String eventHubConnectionString)
	{
		super(name);
		this.eventHubConnectionString = eventHubConnectionString;
	}
	
	public void send(final byte[] msg) throws EventHubException
	{
		if (msg != null)
		{
			EventData data = new EventData(msg);
			this.eventHubSender.sendSync(data);
		}
	}
	
	public void send(final Iterable<byte[]> messages) throws EventHubException
	{
		if (messages != null)
		{
			LinkedList<EventData> events = new LinkedList<EventData>();
			for(byte[] message : messages)
			{
				events.add(new EventData(message));
			}
			
			this.eventHubSender.sendSync(events);
		}
	}

	public void startup() throws EventHubException, IOException
	{
		this.eventHubSender = EventHubClient.createFromConnectionStringSync(this.eventHubConnectionString, EXECUTOR_SERVICE);
	}
}
