package com.microsoft.azure.servicebus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

final class MessageBrowser implements IMessageBrowser
{
	private long lastPeekedSequenceNumber = 0;
	private boolean isReceiveSideBrowser = false;
	private MessageReceiver messageReceiver = null;
	private MessageSender messageSender = null;
		
	public MessageBrowser(MessageReceiver messageReceiver)
	{		
		this.messageReceiver = messageReceiver;
		this.isReceiveSideBrowser = true;
	}
	
	public MessageBrowser(MessageSender messageSender)
	{		
		this.messageSender = messageSender;
		this.isReceiveSideBrowser = false;
	}
	
	@Override
	public IMessage peek() throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.peekAsync());
	}

	@Override
	public IMessage peek(long fromSequenceNumber) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.peekAsync(fromSequenceNumber));
	}

	@Override
	public Collection<IMessage> peekBatch(int messageCount) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.peekBatchAsync(messageCount));
	}

	@Override
	public Collection<IMessage> peekBatch(long fromSequenceNumber, int messageCount) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.peekBatchAsync(fromSequenceNumber, messageCount));
	}

	@Override
	public CompletableFuture<IMessage> peekAsync() {
		return this.peekAsync(this.lastPeekedSequenceNumber + 1);
	}

	@Override
	public CompletableFuture<IMessage> peekAsync(long fromSequenceNumber) {
		return this.peekBatchAsync(fromSequenceNumber, 1).thenApplyAsync((c) -> 
		{
			IMessage message = null;
			Iterator<IMessage> iterator = c.iterator();
			if(iterator.hasNext())
			{
				message = iterator.next();
				iterator.remove();
			}
			return message;
		});
	}

	@Override
	public CompletableFuture<Collection<IMessage>> peekBatchAsync(int messageCount) {
		return this.peekBatchAsync(this.lastPeekedSequenceNumber + 1, messageCount);
	}

	@Override
	public CompletableFuture<Collection<IMessage>> peekBatchAsync(long fromSequenceNumber, int messageCount) {
		CompletableFuture<Collection<org.apache.qpid.proton.message.Message>> peekFuture;
		if(this.isReceiveSideBrowser)
		{
			String sessionId = this.messageReceiver.isSessionReceiver()? this.messageReceiver.getInternalReceiver().getSessionId() : null;
			peekFuture = this.messageReceiver.getInternalReceiver().peekMessagesAsync(fromSequenceNumber, messageCount, sessionId);
		}
		else
		{
			peekFuture = this.messageSender.getInternalSender().peekMessagesAsync(fromSequenceNumber, messageCount);
		}		
		
		return peekFuture.thenApplyAsync((peekedMessages) -> 
		{
			ArrayList<IMessage> convertedMessages = new ArrayList<IMessage>();
			if(peekedMessages != null)
			{
				long sequenceNumberOfLastMessage = 0;
				for(org.apache.qpid.proton.message.Message message : peekedMessages)
				{
					Message convertedMessage = MessageConverter.convertAmqpMessageToBrokeredMessage(message);
					sequenceNumberOfLastMessage = convertedMessage.getSequenceNumber();
					convertedMessages.add(convertedMessage);
				}
				
				if(sequenceNumberOfLastMessage > 0)
				{
					this.lastPeekedSequenceNumber = sequenceNumberOfLastMessage;
				}
			}		
			
			return convertedMessages;
		});
	}
}
