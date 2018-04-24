package com.microsoft.azure.servicebus.primitives;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.TransactionContext;
import org.apache.qpid.proton.message.Message;

public class RequestResponseWorkItem extends WorkItem<Message>
{
	Message request;
	TransactionContext transaction;
	
	public RequestResponseWorkItem(Message request, TransactionContext transaction, CompletableFuture<Message> completableFuture, TimeoutTracker tracker) {
		super(completableFuture, tracker);
		this.request = request;
		this.transaction = transaction;
	}
	
	public RequestResponseWorkItem(Message request, TransactionContext transaction, CompletableFuture<Message> completableFuture, Duration timeout) {
		super(completableFuture, timeout);
		this.request = request;
		this.transaction = transaction;
	}
	
	public Message getRequest()
	{
		return this.request;
	}

	public TransactionContext getTransaction() { return this.transaction; }
}
