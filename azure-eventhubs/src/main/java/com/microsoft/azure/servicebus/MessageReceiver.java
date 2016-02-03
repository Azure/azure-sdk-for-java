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
	private final Duration operationTimeout;
	
	private ConcurrentLinkedQueue<Message> prefetchedMessages;
	private Receiver receiveLink;
	private CompletableFuture<MessageReceiver> linkOpen;
	private CompletableFuture<Void> linkClose;
	private boolean closeCalled;
	
	private ReceiveHandler receiveHandler;
	private Object receiveHandlerLock;
	
	private long epoch;
	private boolean isEpochReceiver;
	private Instant dateTime;
	private boolean offsetInclusive;
	
	private TimeoutTracker currentOperationTracker;
	private String lastReceivedOffset;
	private AtomicInteger pingFlowCount;
	private Instant lastCommunicatedAt;
	
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
			final boolean isEpochReceiver)
	{
		MessageReceiver msgReceiver = new MessageReceiver(factory, name, recvPath, offset, offsetInclusive, dateTime, prefetchCount, epoch, isEpochReceiver);
		
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
			final boolean isEpochReceiver)
	{
		super(name);
		this.underlyingFactory = factory;
		this.operationTimeout = factory.getOperationTimeout();
		this.name = name;
		this.receivePath = recvPath;
		this.prefetchCount = prefetchCount;
		this.epoch = epoch;
		this.isEpochReceiver = isEpochReceiver;
		this.prefetchedMessages = new ConcurrentLinkedQueue<Message>();
		this.pingFlowCount = new AtomicInteger();
		this.linkCreateLock = new Object();
		this.receiveHandlerLock = new Object();
		this.linkClose = new CompletableFuture<Void>();

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
		
		this.linkOpen = new CompletableFuture<MessageReceiver>();
		this.scheduleLinkEventTimeout(this.currentOperationTracker, this.linkOpen, "open");
		this.linkCreateScheduled = true;
		
		this.pendingReceives = new ConcurrentLinkedQueue<WorkItem<Collection<Message>>>();
		
		// onOperationTimeout delegate - per receive call
		this.onOperationTimedout = new Runnable()
		{
			public void run()
			{
				while(MessageReceiver.this.pendingReceives.peek() != null)
				{
					WorkItem<Collection<Message>> topWorkItem = MessageReceiver.this.pendingReceives.peek();
					if (topWorkItem.getTimeoutTracker().remaining().getSeconds() < ClientConstants.TimerTolerance.getSeconds())
					{
						synchronized (MessageReceiver.this.pendingReceives)
						{
							WorkItem<Collection<Message>> dequedWorkItem = MessageReceiver.this.pendingReceives.poll();
							if (dequedWorkItem != null)
							{
								dequedWorkItem.getWork().complete(null);
							}
						}
					}
					else 
					{
						WorkItem<Collection<Message>> topWorkItemToBeTimedOut = MessageReceiver.this.pendingReceives.peek();
						MessageReceiver.this.currentOperationTracker = topWorkItemToBeTimedOut.getTimeoutTracker();
						MessageReceiver.this.scheduleOperationTimer();

						return;
					}
				}
				
				MessageReceiver.this.currentOperationTracker = null;
			}
		};
	}
	
	public int getPrefetchCount()
	{
		return this.prefetchCount;
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
		this.pendingReceives.offer(new WorkItem<Collection<Message>>(onReceive, this.operationTimeout));
		
		if (this.currentOperationTracker == null && this.pendingReceives.peek() != null)
		{
			synchronized (this.pendingReceives)
			{
				WorkItem<Collection<Message>> topWorkItem = this.pendingReceives.peek();
				if (topWorkItem != null)
				{
					this.sendPingFlow();
					
					this.currentOperationTracker = topWorkItem.getTimeoutTracker();
					this.scheduleOperationTimer();
				}
			}
		}
		
		return onReceive;
	}
	
	public void setReceiveHandler(final ReceiveHandler receiveHandler)
	{
		synchronized (this.receiveHandlerLock)
		{
			this.receiveHandler = receiveHandler;
		}
	}	
	
	public void onOpenComplete(Exception exception)
	{
		synchronized (this.linkOpen)
		{
			if (exception == null)
			{
				this.lastCommunicatedAt = Instant.now();
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
		
		synchronized (this.linkCreateLock)
		{
			this.linkCreateScheduled = false;
		}
	}
	
	// intended to be called by proton reactor handler 
	public void onDelivery(LinkedList<Message> messages)
	{
		this.lastCommunicatedAt = Instant.now();
		
		if (this.receiveHandler != null)
		{
			synchronized (this.receiveHandlerLock)
			{
				if (this.receiveHandler != null) 
				{
					assert messages != null && messages.size() > 0;
					
					try
					{
						this.receiveHandler.onReceiveMessages(messages);
						this.lastReceivedOffset = messages.getLast().getMessageAnnotations().getValue().get(AmqpConstants.Offset).toString();
					}
					catch (RuntimeException exception)
					{
						throw exception;
					}
					catch (Exception exception)
					{
						if (TRACE_LOGGER.isLoggable(Level.WARNING))
						{
							TRACE_LOGGER.log(Level.WARNING, 
									String.format(Locale.US, "%s: LinkName (%s), receiverpath (%s): encountered Exception (%s) while running user-code", 
											Instant.now().toString(), this.name, this.receivePath, exception.getClass()));
						}
						
						this.receiveHandler.onError(exception);
					}
					
					this.currentOperationTracker = TimeoutTracker.create(this.operationTimeout);
					this.sendFlow(messages.size());
				}
			}
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
				
				this.lastReceivedOffset = messages.getLast().getMessageAnnotations().getValue().get(AmqpConstants.Offset).toString();
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
						: (this.currentOperationTracker.elapsed().compareTo(this.operationTimeout) > 0) 
								? Duration.ofSeconds(0) 
								: this.operationTimeout.minus(this.currentOperationTracker.elapsed());
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
		
		if (completionException != null && this.receiveHandler != null)
		{
			synchronized (this.receiveHandlerLock)
			{
				if (this.receiveHandler != null)
				{
					if (TRACE_LOGGER.isLoggable(Level.WARNING))
					{
						TRACE_LOGGER.log(Level.WARNING, 
								String.format(Locale.US, "%s: LinkName (%s), receiverpath (%s): encountered Exception (%s) while receiving from ServiceBus service.", 
										Instant.now().toString(), this.getClientId(), this.receivePath, completionException.getClass()));
					}
					
					this.receiveHandler.onError(completionException);
				}
			}
		}
		else if (this.pendingReceives != null && !this.pendingReceives.isEmpty())
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
	
	private void sendPingFlow()
	{
		if (this.receiveLink.getLocalState() == EndpointState.ACTIVE)
		{
			if (Instant.now().isAfter(this.lastCommunicatedAt.plus(ClientConstants.AmqpLinkDetachTimeoutInMin, ChronoUnit.DAYS))
					&& this.pingFlowCount.get() < MessageReceiver.PingFlowThreshold)
			{
				this.receiveLink.flow(1);
				this.lastCommunicatedAt = Instant.now();
				if(TRACE_LOGGER.isLoggable(Level.FINE))
		        {
		        	TRACE_LOGGER.log(Level.FINE,
		        			String.format("MessageReceiver.sendPingFlow (linkname: %s), updated-link-credit: %s", this.receiveLink.getName(), this.receiveLink.getCredit()));
		        }
				
				this.pingFlowCount.incrementAndGet();
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
	
	private void scheduleLinkEventTimeout(final TimeoutTracker timeout, 
			@SuppressWarnings("rawtypes") final CompletableFuture linkEvent, 
			final String eventType)
	{
		// timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
		Timer.schedule(
			new Runnable()
				{
					public void run()
					{
						synchronized(linkEvent)
						{
							if (!linkEvent.isDone())
							{
								Exception operationTimedout = new TimeoutException(String.format(Locale.US, "Receive Link(%s) %s() timed out", name, eventType));
								if (TRACE_LOGGER.isLoggable(Level.WARNING))
								{
									TRACE_LOGGER.log(Level.WARNING, 
											String.format(Locale.US, "message recever(linkName: %s, path: %s) %s call timedout", name, MessageReceiver.this.receivePath, eventType), 
											operationTimedout);
								}
								
								linkEvent.completeExceptionally(operationTimedout);
							}
						}
					}
				}
			, timeout.remaining()
			, TimerType.OneTimeRun);
	}

	public void onClose()
	{
		synchronized (this.linkClose)
		{
			if (this.closeCalled)
			{
				this.linkClose.complete(null);
			}
			else
			{
				this.onError(new ErrorCondition(null, null));
			}
		}
	}
	
	@Override
	public CompletableFuture<Void> closeAsync()
	{
		this.closeInternal();
		return this.linkClose;
	}

	public void close() throws ServiceBusException
	{
		if (this.receiveLink != null && this.receiveLink.getLocalState() != EndpointState.CLOSED && !this.closeCalled)
		{
			try
			{
				this.closeAsync().get();
			}
			catch (InterruptedException | ExecutionException exception)
			{
				throw ServiceBusException.create(true, exception);
			}
		}
	}
	
	private void closeInternal()
	{
		synchronized (this.linkClose)
		{
			if (!this.closeCalled)
			{
				this.receiveLink.close();
				this.scheduleLinkEventTimeout(TimeoutTracker.create(this.operationTimeout), this.linkClose, "close");
				this.closeCalled = true;
			}
		}
	}
}
