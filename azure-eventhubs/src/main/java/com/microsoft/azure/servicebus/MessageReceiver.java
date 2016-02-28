/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.time.*;
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

import com.microsoft.azure.servicebus.amqp.*;

/**
 * Common Receiver that abstracts all amqp related details
 * translates event-driven reactor model into async receive Api
 */
public class MessageReceiver extends ClientEntity implements IAmqpReceiver, IErrorContextProvider
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	private static final int PING_FLOW_THRESHOLD = 2;
	private static final Duration RECEIVE_BATCH_INTERVAL = Duration.ofMillis(5);
	private static final double FLOW_THRESHOLD_PERCENT = (double) 1 / 3;
	private static final int MAX_FLOW_DEFAULT = 32;
	private static final Duration MINIMUM_RECEIVE_TIMER = Duration.ofSeconds(2);
	private static final int LINK_RESET_THRESHOLD = 3;
	
	private final ConcurrentLinkedQueue<WorkItem<Collection<Message>>> pendingReceives;
	private final MessagingFactory underlyingFactory;
	private final String name;
	private final String receivePath;
	private final Runnable onOperationTimedout;
	private final Duration operationTimeout;
	
	private int prefetchCount; 
	private ConcurrentLinkedQueue<Message> prefetchedMessages;
	private Receiver receiveLink;
	private WorkItem<MessageReceiver> linkOpen;
	private CompletableFuture<Void> linkClose;
	private boolean closeCalled;
	
	private long epoch;
	private boolean isEpochReceiver;
	private Instant dateTime;
	private boolean offsetInclusive;
	
	private String lastReceivedOffset;
	private AtomicInteger pingFlowCount;
	private AtomicInteger currentFlow;
	private Instant lastCommunicatedAt;
	
	private boolean linkCreateScheduled;
	private Object linkCreateLock;
	private Exception lastKnownLinkError;
	private boolean onDeliveryTimerSet;
	private Object deliverySync;
	private int linkResetCount;
	
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
		this.linkClose = new CompletableFuture<Void>();
		this.lastKnownLinkError = null;
		this.currentFlow = new AtomicInteger(0);
		this.onDeliveryTimerSet = false;
		this.deliverySync = new Object();
		this.linkResetCount = 0;
		
		if (offset != null)
		{
			this.lastReceivedOffset = offset;
			this.offsetInclusive = offsetInclusive;
		}
		else
		{
			this.dateTime = dateTime;
		}
		
		this.pendingReceives = new ConcurrentLinkedQueue<WorkItem<Collection<Message>>>();
		
		// onOperationTimeout delegate - per receive call
		this.onOperationTimedout = new Runnable()
		{
			public void run()
			{
				WorkItem<Collection<Message>> topWorkItem = null;
				while((topWorkItem = MessageReceiver.this.pendingReceives.peek()) != null)
				{
					if (topWorkItem.getTimeoutTracker().remaining().getSeconds() <= 0)
					{
						WorkItem<Collection<Message>> dequedWorkItem = MessageReceiver.this.pendingReceives.poll();
						dequedWorkItem.getWork().complete(null);
					}
					else
					{
						MessageReceiver.this.scheduleOperationTimer(topWorkItem.getTimeoutTracker());
						return;
					}
				}
			}
		};
	}
	
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
		MessageReceiver msgReceiver = new MessageReceiver(
			factory, 
			name, 
			recvPath, 
			offset, 
			offsetInclusive, 
			dateTime, 
			prefetchCount, 
			epoch, 
			isEpochReceiver);
		return msgReceiver.createLink();
	}
	
	private CompletableFuture<MessageReceiver> createLink()
	{
		this.linkOpen = new WorkItem<MessageReceiver>(new CompletableFuture<MessageReceiver>(), this.operationTimeout);
		this.scheduleLinkOpenTimeout(this.linkOpen.getTimeoutTracker());
		this.linkCreateScheduled = true;
		
		Timer.schedule(new Runnable() {
			@Override
			public void run()
			{
				MessageReceiver.this.receiveLink = MessageReceiver.this.createReceiveLink();
			}}, Duration.ofSeconds(0), TimerType.OneTimeRun);
		
		return this.linkOpen.getWork();
	}
	
	public int getPrefetchCount()
	{
		return this.prefetchCount;
	}
	
	public void setPrefetchCount(final int value)
	{
		int oldPrefetchCount = this.prefetchCount;
		this.prefetchCount = value;
		this.sendFlowInternal(value - oldPrefetchCount);
	}
	
	public final int getInitialPrefetchCount()
	{
		return (this.prefetchCount > MessageReceiver.PING_FLOW_THRESHOLD) 
				? this.prefetchCount - MessageReceiver.PING_FLOW_THRESHOLD 
				: this.prefetchCount;
	}
		
	public CompletableFuture<Collection<Message>> receive()
	{
		if (this.receiveLink.getLocalState() == EndpointState.CLOSED)
		{
			this.scheduleRecreate(Duration.ofMillis(1));
		}
		
		List<Message> returnMessages = null;
		Message currentMessage = null;
		while ((currentMessage = this.pollPrefetchQueue()) != null) 
		{
			if (returnMessages == null)
			{
				returnMessages = new LinkedList<Message>();
			}
			
			returnMessages.add(currentMessage);
			if (returnMessages.size() >= this.getInitialPrefetchCount())
			{
				break;
			}
		}
		
		if (returnMessages != null)
		{
			this.sendFlow(returnMessages.size());
			return CompletableFuture.completedFuture((Collection<Message>) returnMessages);				
		}
		
		if (this.operationTimeout.compareTo(MessageReceiver.MINIMUM_RECEIVE_TIMER) <= 0)
		{
			return CompletableFuture.completedFuture(null);
		}		
		
		if (this.pendingReceives.isEmpty())
		{
			this.scheduleOperationTimer(TimeoutTracker.create(this.operationTimeout));
		}
		
		CompletableFuture<Collection<Message>> onReceive = new CompletableFuture<Collection<Message>>();
		this.pendingReceives.offer(new WorkItem<Collection<Message>>(onReceive, this.operationTimeout));
		
		this.sendPingFlow();
		
		return onReceive;
	}
	
	public int getPingFlowThreshold()
	{
		return (this.prefetchCount > MessageReceiver.PING_FLOW_THRESHOLD) ? MessageReceiver.PING_FLOW_THRESHOLD : 0; 
	}
	
	public void onOpenComplete(Exception exception)
	{
		if (exception == null)
		{
			this.lastCommunicatedAt = Instant.now();
			if (this.linkOpen != null && !this.linkOpen.getWork().isDone())
			{
				this.linkOpen.getWork().complete(this);
			}
			
			this.lastKnownLinkError = null;
			
			// re-open link always starts from the last received offset
			this.offsetInclusive = false;
			this.underlyingFactory.getRetryPolicy().resetRetryCount(this.underlyingFactory.getClientId());
			
			if (this.receiveLink.getCredit() == 0)
			{
				int pendingPrefetch = this.getInitialPrefetchCount() - this.prefetchedMessages.size();
				this.pingFlowCount.set(0);
				this.sendFlow(pendingPrefetch);
			}
		}
		else
		{
			if (this.linkOpen != null && !this.linkOpen.getWork().isDone())
			{
				ExceptionUtil.completeExceptionally(this.linkOpen.getWork(), exception, this);
			}
			
			this.lastKnownLinkError = exception;
		}
		
		synchronized (this.linkCreateLock)
		{
			this.linkCreateScheduled = false;
		}
	}
	
	// intended to be called by proton reactor handler 
	public void onReceiveComplete(Message message)
	{
		this.lastCommunicatedAt = Instant.now();
		this.prefetchedMessages.add(message);
		
		if (!this.onDeliveryTimerSet)
		{
			synchronized(this.deliverySync)
			{
				if (!this.onDeliveryTimerSet && !this.pendingReceives.isEmpty())
				{
					this.onDeliveryTimerSet = true;
			
					Timer.schedule(new Runnable() 
					{
						@Override
						public void run()
						{
							if (MessageReceiver.this.prefetchedMessages.size() == 0)
							{
								return;
							}
							
							WorkItem<Collection<Message>> currentReceive = MessageReceiver.this.pendingReceives.poll();
							LinkedList<Message> returnMessages = null;
							if (currentReceive != null)
							{
								// Receive Api contract is to return null if no messages
								Message message = MessageReceiver.this.pollPrefetchQueue();
								if (message != null)
								{
									returnMessages = new LinkedList<Message>();
									
									do 
									{
										returnMessages.add(message);	
									}while(returnMessages.size() < MessageReceiver.this.getInitialPrefetchCount()
											&& (message = MessageReceiver.this.pollPrefetchQueue()) != null);
									
									MessageReceiver.this.sendFlow(returnMessages.size());
								}
								
								CompletableFuture<Collection<Message>> future = currentReceive.getWork();
								future.complete(returnMessages);
							}
							
							synchronized(MessageReceiver.this.deliverySync)
							{
								MessageReceiver.this.onDeliveryTimerSet = false;
							}
						}
					}, MessageReceiver.RECEIVE_BATCH_INTERVAL, TimerType.OneTimeRun);
				}
			}
		}
	
		this.underlyingFactory.getRetryPolicy().resetRetryCount(this.getClientId());
	}
	
	public void onError(ErrorCondition error)
	{		
		Exception completionException = ExceptionUtil.toException(error);
		this.onError(completionException);
	}
	
	public void onError(Exception exception)
	{
		WorkItem<Collection<Message>> currentReceive = this.pendingReceives.peek();
		
		TimeoutTracker currentOperationTracker = currentReceive != null 
				? currentReceive.getTimeoutTracker() 
				: (this.linkOpen.getWork().isDone() ? null : this.linkOpen.getTimeoutTracker());
		Duration remainingTime = currentOperationTracker == null 
						? Duration.ofSeconds(0)
						: (currentOperationTracker.elapsed().compareTo(this.operationTimeout) > 0) 
								? Duration.ofSeconds(0) 
								: this.operationTimeout.minus(currentOperationTracker.elapsed());
		Duration retryInterval = this.underlyingFactory.getRetryPolicy().getNextRetryInterval(this.getClientId(), exception, remainingTime);
		
		if (retryInterval != null)
		{
			if (this.receiveLink.getLocalState() != EndpointState.CLOSED)
			{
				this.receiveLink.close();
			}
			
			this.scheduleRecreate(retryInterval);			
			return;
		}
		
		this.onOpenComplete(exception);
		WorkItem<Collection<Message>> workItem = null;

		while ((workItem = this.pendingReceives.poll()) != null)
		{
			CompletableFuture<Collection<Message>> future = workItem.getWork();
			if (exception instanceof ServiceBusException && ((ServiceBusException) exception).getIsTransient())
			{
				future.complete(null);
			}
			else
			{
				ExceptionUtil.completeExceptionally(future, exception, this);
			}
		}
	}
	
	private void scheduleOperationTimer(TimeoutTracker tracker)
	{
		if (tracker != null)
		{
			Timer.schedule(this.onOperationTimedout, tracker.remaining(), TimerType.OneTimeRun);
		}
	}
	
	private Receiver createReceiveLink()
	{	
		Connection connection = null;
        
        try
		{
			connection = this.underlyingFactory.getConnection().get(this.operationTimeout.getSeconds(), TimeUnit.SECONDS);
		}
		catch (InterruptedException|ExecutionException exception)
		{
			Throwable throwable = exception.getCause();
			if (throwable != null && throwable instanceof Exception)
			{
				this.onError((Exception) exception.getCause());
			}
			
			if (exception instanceof InterruptedException)
			{
				Thread.currentThread().interrupt();
			}
			
			return null;
		}
        catch (TimeoutException exception)
        {
        	this.onError(new ServiceBusException(true, "Connection creation timed out.", exception));
        	return null;
        }
        
        if (connection == null || connection.getLocalState() == EndpointState.CLOSED)
        {
        	return null;
        }
        
        Source source = new Source();
        source.setAddress(receivePath);
        
        UnknownDescribedType filter = null;
        if (this.lastReceivedOffset == null)
        {
        	long totalMilliSeconds;
        	try
        	{
        		totalMilliSeconds = this.dateTime.toEpochMilli();
	        }
        	catch(ArithmeticException ex)
        	{
        		totalMilliSeconds = Long.MAX_VALUE;
        		if(TRACE_LOGGER.isLoggable(Level.WARNING))
		        {
		        	TRACE_LOGGER.log(Level.WARNING,
		        			String.format("linkname[%s], linkPath[%s], warning[starting receiver from epoch+Long.Max]", this.receiveLink.getName(), this.receivePath, this.receiveLink.getCredit()));
		        }
        	}
        	
            filter = new UnknownDescribedType(AmqpConstants.STRING_FILTER,
        			String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.RECEIVED_AT_ANNOTATION_NAME, StringUtil.EMPTY, totalMilliSeconds));
        }
        else 
        {
        	this.prefetchedMessages.clear();
        	if(TRACE_LOGGER.isLoggable(Level.FINE))
	        {
	        	TRACE_LOGGER.log(Level.FINE, String.format("action[recreateReceiveLink], offset[%s], offsetInclusive[%s]", this.lastReceivedOffset, this.offsetInclusive));
	        }
        	
        	filter =  new UnknownDescribedType(AmqpConstants.STRING_FILTER,
            		String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.OFFSET_ANNOTATION_NAME, this.offsetInclusive ? "=" : StringUtil.EMPTY, this.lastReceivedOffset));
        }
        
        Map<Symbol, UnknownDescribedType> filterMap = Collections.singletonMap(AmqpConstants.STRING_FILTER, filter);
        source.setFilter(filterMap);
        
		Session session = connection.session();
		session.setIncomingCapacity(Integer.MAX_VALUE);
		session.open();
        BaseHandler.setHandler(session, new SessionHandler(this.receivePath));
        
		String receiveLinkName = StringUtil.getRandomString();
		receiveLinkName = receiveLinkName.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(connection.getRemoteContainer());
		Receiver receiver = session.receiver(receiveLinkName);
		receiver.setSource(source);
		receiver.setTarget(new Target());
		
		// use explicit settlement via dispositions (not pre-settled)
        receiver.setSenderSettleMode(SenderSettleMode.UNSETTLED);
        receiver.setReceiverSettleMode(ReceiverSettleMode.SECOND);
        
        if (this.isEpochReceiver)
        {
        	receiver.setProperties(Collections.singletonMap(AmqpConstants.EPOCH, (Object) this.epoch));
        }
        
        ReceiveLinkHandler handler = new ReceiveLinkHandler(this);
        BaseHandler.setHandler(receiver, handler);
        this.underlyingFactory.registerForConnectionError(receiver);
        
        receiver.open();
        
        return receiver;
	}
	
	private Message pollPrefetchQueue()
	{
		// message should be delivered only via Poll on prefetchqueue
		// message lastReceivedOffset should be update upon each poll - as recreateLink will depend on this 
		Message message = this.prefetchedMessages.poll();
		if (message != null)
		{
			this.lastReceivedOffset = message.getMessageAnnotations().getValue().get(AmqpConstants.OFFSET).toString();
		}
		
		return message;
	}
	
	/**
	 * set the link credit; not thread-safe
	 */
	private void sendFlow(int credits)
	{
		if (this.receiveLink.getLocalState() != EndpointState.CLOSED)
		{
			int currentPingFlow = this.pingFlowCount.get();
			if (currentPingFlow > 0)
			{
				if (currentPingFlow < credits)
				{
					this.sendFlowInternal(credits - currentPingFlow);
					this.pingFlowCount.set(0);
				}
				else 
				{
					this.pingFlowCount.set(currentPingFlow - credits);
				}
			}
			else if (credits > 0)
			{
				this.sendFlowInternal(credits);
			}
		}
		else
		{
			this.scheduleRecreate(Duration.ofMillis(1));
		}		
	}
	
	/**
	 * slow down sending the flow - to make the protocol less-chat'y
	 * will not send any flow if totalCredits=0 (@param credits + currentLinkCredit)
	 */
	private void sendFlowInternal(int credits)
	{
		int finalFlow = this.currentFlow.addAndGet(credits);
		int flowThreshold = (int) Math.min(this.getInitialPrefetchCount() * MessageReceiver.FLOW_THRESHOLD_PERCENT, MessageReceiver.MAX_FLOW_DEFAULT);
		if (finalFlow > 0 && finalFlow > flowThreshold)
		{
			this.receiveLink.flow(this.currentFlow.getAndSet(0));
			this.lastCommunicatedAt = Instant.now();
			
			if(TRACE_LOGGER.isLoggable(Level.FINE))
	        {
	        	TRACE_LOGGER.log(Level.FINE,
	        			String.format("linkname[%s], updated-link-credit[%s], sendCredits[%s]", this.receiveLink.getName(), this.receiveLink.getCredit(), finalFlow));
	        }
		}
	}
	
	private void sendPingFlow()
	{
		if (this.receiveLink.getLocalState() != EndpointState.CLOSED)
		{
			if (this.pingFlowCount.get() < this.getPingFlowThreshold())
			{
				if (Instant.now().isAfter(this.lastCommunicatedAt.plus(this.operationTimeout)))
				{
					this.pingFlowCount.incrementAndGet();
					
					// Proton-j library needs to expose the sending flow with echo=true; workaround until then
					this.receiveLink.flow(1);
					this.lastCommunicatedAt = Instant.now();
	
					if(TRACE_LOGGER.isLoggable(Level.FINE))
			        {
			        	TRACE_LOGGER.log(Level.FINE,
			        			String.format("linkname[%s], linkPath[%s], updated-link-credit[%s]", this.receiveLink.getName(), this.receivePath, this.receiveLink.getCredit()));
			        }
				}
				else
				{
					this.sendFlowInternal(0);
				}
			}
			else if (this.receiveLink.getLocalState() != EndpointState.CLOSED)
			{
				String action = null;
				if (this.linkResetCount < MessageReceiver.LINK_RESET_THRESHOLD)
				{
					this.receiveLink.close();
					action = "detectedReceiveStuck-closingLink";
					this.linkResetCount++;
				}
				else 
				{
					this.underlyingFactory.resetConnection();
					this.linkResetCount = 0;
				}
				
				if(TRACE_LOGGER.isLoggable(Level.FINE))
		        {
		        	TRACE_LOGGER.log(Level.FINE,
		        			String.format("linkname[%s], linkPath[%s], linkCredit[%s], action[%s]", this.receiveLink.getName(), this.receivePath, this.receiveLink.getCredit(), action));
		        }
			}
		}
		else
		{
			this.scheduleRecreate(Duration.ofMillis(1));
		}
	}
	
	/**
	 *  Before invoking this - this.receiveLink is expected to be closed
	 */
	private void scheduleRecreate(Duration runAfter)
	{
		synchronized (this.linkCreateLock) 
		{
			if (this.linkCreateScheduled)
			{
				return;
			}
			
			this.linkCreateScheduled = true;
		}
		
		Timer.schedule(
			new Runnable()
			{
				@Override
				public void run()
				{
					if (MessageReceiver.this.receiveLink.getLocalState() != EndpointState.CLOSED)
					{
						return;
					}
					
					Receiver receiver = MessageReceiver.this.createReceiveLink();
					if (receiver != null)
					{
						Receiver oldReceiver = MessageReceiver.this.receiveLink;
						MessageReceiver.this.underlyingFactory.deregisterForConnectionError(oldReceiver);
						
						MessageReceiver.this.receiveLink = receiver;
					}
					else
					{
						synchronized (MessageReceiver.this.linkCreateLock) 
						{
							MessageReceiver.this.linkCreateScheduled = false;
						}
					}
					
					MessageReceiver.this.underlyingFactory.getRetryPolicy().incrementRetryCount(MessageReceiver.this.getClientId());
				}
			},
			runAfter,
			TimerType.OneTimeRun);
	}
	
	private void scheduleLinkOpenTimeout(final TimeoutTracker timeout)
	{
		// timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
		Timer.schedule(
			new Runnable()
				{
					public void run()
					{
						if (!linkOpen.getWork().isDone())
						{
							Exception cause = MessageReceiver.this.lastKnownLinkError;
							Exception operationTimedout = new ServiceBusException(
									cause != null && cause instanceof ServiceBusException ? ((ServiceBusException) cause).getIsTransient() : ClientConstants.DEFAULT_IS_TRANSIENT,
									String.format(Locale.US, "ReceiveLink(%s) %s() on path(%s) timed out", MessageReceiver.this.receiveLink.getName(), "Open", MessageReceiver.this.receivePath),
									cause);
							if (TRACE_LOGGER.isLoggable(Level.WARNING))
							{
								TRACE_LOGGER.log(Level.WARNING, 
										String.format(Locale.US, "message recever(linkName: %s, path: %s) %s call timedout", MessageReceiver.this.receiveLink.getName(), MessageReceiver.this.receivePath, "Open"), 
										operationTimedout);
							}
							
							ExceptionUtil.completeExceptionally(linkOpen.getWork(), operationTimedout, MessageReceiver.this);
						}
					}
				}
			, timeout.remaining()
			, TimerType.OneTimeRun);
	}
	
	private void scheduleLinkCloseTimeout(final TimeoutTracker timeout)
	{
		// timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
		Timer.schedule(
			new Runnable()
				{
					public void run()
					{
						synchronized(linkClose)
						{
							if (!linkClose.isDone())
							{
								Exception operationTimedout = new TimeoutException(String.format(Locale.US, "Receive Link(%s) %s() timed out", MessageReceiver.this.receiveLink.getName(), "Close"));
								if (TRACE_LOGGER.isLoggable(Level.WARNING))
								{
									TRACE_LOGGER.log(Level.WARNING, 
											String.format(Locale.US, "message recever(linkName: %s, path: %s) %s call timedout", MessageReceiver.this.receiveLink.getName(), MessageReceiver.this.receivePath, "Close"), 
											operationTimedout);
								}
								
								ExceptionUtil.completeExceptionally(linkClose, operationTimedout, MessageReceiver.this);
							}
						}
					}
				}
			, timeout.remaining()
			, TimerType.OneTimeRun);
	}

	public void onClose(ErrorCondition condition)
	{
		synchronized (this.linkClose)
		{
			if (this.closeCalled)
			{
				this.linkClose.complete(null);
				this.closeCalled = false;
				return;
			}
		}
		
		if (condition == null)
		{
			this.onError(new ServiceBusException(true, 
					String.format(Locale.US,"Closing the link. LinkName(%s), EntityPath(%s)", this.receiveLink.getName(), this.receivePath)));
		}
		else
		{
			this.onError(condition);
		}
	}
	
	@Override
	public CompletableFuture<Void> close()
	{
		this.closeInternal();
		return this.linkClose;
	}

	public void closeSync() throws ServiceBusException
	{
		if (this.receiveLink != null && this.receiveLink.getLocalState() != EndpointState.CLOSED && !this.closeCalled)
		{
			try
			{
				this.close().get();
			}
			catch (InterruptedException | ExecutionException exception)
			{
				throw new ServiceBusException(true, String.format(Locale.US, "Close link failed. getCause() could present more details."), exception);
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
				this.scheduleLinkCloseTimeout(TimeoutTracker.create(this.operationTimeout));
				this.closeCalled = true;
			}
		}
	}
	
	@Override
	public ErrorContext getContext()
	{
		final boolean isLinkOpened = this.linkOpen != null && this.linkOpen.getWork().isDone();
		final String referenceId = this.receiveLink != null && this.receiveLink.getRemoteProperties() != null && this.receiveLink.getRemoteProperties().containsKey(ClientConstants.TRACKING_ID_PROPERTY)
				? this.receiveLink.getRemoteProperties().get(ClientConstants.TRACKING_ID_PROPERTY).toString()
				: ((this.receiveLink != null) ? this.receiveLink.getName(): null);

		ReceiverContext errorContext = new ReceiverContext(this.underlyingFactory != null ? this.underlyingFactory.getHostName() : null,
				this.receivePath,
				referenceId,
			 	isLinkOpened ? new Long(this.lastReceivedOffset) : null, 
			 	isLinkOpened ? this.prefetchCount : null, 
			 	isLinkOpened ? this.receiveLink.getCredit(): null, 
			 	isLinkOpened && this.prefetchedMessages != null ? this.prefetchedMessages.size(): null, 
			 	this.isEpochReceiver);
		
		return errorContext;
	}	
}
