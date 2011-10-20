package com.microsoft.azure.services.serviceBus;

import javax.xml.datatype.Duration;

import org.w3._2005.atom.Content;

import com.microsoft.azure.services.serviceBus.contract.QueueDescription;

public class Queue extends Entity<QueueDescription> implements MessageSender, MessageReceiver {
	Queue(ServiceBusClient client, String path) {
		super(client);
		
		Content content = new Content();
		content.setQueueDescription(new QueueDescription());
		getEntry().setContent(content);
		setModel(new QueueDescription());
		
		setPath(path);
	}

	
	
	// public object verbs
	
	public void save() {
//		getContract().createQueue(getEntryModel());
	}

	public void delete() {
		getContract().deleteQueue(getPath());
	}
	
	public void fetch() {
//		setEntryModel(getContract().getQueue(getPath()));
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
    	return getModel().getLockDuration();
    }

    public void setLockDuration(Duration value) {
    	getModel().setLockDuration(value);
    }

    public Long getMaxSizeInMegabytes() {
        return getModel().getMaxSizeInMegabytes();
    }

    public void setMaxSizeInMegabytes(Long value) {
        getModel().setMaxSizeInMegabytes(value);
    }

    public Long getMessageCount() {
        return getModel().getMessageCount();
    }






}
