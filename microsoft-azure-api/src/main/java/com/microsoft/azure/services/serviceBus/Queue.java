package com.microsoft.azure.services.serviceBus;

import javax.xml.datatype.Duration;
import org.w3._2005.atom.Content;
import org.w3._2005.atom.Entry;

import com.microsoft.azure.services.serviceBus.contract.MessageResult;
import com.microsoft.azure.services.serviceBus.contract.QueueDescription;
import com.microsoft.azure.services.serviceBus.contract.ReceiveMode;

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
	
	public void save() {
		setEntry(getContract().createQueue(getEntry()));
	}

	public void delete() {
		getContract().deleteQueue(getName());
	}
	
	public void fetch() {
		setEntry(getContract().getQueue(getName()));
	}

	public void sendMessage(Message message) {
		sendMessage(message, SendMessageOptions.DEFAULT);
	}

	public void sendMessage(Message message, SendMessageOptions options) {
		getContract().sendMessage(getName(), message.getProperties(), message.getBody());
	}

	public Message receiveMessage() {
		return receiveMessage(ReceiveMessageOptions.DEFAULT);
	}
	
	public Message receiveMessage(ReceiveMessageOptions options) {
		MessageResult result = getContract().receiveMessage(getName(), options.getTimeout(), ReceiveMode.RECEIVE_AND_DELETE);
		return new Message(result.getBrokerProperties(), result.getBody());
	}

	public Message peekLockMessage() {
		return peekLockMessage(ReceiveMessageOptions.DEFAULT);
	}

	public Message peekLockMessage(ReceiveMessageOptions options) {
		MessageResult result = getContract().receiveMessage(getName(), options.getTimeout(), ReceiveMode.PEEK_LOCK);
		return new Message(result.getBrokerProperties(), result.getBody());
	}
	
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

}
