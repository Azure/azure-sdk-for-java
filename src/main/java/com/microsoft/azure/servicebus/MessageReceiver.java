package com.microsoft.azure.servicebus;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.amqp.*;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.*;
import org.apache.qpid.proton.reactor.Reactor;

/**
 * Common Receiver that abstracts all amqp related details
 * translates event-driven reactor model into async receive Api
 * TODO: NEED AmqpObject state-machine for open/close
 */
public class MessageReceiver extends ClientEntity
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.ServiceBusClientTrace);
	
	private final int prefetchCount; 
	private final ConcurrentLinkedQueue<WorkItem<Collection<Message>>> pendingReceives;
	private final MessagingFactory underlyingFactory;
	private final String name;
	private final String receivePath;
	
	
	private ConcurrentLinkedQueue<Message> prefetchedMessages;
	private Receiver receiveLink;
	private CompletableFuture<MessageReceiver> linkOpen;
	private MessageReceiveHandler receiveHandler;
	
	private long epoch;
	private boolean isEpochReceiver;
	private TimeoutTracker currentOperationTracker;
	private String lastReceivedOffset;
	
	/**
	 * @param connection Connection on which the MessageReceiver's receive Amqp link need to be created on.
	 * Connection has to be associated with Reactor before Creating a receiver on it.
	 */
	public static CompletableFuture<MessageReceiver> Create(
			final MessagingFactory factory, 
			final String name, 
			final String recvPath, 
			final String offset,
			final boolean offsetInclusive,
			final int prefetchCount,
			final long epoch,
			final boolean isEpochReceiver,
			final MessageReceiveHandler receiveHandler)
	{
		MessageReceiver msgReceiver = new MessageReceiver(factory, name, recvPath, offset, offsetInclusive, prefetchCount, epoch, isEpochReceiver, receiveHandler);
		
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
			final Long epoch,
			final boolean isEpochReceiver,
			final MessageReceiveHandler receiveHandler)
	{
		super(name);
		this.underlyingFactory = factory;
		this.name = name;
		this.receivePath = recvPath;
		this.prefetchCount = prefetchCount;
		this.epoch = epoch;
		this.isEpochReceiver = isEpochReceiver;
		this.prefetchedMessages = new ConcurrentLinkedQueue<Message>();
		this.receiveLink = this.createReceiveLink(factory.getConnection(), name, recvPath, offset, offsetInclusive);
		this.lastReceivedOffset = offset.toString();
		this.currentOperationTracker = TimeoutTracker.create(factory.getOperationTimeout());
		this.initializeLinkOpen(this.currentOperationTracker);
		
		this.pendingReceives = new ConcurrentLinkedQueue<WorkItem<Collection<Message>>>();
		this.receiveHandler = receiveHandler;
	}
	
	public int getPrefetchCount()
	{
		return this.prefetchCount;
	}
	
	/**
	 * Optimization: This needs to be invoked by the Receiver which Implements MessageReceiver.
	 * TODO: Make this contract more explicit
	 */
	public void setLastReceivedOffset(final String value)
	{
		this.lastReceivedOffset = value;
	}
	
	/*
	 * *****Important*****: if ReceiveHandler is passed to the Constructor - this receive shouldn't be invoked
	 */
	public CompletableFuture<Collection<Message>> receive()
	{
		if (!this.prefetchedMessages.isEmpty())
		{
			synchronized (this.prefetchedMessages)
			{
				if (!this.prefetchedMessages.isEmpty())
				{
					// return all available msgs to application-layer and send 'link-flow' frame for prefetch
					Collection<Message> returnMessages = this.prefetchedMessages;
					this.prefetchedMessages = new ConcurrentLinkedQueue<Message>();
					
					this.sendFlow();
					return CompletableFuture.completedFuture(returnMessages);
				}
			}
		}
		
		CompletableFuture<Collection<Message>> onReceive = new CompletableFuture<Collection<Message>>();
		this.pendingReceives.offer(new WorkItem<Collection<Message>>(onReceive, this.underlyingFactory.getOperationTimeout()));
		this.sendFlow();
		
		if (this.currentOperationTracker == null && this.pendingReceives.peek() != null)
		{
			synchronized (this.pendingReceives)
			{
				if (this.pendingReceives.peek() != null)
				{
					this.currentOperationTracker = this.pendingReceives.peek().getTimeoutTracker();
				}
			}
		}
		
		return onReceive;
	}
	
	void onOpenComplete(Exception exception)
	{
		synchronized (this.linkOpen)
		{
			if (!this.linkOpen.isDone())
			{
				if (exception == null)
				{
					this.linkOpen.complete(this);
				}
				else
				{
					this.linkOpen.completeExceptionally(exception);
				}
				
				this.currentOperationTracker = null;
				this.underlyingFactory.getRetryPolicy().resetRetryCount(this.underlyingFactory.getClientId());
			}
		}
	}
	
	// intended to be called by proton reactor handler 
	void onDelivery(Collection<Message> messages)
	{
		if (this.receiveHandler != null)
		{
			this.receiveHandler.onReceiveMessages(messages);
			this.currentOperationTracker = TimeoutTracker.create(this.underlyingFactory.getOperationTimeout());
			this.sendFlow();
		}		
		else
		{
			synchronized (this.pendingReceives)
			{
				if (this.pendingReceives.isEmpty())
				{
					this.prefetchedMessages.addAll(messages);
					this.currentOperationTracker = null;
				}
				else
				{
					this.pendingReceives.poll().getWork().complete(messages);
					this.currentOperationTracker = this.pendingReceives.peek() != null ? this.pendingReceives.peek().getTimeoutTracker() : null;
				}
			}
		}
		
		this.underlyingFactory.getRetryPolicy().resetRetryCount(this.getClientId());
	}
	
	void onError(ErrorCondition error)
	{		
		Exception completionException = ExceptionUtil.toException(error);
		
		// if CurrentOpTracker is null - no operation is in progress
		Duration remainingTime = this.currentOperationTracker == null 
						? Duration.ofSeconds(0)
						: (this.currentOperationTracker.elapsed().compareTo(this.underlyingFactory.getOperationTimeout()) > 0) 
								? Duration.ofSeconds(0) 
								: this.underlyingFactory.getOperationTimeout().minus(this.currentOperationTracker.elapsed());
		Duration retryInterval = this.underlyingFactory.getRetryPolicy().getNextRetryInterval(this.getClientId(), completionException, remainingTime);
		
		if (retryInterval != null)
		{
			this.scheduleRecreate(retryInterval);			
			return;
		}
		
		synchronized (this.linkOpen)
		{
			if (!this.linkOpen.isDone())
			{
				this.onOpenComplete(completionException);
				return;
			}
		}
		
		if (this.pendingReceives != null && !this.pendingReceives.isEmpty())
		{
			synchronized (this.pendingReceives)
			{
				if (this.pendingReceives != null && !this.pendingReceives.isEmpty())
					while (this.pendingReceives.peek() != null)
					{
						WorkItem<Collection<Message>> workItem = this.pendingReceives.poll();
						if (completionException instanceof ServiceBusException && ((ServiceBusException) completionException).getIsTransient())
						{
							workItem.getWork().complete(null);
						}
						else
						{
							workItem.getWork().completeExceptionally(completionException);
						}
					}
			}
		}
	}

	@Override
	public void close()
	{
		if (this.receiveLink != null && this.receiveLink.getLocalState() == EndpointState.ACTIVE)
		{
			this.receiveLink.close();
		}
	}
	
	private Receiver createReceiveLink(
								final Connection connection, 
								final String name, 
								final String receivePath, 
								final String offset,
								final boolean offsetInclusive)
	{	
		Source source = new Source();
        source.setAddress(receivePath);
        source.setFilter(Collections.singletonMap(
        		AmqpConstants.StringFilter,
        		new UnknownDescribedType(AmqpConstants.StringFilter, 
        				String.format("amqp.annotation.%s >%s '%s'", AmqpConstants.OffsetName, offsetInclusive ? "=" : StringUtil.EMPTY, offset))));
        
		Session ssn = connection.session();
		Receiver receiver = ssn.receiver(name);
		receiver.setSource(source);
		receiver.setTarget(new Target());
		
		// use explicit settlement via dispositions (not pre-settled)
        receiver.setSenderSettleMode(SenderSettleMode.UNSETTLED);
        receiver.setReceiverSettleMode(ReceiverSettleMode.SECOND);
        
        if (this.isEpochReceiver)
        {
        	receiver.setProperties(Collections.singletonMap(AmqpConstants.Epoch, (Object) this.epoch));
        }
        
        ssn.open();
        receiver.open();
                
        return receiver;
	}
	
	private void sendFlow()
	{
		if (this.receiveLink.getLocalState() == EndpointState.ACTIVE)
		{
			this.receiveLink.flow(this.prefetchCount);
		}
		else
		{
			this.scheduleRecreate(Duration.ofSeconds(0));
		}		
	}
	
	private void scheduleRecreate(Duration runAfter)
	{
		Timer.schedule(
				new Runnable()
				{
					@Override
					public void run()
					{
						MessageReceiver.this.receiveLink = MessageReceiver.this.createReceiveLink(
								MessageReceiver.this.underlyingFactory.getConnection(), 
								MessageReceiver.this.name, 
								MessageReceiver.this.receivePath, 
								MessageReceiver.this.lastReceivedOffset, 
								false);
						ReceiveLinkHandler handler = new ReceiveLinkHandler(name, MessageReceiver.this);
						BaseHandler.setHandler(MessageReceiver.this.receiveLink, handler);
						MessageReceiver.this.underlyingFactory.getRetryPolicy().incrementRetryCount(MessageReceiver.this.getClientId());
					}
				},
				runAfter,
				TimerType.OneTimeRun);
	}
	
	private void initializeLinkOpen(TimeoutTracker timeout)
	{
		this.linkOpen = new CompletableFuture<MessageReceiver>();
		
		// timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
		Timer.schedule(
			new Runnable()
				{
					public void run()
					{
						synchronized(MessageReceiver.this.linkOpen)
						{
							if (!MessageReceiver.this.linkOpen.isDone())
							{
								Exception operationTimedout = new TimeoutException(
										String.format(Locale.US, "Receive Link(%s) open() timed out", name));
								if (TRACE_LOGGER.isLoggable(Level.WARNING))
								{
									TRACE_LOGGER.log(Level.WARNING, 
											String.format(Locale.US, "message recever(linkName: %s, path: %s) open call timedout", name, MessageReceiver.this.receivePath), 
											operationTimedout);
								}
								
								MessageReceiver.this.linkOpen.completeExceptionally(operationTimedout);
							}
						}
					}
				}
			, timeout.remaining()
			, TimerType.OneTimeRun);
	}
}
