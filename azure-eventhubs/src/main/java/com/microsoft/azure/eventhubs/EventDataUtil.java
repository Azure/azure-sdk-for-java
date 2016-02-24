/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.microsoft.azure.eventhubs;

import java.util.*;
import java.util.function.*;
import org.apache.qpid.proton.message.*;

/*
 * Internal utility class for EventData
 */
final class EventDataUtil
{
	private EventDataUtil(){}
	
	static LinkedList<EventData> toEventDataCollection(final Collection<Message> messages)
	{
		if (messages == null)
		{
			return null;
		}
		
		// TODO: no-copy solution
		LinkedList<EventData> events = new LinkedList<EventData>();
		for(Message message : messages)
		{
			events.add(new EventData(message));
		}
		
		return events;
	}
	
	static Iterable<Message> toAmqpMessages(final Iterable<EventData> eventDatas, final String partitionKey)
	{
		final LinkedList<Message> messages = new LinkedList<Message>();
		eventDatas.forEach(new Consumer<EventData>()
		{
			@Override
			public void accept(EventData eventData)
			{				
				Message amqpMessage = partitionKey == null ? eventData.toAmqpMessage() : eventData.toAmqpMessage(partitionKey);
				messages.add(amqpMessage);
			}
		});
		
		return messages;
	}
	
	static Iterable<Message> toAmqpMessages(final Iterable<EventData> eventDatas)
	{
		return EventDataUtil.toAmqpMessages(eventDatas, null);
	}
}
