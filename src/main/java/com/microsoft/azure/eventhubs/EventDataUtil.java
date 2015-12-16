package com.microsoft.azure.eventhubs;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.qpid.proton.message.Message;

/*
 * Internal utility class for EventData
 */
final class EventDataUtil {
	
	private EventDataUtil(){}
	
	public static Collection<EventData> toEventDataCollection(Collection<Message> messages) {

		// TODO: no-copy solution
		LinkedList<EventData> events = new LinkedList<EventData>();
		for(Message message : messages) {
			events.add(new EventData(message));
		}
		
		return events;
	}
}
