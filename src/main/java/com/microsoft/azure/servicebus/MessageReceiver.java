package com.microsoft.azure.servicebus;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.qpid.proton.amqp.*;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.*;
import org.apache.qpid.proton.reactor.Reactor;

/**
 * Abstracts all amqp related details
 * translates event-driven reactor model into async receive Api
 * Manage reconnect? - store currentConsumedOffset
 */
public class MessageReceiver extends ClientEntity {
	
	private final int prefetchCount; 
	private final ConcurrentLinkedQueue<CompletableFuture<Collection<Message>>> pendingReceives;
	private final MessagingFactory underlyingFactory;
	
	private ConcurrentLinkedQueue<Message> prefetchedMessages;
	private Receiver receiveLink;
	private CompletableFuture<MessageReceiver> linkOpen;
	private MessageReceiveHandler receiveHandler;
	/**
	 * @param connection Connection on which the MessageReceiver's receive Amqp link need to be created on. Connection has to be associated with Reactor before Creating a receiver on it.
	 */
	public static CompletableFuture<MessageReceiver> Create(
			final MessagingFactory factory, 
			final String name, 
			final String recvPath, 
			final String offset,
			final boolean offsetInclusive,
			final int prefetchCount,
			final MessageReceiveHandler receiveHandler)
	{
		MessageReceiver msgReceiver = new MessageReceiver(factory, name, recvPath, offset, offsetInclusive, prefetchCount, receiveHandler);
		
		ReceiveLinkHandler handler = new ReceiveLinkHandler(name, msgReceiver);
		BaseHandler.setHandler(msgReceiver.receiveLink, handler);

		return msgReceiver.linkOpen;
	}
	
	private MessageReceiver(final MessagingFactory factory, 
			final String name, 
			final String recvPath, 
			final String offset,
			final boolean offsetInclusive,
			final int prefetchCount,
			final MessageReceiveHandler receiveHandler){
		this.prefetchCount = prefetchCount;
		this.prefetchedMessages = new ConcurrentLinkedQueue<Message>();
		this.receiveLink = this.createReceiveLink(factory.getConnection(), name, recvPath, offset, offsetInclusive);
		this.linkOpen = new CompletableFuture<MessageReceiver>();
		this.pendingReceives = new ConcurrentLinkedQueue<CompletableFuture<Collection<Message>>>();
		this.underlyingFactory = factory;
		this.receiveHandler = receiveHandler;
	}
	
	public int getPrefetchCount() {
		return this.prefetchCount;
	}
	
	/*
	 * if ReceiveHandler is passed to the Constructor - this receive shouldn't be called
	 */
	public CompletableFuture<Collection<Message>> receive(){
		if (!this.prefetchedMessages.isEmpty()) {
			synchronized (this.prefetchedMessages) {
				if (!this.prefetchedMessages.isEmpty()) {
					Collection<Message> returnMessages = this.prefetchedMessages;
					this.prefetchedMessages = new ConcurrentLinkedQueue<Message>();
					return CompletableFuture.completedFuture(returnMessages);
				}
			}
		}
		
		CompletableFuture<Collection<Message>> onReceive = new CompletableFuture<Collection<Message>>();
		this.pendingReceives.offer(onReceive);
		return onReceive;
	}
	
	void onOpenComplete(ErrorCondition condition){
		if (condition == null) {
			this.linkOpen.complete(this);
		}
		else {
			this.linkOpen.completeExceptionally(ExceptionUtil.toException(condition));
		}
	}
	
	// intended to be called by proton reactor handler 
	void onDelivery(Collection<Message> messages) {
		if (this.receiveHandler != null) {
			this.receiveHandler.onReceiveMessages(messages);
			
			return;
		}
		
		synchronized (this.pendingReceives) {
			if (this.pendingReceives.isEmpty()) {
				this.prefetchedMessages.addAll(messages);
			}
			else {
				this.pendingReceives.poll().complete(messages);
			}
		}
	}
	
	// TODO: Map to appropriate exception based on ErrMsg 
	void onError(ErrorCondition error) {
		
		// TODO: apply retryPolicy here - recreate link - "preserve offset"
		
		synchronized (this.linkOpen) {
			if (!this.linkOpen.isDone()) {
				this.onOpenComplete(error);
				return;
			}
		}
		
		if (this.pendingReceives != null && !this.pendingReceives.isEmpty()) {
			
			synchronized (this.pendingReceives) {
				for(CompletableFuture<Collection<Message>> future: this.pendingReceives) {
					future.completeExceptionally(ExceptionUtil.toException(error));
				}
			}
		}
	}

	@Override
	public void close() {
		if (this.receiveLink != null && this.receiveLink.getLocalState() == EndpointState.ACTIVE) {
			this.receiveLink.close();
		}
	}	
	
	private Receiver createReceiveLink(
								final Connection connection, 
								final String name, 
								final String recvPath, 
								final String offset,
								final boolean offsetInclusive)
	{	
		Source source = new Source();
        source.setAddress(recvPath);
        source.setFilter(Collections.singletonMap(
        		Symbol.valueOf("apache.org:selector-filter:string"),
        		new UnknownDescribedType(Symbol.valueOf("apache.org:selector-filter:string"), 
        				String.format("amqp.annotation.%s >%s '%s'", AmqpConstants.OffsetName, offsetInclusive ? "=" : StringUtil.EMPTY, offset))));
        // source.setDynamicNodeProperties(Collections.singletonMap(Symbol.valueOf("com.microsoft:epoch"), 122));
		
		Session ssn = connection.session();
		Receiver receiver = ssn.receiver(name);
		receiver.setSource(source);
		receiver.setTarget(new Target());
		
		// use explicit settlement via dispositions (not pre-settled)
        receiver.setSenderSettleMode(SenderSettleMode.UNSETTLED);
        receiver.setReceiverSettleMode(ReceiverSettleMode.SECOND);
        // receiver.setContext(Collections.singletonMap(Symbol.valueOf("com.microsoft:epoch"), 122));
        
        ssn.open();
        receiver.open();
                
        return receiver;
	}
}
