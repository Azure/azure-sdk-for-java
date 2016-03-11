/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.extensions;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.core.appender.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public final class EventHubsManager extends AbstractManager
{
	private final String eventHubConnectionString;
	
	private EventHubClient eventHubSender;
	
	protected EventHubsManager(final String name, final String eventHubConnectionString)
	{
		super(name);
		this.eventHubConnectionString = eventHubConnectionString;
	}
	
	public void send(final byte[] msg) throws ServiceBusException
	{
		if (msg != null)
		{
			EventData data = new EventData(msg);
			this.eventHubSender.sendSync(data);
		}
	}
	
	public void send(final Iterable<byte[]> messages) throws ServiceBusException
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

	public void startup() throws ServiceBusException, IOException
	{
		this.eventHubSender = EventHubClient.createFromConnectionStringSync(this.eventHubConnectionString);
	}
}
