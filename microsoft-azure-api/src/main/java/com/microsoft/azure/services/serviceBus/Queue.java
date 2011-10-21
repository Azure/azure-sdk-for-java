package com.microsoft.azure.services.serviceBus;

import javax.xml.datatype.Duration;

import org.w3._2005.atom.Content;
import org.w3._2005.atom.Entry;

import com.microsoft.azure.services.serviceBus.contract.QueueDescription;

public class Queue extends Entity<QueueDescription> implements MessageSender, MessageReceiver {
	Queue(ServiceBusClient client, String path) {
		super(client);
		
		Content content = new Content();
		content.setType("application/xml");
		content.setQueueDescription(new QueueDescription());
		getEntry().setContent(content);
		
		setPath(path);
	}

	public Queue(ServiceBusClient client, Entry entry) {
		super(client, entry);
	}

	QueueDescription getQueueDescription(){
		return getEntry().getContent().getQueueDescription();
	}
	
	
	// public object verbs
	
	public void save() {
		getContract().createQueue(getEntry());
	}

	public void delete() {
		getContract().deleteQueue(getPath());
	}
	
	public void fetch() {
		setEntry(getContract().getQueue(getPath()));
	}



	public void sendMessage(Message message) {
		// TODO Auto-generated method stub
		
	}
	
	public Message receiveMessage(int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Message peekLockMessage(int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void abandonMessage(Message message) {
		// TODO Auto-generated method stub
		
	}

	
	public void completeMessage(Message message) {
		// TODO Auto-generated method stub
		
	}


	
	
	// entity state properties

	public String getPath() {
		return getEntry().getTitle();
	}

	public void setPath(String value) {
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
