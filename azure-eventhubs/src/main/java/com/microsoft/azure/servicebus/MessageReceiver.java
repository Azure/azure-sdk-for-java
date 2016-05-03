/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import com.microsoft.azure.servicebus.amqp.IAmqpReceiver;
import com.microsoft.azure.servicebus.amqp.ReceiveLinkHandler;
import com.microsoft.azure.servicebus.amqp.SessionHandler;

/**
 * Common Receiver that abstracts all amqp related details
 * translates event-driven reactor model into async receive Api
 */
public class MessageReceiver extends ClientEntity implements IAmqpReceiver, IErrorContextProvider
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	private static final Duration MINIMUM_RECEIVE_TIMER = Duration.ofSeconds(2);

	private final ConcurrentLinkedQueue<WorkItem<Collection<Message>>> pendingReceives;
	private final MessagingFactory underlyingFactory;
	private final ITimeoutErrorHandler stuckTransportHandler;
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
	
	private boolean linkCreateScheduled;
	private Object linkCreateLock;
	private Exception lastKnownLinkError;
	
	private int nextCreditToFlow;
	private Object flowSync;
	
	private MessageReceiver(final MessagingFactory factory,
			final ITimeoutErrorHandler stuckTransportHandler,
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
		this.stuckTransportHandler = stuckTransportHandler;
		this.operationTimeout = factory.getOperationTimeout();
		this.receivePath = recvPath;
		this.prefetchCount = prefetchCount;
		this.epoch = epoch;
		this.isEpochReceiver = isEpochReceiver;
		this.prefetchedMessages = new ConcurrentLinkedQueue<Message>();
		this.linkCreateLock = new Object();
		this.linkClose = new CompletableFuture<Void>();
		this.lastKnownLinkError = null;
		this.flowSync = new Object();

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
				boolean workItemTimedout = false;
				while((topWorkItem = MessageReceiver.this.pendingReceives.peek()) != null)
				{
					if (topWorkItem.getTimeoutTracker().remaining().getSeconds() <= 0)
					{
						WorkItem<Collection<Message>> dequedWorkItem = MessageReceiver.this.pendingReceives.poll();
						if (dequedWorkItem != null)
						{
							workItemTimedout = true;
							dequedWorkItem.getWork().complete(null);
						}
						else
							break;
					}
					else
					{
						MessageReceiver.this.scheduleOperationTimer(topWorkItem.getTimeoutTracker());
						break;
					}
				}
				
				if (workItemTimedout)
				{
					// workaround to push the sendflow-performative to reactor
					MessageReceiver.this.receiveLink.flow(0);
					
					// we have a known issue with proton libraries where transport layer is stuck while Sending Flow
					// to workaround this - we built a mechanism to reset the transport whenever we encounter this
					// https://issues.apache.org/jira/browse/PROTON-1185
					MessageReceiver.this.stuckTransportHandler.reportTimeoutError();
				}
			}
		};
	}
	
	
	// @param connection Connection on which the MessageReceiver's receive Amqp link need to be created on.
	// Connection has to be associated with Reactor before Creating a receiver on it.
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
		this.prefetchCount = value;
	}
		
	public CompletableFuture<Collection<Message>> receive()
	{
		List<Message> returnMessages = this.receiveCore();
		
		if (returnMessages != null)
		{
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
		
		return onReceive;
	}
	
	public List<Message> receiveCore()
	{
		List<Message> returnMessages = null;
		Message currentMessage = null;
		while ((currentMessage = this.pollPrefetchQueue()) != null) 
		{
			if (returnMessages == null)
			{
				returnMessages = new LinkedList<Message>();
			}
			
			returnMessages.add(currentMessage);
			if (returnMessages.size() >= this.prefetchCount)
			{
				break;
			}
		}
		
		return returnMessages;
	}
	
	public void onOpenComplete(Exception exception)
	{
		if (exception == null)
		{
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
				int pendingPrefetch = this.prefetchCount - this.prefetchedMessages.size();
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

		this.stuckTransportHandler.resetTimeoutErrorTracking();
		synchronized (this.linkCreateLock)
		{
			this.linkCreateScheduled = false;
		}
	}
	
	// intended to be invoked by proton reactor handler - upon delivery 
	public void onReceiveComplete(Message message)
	{
		this.prefetchedMessages.add(message);
		
		this.underlyingFactory.getRetryPolicy().resetRetryCount(this.getClientId());
		this.stuckTransportHandler.resetTimeoutErrorTracking();
		
		WorkItem<Collection<Message>> currentReceive = this.pendingReceives.poll();
		if (currentReceive != null)
		{
			List<Message> returnMessages = this.receiveCore();
			CompletableFuture<Collection<Message>> future = currentReceive.getWork();
			future.complete(returnMessages);
		}
	}
	
	public void onError(ErrorCondition error)
	{		
		Exception completionException = ExceptionUtil.toException(error);
		this.onError(completionException);
	}
	
	public void onError(Exception exception)
	{
		exception.getStackTrace();
		
		this.lastKnownLinkError = exception;
		WorkItem<Collection<Message>> currentReceive = this.pendingReceives.peek();
		
		TimeoutTracker currentOperationTracker = currentReceive != null 
				? currentReceive.getTimeoutTracker() 
				: ((this.linkOpen != null && this.linkOpen.getWork() != null && !this.linkOpen.getWork().isDone()) 
						? this.linkOpen.getTimeoutTracker() : new TimeoutTracker(this.operationTimeout, true));
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
        catch (java.util.concurrent.TimeoutException exception)
        {
        	this.onError(new TimeoutException("Connection creation timed out.", exception));
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
	
	// CONTRACT: message should be delivered to the caller of MessageReceiver.receive() only via Poll on prefetchqueue
	private Message pollPrefetchQueue()
	{
		Message message = this.prefetchedMessages.poll();
		if (message != null)
		{
			// message lastReceivedOffset should be up-to-date upon each poll - as recreateLink will depend on this 
			this.lastReceivedOffset = message.getMessageAnnotations().getValue().get(AmqpConstants.OFFSET).toString();
			this.sendFlow(1);
		}
		
		return message;
	}
	
	
	// set the link credit; thread-safe
	private void sendFlow(final int credits)
	{
		int tempFlow = 0;
		
		// slow down sending the flow - to make the protocol less-chat'y
		synchronized (this.flowSync)
		{
			this.nextCreditToFlow += credits;
			if (this.nextCreditToFlow >= this.prefetchCount)
			{
				tempFlow = this.nextCreditToFlow;
				this.receiveLink.flow(this.nextCreditToFlow);
				this.nextCreditToFlow = 0;
			}
		}
		
		if (tempFlow != 0)
		{
			if(TRACE_LOGGER.isLoggable(Level.FINE))
	        {
	        	TRACE_LOGGER.log(Level.FINE, String.format("linkname[%s], updated-link-credit[%s], sentCredits[%s]", this.receiveLink.getName(), this.receiveLink.getCredit(), credits));
	        }
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
							Exception operationTimedout = new TimeoutException(
									String.format(Locale.US, "%s operation on ReceiveLink(%s) to path(%s) timed out at %s.", "Open", MessageReceiver.this.receiveLink.getName(), MessageReceiver.this.receivePath, ZonedDateTime.now()),
									MessageReceiver.this.lastKnownLinkError);
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
								Exception operationTimedout = new TimeoutException(String.format(Locale.US, "%s operation on Receive Link(%s) timed out at %s", "Close", MessageReceiver.this.receiveLink.getName(), ZonedDateTime.now()));
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
	
	private void closeInternal()
	{
		synchronized (this.linkClose)
		{
			if (!this.closeCalled)
			{
				if (this.receiveLink != null && this.receiveLink.getLocalState() != EndpointState.CLOSED)
				{
					this.receiveLink.close();
					this.scheduleLinkCloseTimeout(TimeoutTracker.create(this.operationTimeout));
					this.closeCalled = true;
				}
				else
				{
					this.linkClose.complete(null);
				}
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
