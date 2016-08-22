/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;

/*
 * Internal utility class for EventData
 */
final class EventDataUtil
{
	@SuppressWarnings("serial")
	static final Set<String> RESERVED_SYSTEM_PROPERTIES = Collections.unmodifiableSet(new HashSet<String>()
			{{
				add(AmqpConstants.OFFSET_ANNOTATION_NAME);
				add(AmqpConstants.PARTITION_KEY_ANNOTATION_NAME);
				add(AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME);
				add(AmqpConstants.ENQUEUED_TIME_UTC_ANNOTATION_NAME);
				add(AmqpConstants.PUBLISHER_ANNOTATION_NAME);
			}});
	
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
