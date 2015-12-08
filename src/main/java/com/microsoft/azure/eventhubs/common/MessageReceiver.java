package com.microsoft.azure.eventhubs.common;

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
 * TODO: onReceive
 */
public class MessageReceiver extends ClientEntity {
	
	private final int prefetchCount; 
	private final ConcurrentLinkedQueue<Message> prefetchedMessages;
	private final ConcurrentLinkedQueue<CompletableFuture<Collection<Message>>> pendingReceives;
	private final MessagingFactory underlyingFactory;
	
	private Receiver receiveLink;
	
	/**
	 * @param connection Connection on which the MessageReceiver's receive Amqp link need to be created on. Connection has to be associated with Reactor before Creating a receiver on it.
	 */
	public static MessageReceiver Create(
			final MessagingFactory factory, 
			final String name, 
			final String recvPath, 
			final String offset,
			final int prefetchCount)
	{
		MessageReceiver msgReceiver = new MessageReceiver(factory, name, recvPath, offset, prefetchCount);
		
		ReceiveLinkHandler handler = new ReceiveLinkHandler(name, msgReceiver);
		BaseHandler.setHandler(msgReceiver.receiveLink, handler);

		return msgReceiver;
	}
	
	private MessageReceiver(final MessagingFactory factory, 
			final String name, 
			final String recvPath, 
			final String offset,
			final int prefetchCount){
		this.prefetchCount = prefetchCount;
		this.prefetchedMessages = new ConcurrentLinkedQueue<Message>();
		this.receiveLink = this.createReceiveLink(factory.getConnection(), name, recvPath, offset);
		this.pendingReceives = new ConcurrentLinkedQueue<CompletableFuture<Collection<Message>>>();
		this.underlyingFactory = factory;
	}
	
	public int getPrefetchCount() {
		return this.prefetchCount;
	}
	
	public CompletableFuture<Collection<Message>> receive(){
		if (!this.prefetchedMessages.isEmpty()) {
			synchronized (this.prefetchedMessages) {
				if (!this.prefetchedMessages.isEmpty()) {
					Collection<Message> returnMessages = this.prefetchedMessages;
					this.prefetchedMessages.removeAll((Collection<?>)returnMessages);
					return CompletableFuture.completedFuture(returnMessages);
				}
			}
		}
		
		CompletableFuture<Collection<Message>> onReceive = new CompletableFuture<Collection<Message>>();
		this.pendingReceives.offer(onReceive);
		return onReceive;
	}
	
	// intended to be called by proton reactor handler 
	void onDelivery(Collection<Message> messages) {
		if (this.pendingReceives.isEmpty()) {
			this.prefetchedMessages.addAll(messages);
		}
		else {
			this.pendingReceives.poll().complete(messages);
		}
	}
	
	// TODO: Map to appropriate exception based on ErrMsg 
	void onError(ErrorCondition error) {
		if (this.pendingReceives != null && !this.pendingReceives.isEmpty()) {
			synchronized (this.pendingReceives) {
				
				for(CompletableFuture<Collection<Message>> future: this.pendingReceives) {
					future.completeExceptionally(new Exception(error.toString()));
				}
			}
		}
	}
	
	private Receiver createReceiveLink(
								final Connection connection, 
								final String name, 
								final String recvPath, 
								final String offset)
	{	
		Source source = new Source();
        source.setAddress(recvPath);
        source.setFilter(Collections.singletonMap(
        		Symbol.valueOf("apache.org:selector-filter:string"),
        		new UnknownDescribedType(Symbol.valueOf("apache.org:selector-filter:string"), 
        				String.format("amqp.annotation.x-opt-offset > '%s'", offset))));
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

	@Override
	public void close() {
		if (this.receiveLink != null && this.receiveLink.getLocalState() == EndpointState.ACTIVE) {
			this.receiveLink.close();
		}
	}
	
}
