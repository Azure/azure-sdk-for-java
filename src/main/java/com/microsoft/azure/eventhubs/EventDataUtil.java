package com.microsoft.azure.eventhubs;

import java.util.*;
import java.util.function.Consumer;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.DataList;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.MessageSender;
import com.microsoft.azure.servicebus.amqp.AmqpConstants;

/*
 * Internal utility class for EventData
 */
final class EventDataUtil
{
	
	private EventDataUtil(){}
	
	static LinkedList<EventData> toEventDataCollection(final Collection<Message> messages)
	{
		// TODO: no-copy solution
		LinkedList<EventData> events = new LinkedList<EventData>();
		for(Message message : messages)
		{
			events.add(new EventData(message));
		}
		
		return events;
	}
	
	/**
	 * Convert a Batch of @@EventData to @@Message
	 */
	static Message toAmqpMessage(final Iterable<EventData> eventDatas, final String partitionKey)
	{
		final LinkedList<Data> dataList = new LinkedList<Data>();
		eventDatas.forEach(new Consumer<EventData>()
		{
			@Override
			public void accept(EventData eventData)
			{				
				Message amqpMessage = partitionKey == null ? eventData.toAmqpMessage() : eventData.toAmqpMessage(partitionKey);
				
				// TODO: calculate approximate value using the underlying byte[] for allocation; find alternative API - check if copying bytes
				byte[] bytes = new byte[MessageSender.MaxMessageLength];
				int encodedSize = amqpMessage.encode(bytes, 0, (int)(MessageSender.MaxMessageLength));
				
				dataList.add(new Data(new Binary(bytes, 0, encodedSize)));
			}
		});
		
		Message amqpMessage = Proton.message();
		
		if (partitionKey != null)
		{
			MessageAnnotations messageAnnotations = (amqpMessage.getMessageAnnotations() == null) 
					? new MessageAnnotations(new HashMap<Symbol, Object>()) 
					: amqpMessage.getMessageAnnotations();		
			messageAnnotations.getValue().put(AmqpConstants.PartitionKey, partitionKey);
			amqpMessage.setMessageAnnotations(messageAnnotations);
		}
		
		amqpMessage.setBody(new DataList(dataList));
		return amqpMessage;
	}
	
	/**
	 * Convert a Batch of @@EventData to @@Message
	 */
	static Message toAmqpMessage(final Iterable<EventData> eventDatas)
	{
		return EventDataUtil.toAmqpMessage(eventDatas, null);
	}
}
