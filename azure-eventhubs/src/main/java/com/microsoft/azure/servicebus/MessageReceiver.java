package com.microsoft.azure.servicebus;

import java.time.*;
import java.time.temporal.ChronoUnit;
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
	private static final double PING_FLOW_RESERVE_PERCENT = 10;
	
	private final int prefetchCount; 
	private final ConcurrentLinkedQueue<WorkItem<Collection<Message>>> pendingReceives;
	private final MessagingFactory underlyingFactory;
	private final String name;
	private final String receivePath;
	private final Runnable onOperationTimedout;
	private final Duration operationTimeout;
	
	private ConcurrentLinkedQueue<Message> prefetchedMessages;
	private Receiver receiveLink;
	private WorkItem<MessageReceiver> linkOpen;
	private CompletableFuture<Void> linkClose;
	private boolean closeCalled;
	
	private ReceiveHandler receiveHandler;
	private Object receiveHandlerLock;
	
	private long epoch;
	private boolean isEpochReceiver;
	private Instant dateTime;
	private boolean offsetInclusive;
	
	private String lastReceivedOffset;
	private AtomicInteger pingFlowCount;
	private Instant lastCommunicatedAt;
	
	private boolean linkCreateScheduled;
	private Object linkCreateLock;
	private Exception lastKnownLinkError;
	
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
		this.lastKnownLinkError = null;
		
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
						if (dequedWorkItem == topWorkItem)
						{
							dequedWorkItem.getWork().complete(null);
						}
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
	
	public final int getInitialPrefetchCount()
	{
		return (int) (this.prefetchCount * (100 - MessageReceiver.PING_FLOW_RESERVE_PERCENT) / 100);
	}
		
	/*
	 * *****Important*****: if ReceiveHandler is passed to the Constructor - this receive shouldn't be invoked
	 */
	public CompletableFuture<Collection<Message>> receive()
	{
		if (this.receiveLink.getLocalState() == EndpointState.CLOSED)
		{
			this.scheduleRecreate(Duration.ofSeconds(0));
		}
		
		List<Message> returnMessages = null;
		Message currentMessage = null;
		Message lastMessage = null;
		while ((currentMessage = this.prefetchedMessages.poll()) != null) 
		{
			if (returnMessages == null)
			{
				returnMessages = new LinkedList<Message>();
			}
			
			returnMessages.add(currentMessage);
			lastMessage = currentMessage;
		}
		
		if (returnMessages != null)
		{
			this.sendFlow(returnMessages.size());
			
			this.lastReceivedOffset = lastMessage.getMessageAnnotations().getValue().get(AmqpConstants.Offset).toString();
			return CompletableFuture.completedFuture((Collection<Message>) returnMessages);
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
		return (int)(this.prefetchCount * MessageReceiver.PING_FLOW_RESERVE_PERCENT / 100); 
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
		if (exception == null)
		{
			this.lastCommunicatedAt = Instant.now();
			if (this.linkOpen != null && !this.linkOpen.getWork().isDone())
				this.linkOpen.getWork().complete(this);
			
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
				this.linkOpen.getWork().completeExceptionally(exception);
			
			this.lastKnownLinkError = exception;
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
			ReceiveHandler localReceiveHandler = null;
			synchronized (this.receiveHandlerLock)
			{
				localReceiveHandler = this.receiveHandler;
			}
			
			if (localReceiveHandler != null) 
			{
				assert messages != null && messages.size() > 0;
				
				try
				{
					localReceiveHandler.onReceiveMessages(messages);
					this.lastReceivedOffset = messages.getLast().getMessageAnnotations().getValue().get(AmqpConstants.Offset).toString();
					this.underlyingFactory.getRetryPolicy().resetRetryCount(this.getClientId());
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
					
					localReceiveHandler.onError(exception);
				}
				
				this.sendFlow(messages.size());
				return;
			}		
		}		
	
		WorkItem<Collection<Message>> currentReceive = this.pendingReceives.poll();
		
		if (currentReceive == null)
		{
			this.prefetchedMessages.addAll(messages);
		}
		else 
		{
			this.sendFlow(messages.size());

			this.lastReceivedOffset = messages.getLast().getMessageAnnotations().getValue().get(AmqpConstants.Offset).toString();
			CompletableFuture<Collection<Message>> future = currentReceive.getWork();
			future.complete(messages);
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
			this.scheduleRecreate(retryInterval);			
			return;
		}
		
		this.onOpenComplete(exception);
		
		if (exception != null && this.receiveHandler != null)
		{
			synchronized (this.receiveHandlerLock)
			{
				if (this.receiveHandler != null)
				{
					if (TRACE_LOGGER.isLoggable(Level.WARNING))
					{
						TRACE_LOGGER.log(Level.WARNING, 
								String.format(Locale.US, "%s: LinkName (%s), receiverpath (%s): encountered Exception (%s) while receiving from ServiceBus service.", 
										Instant.now().toString(), this.receiveLink.getName(), this.receivePath, exception.getClass()));
					}
					
					this.receiveHandler.onError(exception);
				}
			}
		}
		else
		{
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
					future.completeExceptionally(exception);
				}
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
        
        Connection connection = null;
        
        try
		{
			connection = this.underlyingFactory.getConnectionAsync().get(this.operationTimeout.getSeconds(), TimeUnit.SECONDS);
		}
		catch (InterruptedException|ExecutionException exception)
		{
			Throwable throwable = exception.getCause();
			if (throwable != null && throwable instanceof Exception)
			{
				this.onError((Exception) exception.getCause());
			}
			
			return null;
		}
        catch (TimeoutException exception)
        {
        	this.onError(exception);
        	return null;
        }
    
		Session ssn = connection.session();
		ssn.open();
        
		String receiveLinkName = StringUtil.getRandomString();
		receiveLinkName = receiveLinkName.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(connection.getRemoteContainer());
		Receiver receiver = ssn.receiver(receiveLinkName);
		receiver.setSource(source);
		receiver.setTarget(new Target());
		
		// use explicit settlement via dispositions (not pre-settled)
        receiver.setSenderSettleMode(SenderSettleMode.UNSETTLED);
        receiver.setReceiverSettleMode(ReceiverSettleMode.SECOND);
        
        if (this.isEpochReceiver)
        {
        	receiver.setProperties(Collections.singletonMap(AmqpConstants.Epoch, (Object) this.epoch));
        }
        
        ReceiveLinkHandler handler = new ReceiveLinkHandler(this);
        BaseHandler.setHandler(receiver, handler);
        this.underlyingFactory.links.add(receiver);
        
        receiver.open();
        
        return receiver;
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
					this.receiveLink.flow(credits - currentPingFlow);
					this.pingFlowCount.set(0);
				}
				else 
				{
					this.pingFlowCount.set(currentPingFlow - credits);
				}
				
				if(TRACE_LOGGER.isLoggable(Level.FINE))
		        {
		        	TRACE_LOGGER.log(Level.FINE,
		        			String.format("MessageReceiver.sendFlow (linkname: %s), updated-link-credit: %s", this.receiveLink.getName(), this.receiveLink.getCredit()));
		        }
			}
			else if (credits > 0)
			{
				this.receiveLink.flow(credits);
			}
		}
		else
		{
			this.scheduleRecreate(Duration.ofSeconds(0));
		}		
	}
	
	private void sendPingFlow()
	{
		// TODO: Proton-j library needs to expose the sending flow with echo=true; a workaround until then
		if (this.receiveLink.getLocalState() != EndpointState.CLOSED)
		{
			int currentCredit = this.receiveLink.getCredit();
			if (currentCredit < this.getInitialPrefetchCount())
			{
				this.receiveLink.flow(this.getInitialPrefetchCount() - currentCredit);
				this.lastCommunicatedAt = Instant.now();
			}
			else if (currentCredit < this.getPrefetchCount() && Instant.now().isAfter(this.lastCommunicatedAt.plus(this.operationTimeout)))
			{
				if (this.pingFlowCount.get() < this.getPingFlowThreshold())
				{
					this.pingFlowCount.incrementAndGet();
					this.receiveLink.flow(1);
					this.lastCommunicatedAt = Instant.now();
					if(TRACE_LOGGER.isLoggable(Level.FINE))
			        {
			        	TRACE_LOGGER.log(Level.FINE,
			        			String.format("linkname[%s], linkPath[%s], updated-link-credit[%s]", this.receiveLink.getName(), this.receivePath, this.receiveLink.getCredit()));
			        }
				}
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
						MessageReceiver.this.underlyingFactory.links.remove(oldReceiver);
						oldReceiver.free();
						
						MessageReceiver.this.receiveLink = receiver;
					}
					
					synchronized (MessageReceiver.this.linkCreateLock) 
					{
						MessageReceiver.this.linkCreateScheduled = false;
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
							Exception operationTimedout = ServiceBusException.create(
									cause != null && cause instanceof ServiceBusException ? ((ServiceBusException) cause).getIsTransient() : ClientConstants.DefaultIsTransient,
									String.format(Locale.US, "ReceiveLink(%s) %s() on path(%s) timed out", MessageReceiver.this.receiveLink.getName(), "Open", MessageReceiver.this.receivePath),
									cause);
							if (TRACE_LOGGER.isLoggable(Level.WARNING))
							{
								TRACE_LOGGER.log(Level.WARNING, 
										String.format(Locale.US, "message recever(linkName: %s, path: %s) %s call timedout", MessageReceiver.this.receiveLink.getName(), MessageReceiver.this.receivePath, "Open"), 
										operationTimedout);
							}
							
							linkOpen.getWork().completeExceptionally(operationTimedout);
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
								
								linkClose.completeExceptionally(operationTimedout);
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
			this.onError(ServiceBusException.create(true, 
					String.format(Locale.US,"Closing the link. LinkName(%s), EntityPath(%s)", this.receiveLink.getName(), this.receivePath)));
		}
		else
			this.onError(condition);
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
				this.scheduleLinkCloseTimeout(TimeoutTracker.create(this.operationTimeout));
				this.closeCalled = true;
			}
		}
	}
}
