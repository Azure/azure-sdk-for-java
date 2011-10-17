package com.microsoft.azure.services.serviceBus;

import javax.xml.datatype.Duration;

import com.microsoft.azure.services.serviceBus.contract.BrokeredMessage;
import com.microsoft.azure.services.serviceBus.contract.EntryModel;
import com.microsoft.azure.services.serviceBus.contract.QueueDescription;
import com.microsoft.azure.services.serviceBus.contract.ReceiveMode;
import com.sun.syndication.feed.atom.Entry;

public class Queue extends Entity<QueueDescription> implements MessageSender, MessageReceiver {
	public Queue(ServiceBusClient client, String path) {
		super(client);
		setModel(new QueueDescription());
		
		setPath(path);
	}

	
	
	// public object verbs
	
	public void create() {
		getContract().createQueue(getEntryModel());
	}

	public void delete() {
		getContract().deleteQueue(getPath());
	}
	
	public void get() {
		setEntryModel(getContract().getQueue(getPath()));
	}
	
	public void commit() {
		getContract().createQueue(getEntryModel());
	}

	public void send(BrokeredMessage message) {
		// TODO Auto-generated method stub
		
	}	
	
	public BrokeredMessage receive(int timeout, ReceiveMode receiveMode) {
		// TODO Auto-generated method stub
		return null;
	}

	public void abandon(BrokeredMessage message) {
		// TODO Auto-generated method stub
		
	}

	public void complete(BrokeredMessage message) {
		// TODO Auto-generated method stub
	}
	


	// public object data

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



    //public void setMessageCount(Long value) {
    //	getDescription().setMessageCount(value);
    //}


}
