package com.microsoft.azure.services.serviceBus.messaging;

import javax.xml.datatype.Duration;
import com.microsoft.azure.services.serviceBus.schema.Content;
import com.microsoft.azure.services.serviceBus.schema.Entry;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.services.serviceBus.MessageResult;
import com.microsoft.azure.services.serviceBus.ReceiveMode;
import com.microsoft.azure.services.serviceBus.schema.QueueDescription;

public class Queue extends AbstractEntity implements MessageSender, MessageReceiver, MessageTransceiver {
	Queue(ServiceBusClient client, String name) {
		super(client);
		
		Content content = new Content();

		getEntry().setContent(content);
		content.setType("application/xml");
		content.setQueueDescription(new QueueDescription());
		
		setName(name);
	}

	Queue(ServiceBusClient client, Entry entry) {
		super(client, entry);
	}

	QueueDescription getQueueDescription(){
		return getEntry().getContent().getQueueDescription();
	}
	
	
	// API methods
//	
//	public void save() throws ServiceException {
//		setEntry(getContract().createQueue(getEntry()));
//	}
//
//	public void delete() throws ServiceException {
//		getContract().deleteQueue(getName());
//	}
//	
//	public void fetch() throws ServiceException {
//		setEntry(getContract().getQueue(getName()));
//	}
//
//	public void sendMessage(Message message) throws ServiceException {
//		sendMessage(message, SendMessageOptions.DEFAULT);
//	}
//
//	public void sendMessage(Message message, SendMessageOptions options) throws ServiceException {
//		getContract().sendMessage(getName(), message.getProperties(), message.getBody());
//	}
//
//	public Message receiveMessage() throws ServiceException {
//		return receiveMessage(ReceiveMessageOptions.DEFAULT);
//	}
//	
//	public Message receiveMessage(ReceiveMessageOptions options) throws ServiceException {
//		MessageResult result = getContract().receiveMessage(getName(), options.getTimeout(), ReceiveMode.RECEIVE_AND_DELETE);
//		return new Message(result.getBrokerProperties(), result.getBody());
//	}
//
//	public Message peekLockMessage() throws ServiceException {
//		return peekLockMessage(ReceiveMessageOptions.DEFAULT);
//	}
//
//	public Message peekLockMessage(ReceiveMessageOptions options) throws ServiceException {
//		MessageResult result = getContract().receiveMessage(getName(), options.getTimeout(), ReceiveMode.PEEK_LOCK);
//		return new Message(result.getBrokerProperties(), result.getBody());
//	}
	
	public void abandonMessage(Message message) {
		// TODO Auto-generated method stub
	}
	
	public void completeMessage(Message message) {
		// TODO Auto-generated method stub
	}


	
	
	// API properties

	public String getName() {
		return getEntry().getTitle();
	}

	public void setName(String value) {
		getEntry().setTitle(value);
	}
	
    public Duration getLockDuration() {
    	return getQueueDescription().getLockDuration();
    }

    public void setLockDuration(Duration value) {
    	getQueueDescription().setLockDuration(value);
    }

    public Long getMaxSizeInMegabytes() {
        return getQueueDescription().getMaxSizeInMegabytes();
    }

    public void setMaxSizeInMegabytes(Long value) {
    	getQueueDescription().setMaxSizeInMegabytes(value);
    }

    public Long getMessageCount() {
        return getQueueDescription().getMessageCount();
    }

	public Message receiveMessage() throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Message receiveMessage(ReceiveMessageOptions options)
			throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Message peekLockMessage() throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Message peekLockMessage(ReceiveMessageOptions options)
			throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendMessage(Message message) throws ServiceException {
		// TODO Auto-generated method stub
		
	}

	public void sendMessage(Message message, SendMessageOptions options)
			throws ServiceException {
		// TODO Auto-generated method stub
		
	}

}
