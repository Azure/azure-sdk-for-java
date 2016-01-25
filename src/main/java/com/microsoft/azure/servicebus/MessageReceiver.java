package com.microsoft.azure.servicebus;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;

import org.apache.qpid.proton.amqp.*;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.*;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import com.microsoft.azure.servicebus.amqp.ReceiveLinkHandler;

/**
 * Common Receiver that abstracts all amqp related details
 * translates event-driven reactor model into async receive Api
 * TODO: NEED AmqpObject state-machine for open/close
 */
public class MessageReceiver extends ClientEntity
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.ServiceBusClientTrace);
	private static final int PingFlowThreshold = 50;
	
	private final int prefetchCount; 
	private final ConcurrentLinkedQueue<WorkItem<Collection<Message>>> pendingReceives;
	private final MessagingFactory underlyingFactory;
	private final String name;
	private final String receivePath;
	private final Runnable onOperationTimedout;
	
	private ConcurrentLinkedQueue<Message> prefetchedMessages;
	private Receiver receiveLink;
	private CompletableFuture<MessageReceiver> linkOpen;
	private MessageReceiveHandler receiveHandler;
	
	private long epoch;
	private boolean isEpochReceiver;
	private Instant dateTime;
	private boolean offsetInclusive;
	
	private TimeoutTracker currentOperationTracker;
	private String lastReceivedOffset;
	private AtomicInteger pingFlowCount;
	private Instant lastReceivedAt;
	
	private boolean linkCreateScheduled;
	private Object linkCreateLock;
	
	/**
	 * @param connection Connection on which the MessageReceiver's receive Amqp link need to be created on.
	 * Connection has to be associated with Reactor before Creating a receiver on it.
	 */
	public static CompletableFuture<MessageReceiver> create(
			final MessagingFactory factory, 
			final String name, 
			final String recvPath, 
			final String offset,
			final boolean offsetInclusive,
			final Instant dateTime,
			final int prefetchCount,
			final long epoch,
			final boolean isEpochReceiver,
			final MessageReceiveHandler receiveHandler)
	{
		MessageReceiver msgReceiver = new MessageReceiver(factory, name, recvPath, offset, offsetInclusive, dateTime, prefetchCount, epoch, isEpochReceiver, receiveHandler);
		
		ReceiveLinkHandler handler = new ReceiveLinkHandler(name, msgReceiver);
		BaseHandler.setHandler(msgReceiver.receiveLink, handler);

		return msgReceiver.linkOpen;
	}
	
	private MessageReceiver(final MessagingFactory factory, 
			final String name, 
			final String recvPath, 
			final String offset,
			final boolean offsetInclusive,
			final Instant dateTime,
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
		this.pingFlowCount = new AtomicInteger();
		this.linkCreateLock = new Object();
		
		if (offset != null)
		{
			this.lastReceivedOffset = offset;
			this.offsetInclusive = offsetInclusive;
		}
		else
		{
			this.dateTime = dateTime;
		}
		
		this.receiveLink = this.createReceiveLink();
		this.currentOperationTracker = TimeoutTracker.create(factory.getOperationTimeout());
		this.initializeLinkOpen(this.currentOperationTracker);
		this.linkCreateScheduled = true;
		
		this.pendingReceives = new ConcurrentLinkedQueue<WorkItem<Collection<Message>>>();
		this.receiveHandler = receiveHandler;
		
		// onOperationTimeout delegate - per receive call
		this.onOperationTimedout = new Runnable()
		{
			public void run()
			{
				while(MessageReceiver.this.pendingReceives.peek() != null)
				{
					if (MessageReceiver.this.pendingReceives.peek().getTimeoutTracker().remaining().getSeconds() < ClientConstants.TimerTolerance.getSeconds())
					{
						MessageReceiver.this.pendingReceives.poll().getWork().complete(null);
						MessageReceiver.this.currentOperationTracker = null;
					}
					else 
					{
						MessageReceiver.this.currentOperationTracker = MessageReceiver.this.pendingReceives.peek().getTimeoutTracker();
						MessageReceiver.this.scheduleOperationTimer();
						break;
					}
				}
			}
		};
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
		if (this.receiveLink.getLocalState() != EndpointState.ACTIVE)
		{
			this.scheduleRecreate(Duration.ofSeconds(0));
		}
		
		if (!this.prefetchedMessages.isEmpty())
		{
			synchronized (this.prefetchedMessages)
			{
				if (!this.prefetchedMessages.isEmpty())
				{
					// return all available msgs to application-layer and send 'link-flow' frame for prefetch
					Collection<Message> returnMessages = this.prefetchedMessages;
					this.prefetchedMessages = new ConcurrentLinkedQueue<Message>();
					this.sendFlow(returnMessages.size());					
					return CompletableFuture.completedFuture(returnMessages);
				}
			}
		}
		
		CompletableFuture<Collection<Message>> onReceive = new CompletableFuture<Collection<Message>>();
		this.pendingReceives.offer(new WorkItem<Collection<Message>>(onReceive, this.underlyingFactory.getOperationTimeout()));
		
		if (this.currentOperationTracker == null && this.pendingReceives.peek() != null)
		{
			synchronized (this.pendingReceives)
			{
				if (this.pendingReceives.peek() != null)
				{
					if (Instant.now().isAfter(this.lastReceivedAt.plus(ClientConstants.AmqpLinkDetachTimeoutInMin, ChronoUnit.DAYS))
							&& this.pingFlowCount.get() < MessageReceiver.PingFlowThreshold)
					{
						this.sendFlow(1);
						this.pingFlowCount.incrementAndGet();
					}
					
					this.currentOperationTracker = this.pendingReceives.peek().getTimeoutTracker();
					this.scheduleOperationTimer();
				}
			}
		}
		
		return onReceive;
	}
	
	public void onOpenComplete(Exception exception)
	{
		synchronized (this.linkOpen)
		{
			if (!this.linkOpen.isDone())
			{
				if (exception == null)
				{
					this.lastReceivedAt = Instant.now();
					this.linkOpen.complete(this);
				}
				else
				{
					this.linkOpen.completeExceptionally(exception);
				}
				
				this.offsetInclusive = false; // re-open link always starts from the last received offset
				this.currentOperationTracker = null;
				this.underlyingFactory.getRetryPolicy().resetRetryCount(this.underlyingFactory.getClientId());
			}
		}
		
		synchronized (this.linkCreateLock)
		{
			this.linkCreateScheduled = false;
		}
	}
	
	// intended to be called by proton reactor handler 
	public void onDelivery(Collection<Message> messages)
	{
		this.lastReceivedAt = Instant.now();
		
		if (this.receiveHandler != null)
		{
			this.receiveHandler.onReceiveMessages(messages);
			this.currentOperationTracker = TimeoutTracker.create(this.underlyingFactory.getOperationTimeout());
			this.sendFlow(messages.size());
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
					WorkItem<Collection<Message>> currentReceive = this.pendingReceives.poll();
					this.currentOperationTracker = this.pendingReceives.peek() != null ? this.pendingReceives.peek().getTimeoutTracker() : null;
					currentReceive.getWork().complete(messages);
					this.sendFlow(messages.size());
				}
			}
		}
		
		this.underlyingFactory.getRetryPolicy().resetRetryCount(this.getClientId());
	}
	
	public void onError(ErrorCondition error)
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
	
	private void scheduleOperationTimer()
	{
		Timer.schedule(this.onOperationTimedout, this.currentOperationTracker.remaining(), TimerType.OneTimeRun);
	}
	
	private Receiver createReceiveLink()
	{	
		Source source = new Source();
        source.setAddress(receivePath);
        
        UnknownDescribedType filter = null;
        if (this.lastReceivedOffset == null)
        {
        	long totalMilliSeconds;
        	try
        	{
        		// TODO: how to handle the case when : this.dateTime - epoch doesn't fit in a long !
	        	totalMilliSeconds = this.dateTime.toEpochMilli();
	        }
        	catch(ArithmeticException ex)
        	{
        		totalMilliSeconds = Long.MAX_VALUE;
        	}
        	
            filter = new UnknownDescribedType(AmqpConstants.StringFilter,
        			String.format(AmqpConstants.AmqpAnnotationFormat, AmqpConstants.ReceivedAtAnnotationName, StringUtil.EMPTY, totalMilliSeconds));
        }
        else 
        {
        	filter =  new UnknownDescribedType(AmqpConstants.StringFilter,
            		String.format(AmqpConstants.AmqpAnnotationFormat, AmqpConstants.OffsetAnnotationName, this.offsetInclusive ? "=" : StringUtil.EMPTY, this.lastReceivedOffset));
        }
        
        Map<Symbol, UnknownDescribedType> filterMap = Collections.singletonMap(AmqpConstants.StringFilter, filter);
        source.setFilter(filterMap);
        
		Session ssn = this.underlyingFactory.getConnection().session();
		
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
	
	/**
	 * set the link credit; not thread-safe
	 */
	private void sendFlow(int credits)
	{
		if (this.receiveLink.getLocalState() == EndpointState.ACTIVE)
		{
			synchronized(this.pingFlowCount)
			{
				if (this.pingFlowCount.get() < credits)
				{
					this.receiveLink.flow(credits - this.pingFlowCount.get());
					this.pingFlowCount.set(0);
				}
				else 
				{
					this.pingFlowCount.set(this.pingFlowCount.get() - credits);
				}
			}
			
			if(TRACE_LOGGER.isLoggable(Level.FINE))
	        {
	        	TRACE_LOGGER.log(Level.FINE,
	        			String.format("MessageReceiver.sendFlow (linkname: %s), updated-link-credit: %s", this.receiveLink.getName(), this.receiveLink.getCredit()));
	        }
		}
		else
		{
			this.scheduleRecreate(Duration.ofSeconds(0));
		}		
	}
	
	private void scheduleRecreate(Duration runAfter)
	{
		synchronized (this.linkCreateLock) 
		{
			if (this.linkCreateScheduled)
			{
				return;
			}
			
			this.linkCreateScheduled = true;
			Timer.schedule(
				new Runnable()
				{
					@Override
					public void run()
					{
						MessageReceiver.this.receiveLink = MessageReceiver.this.createReceiveLink();
						ReceiveLinkHandler handler = new ReceiveLinkHandler(name, MessageReceiver.this);
						BaseHandler.setHandler(MessageReceiver.this.receiveLink, handler);
						MessageReceiver.this.underlyingFactory.getRetryPolicy().incrementRetryCount(MessageReceiver.this.getClientId());
					}
				},
				runAfter,
				TimerType.OneTimeRun);
		}
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
