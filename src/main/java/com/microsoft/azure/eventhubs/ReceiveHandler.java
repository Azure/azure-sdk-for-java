package com.microsoft.azure.eventhubs;

import java.util.Collection;

import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.MessageReceiveHandler;

public abstract class ReceiveHandler extends MessageReceiveHandler {

	public abstract void onReceive(Collection<EventData> events);
	
	@Override
	public void onReceiveMessages(Collection<Message> messages) {
		this.onReceive(EventDataUtil.toEventDataCollection(messages));
	}
}
