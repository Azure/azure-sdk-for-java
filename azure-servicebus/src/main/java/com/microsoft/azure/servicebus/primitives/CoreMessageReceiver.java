/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.microsoft.azure.servicebus.TransactionContext;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Modified;
import org.apache.qpid.proton.amqp.messaging.Outcome;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.amqp.DispatchHandler;
import com.microsoft.azure.servicebus.amqp.IAmqpReceiver;
import com.microsoft.azure.servicebus.amqp.ReceiveLinkHandler;
import com.microsoft.azure.servicebus.amqp.SessionHandler;

/*
 * Common Receiver that abstracts all amqp related details
 * translates event-driven reactor model into async receive Api
 */

// TODO Take a re-look at the choice of collections used. Some of them are overkill may be.
public class CoreMessageReceiver extends ClientEntity implements IAmqpReceiver, IErrorContextProvider
{
	private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(CoreMessageReceiver.class);
	private static final Duration LINK_REOPEN_TIMEOUT = Duration.ofMinutes(5); // service closes link long before this timeout expires
	private static final Duration RETURN_MESSAGES_DAEMON_WAKE_UP_INTERVAL = Duration.ofMillis(1); // Wakes up every 1 millisecond
	private static final Duration UPDATE_STATE_REQUESTS_DAEMON_WAKE_UP_INTERVAL = Duration.ofMillis(500); // Wakes up every 500 milliseconds
	private static final Duration ZERO_TIMEOUT_APPROXIMATION = Duration.ofMillis(200);
	private static final int CREDIT_FLOW_BATCH_SIZE = 50;// Arbitrarily chosen 50 to avoid sending too many flows in case prefetch count is large
	
	private final Object requestResonseLinkCreationLock = new Object();
	private final ConcurrentLinkedQueue<ReceiveWorkItem> pendingReceives;
	private final ConcurrentHashMap<String, UpdateStateWorkItem> pendingUpdateStateRequests;
	private final ConcurrentHashMap<String, Delivery> tagsToDeliveriesMap;
	private final MessagingFactory underlyingFactory;
	private final String receivePath;
	private final String sasTokenAudienceURI;
	private final Duration operationTimeout;
	private final CompletableFuture<Void> linkClose;
	private final Object prefetchCountSync;
	private final SettleModePair settleModePair;
	private final RetryPolicy retryPolicy;
	private int prefetchCount;
	private String sessionId;
	private boolean isSessionReceiver;
	private boolean isBrowsableSession;
	private Instant sessionLockedUntilUtc;
	private boolean isSessionLockLost;
	private ConcurrentLinkedQueue<MessageWithDeliveryTag> prefetchedMessages;
	private Receiver receiveLink;
	private RequestResponseLink requestResponseLink;
	private WorkItem<CoreMessageReceiver> linkOpen;
	private Duration factoryRceiveTimeout;

	private Exception lastKnownLinkError;
	private Instant lastKnownErrorReportedAt;
	private final AtomicInteger creditToFlow;
	private final AtomicInteger creditNeededtoServePendingReceives;
	private final AtomicInteger currentPrefetechedMessagesCount; // size() on concurrentlinkedqueue is o(n) operation
	private ScheduledFuture<?> sasTokenRenewTimerFuture;
	private CompletableFuture<Void> requestResponseLinkCreationFuture;
	private CompletableFuture<Void> receiveLinkReopenFuture;
	private final Runnable timedOutUpdateStateRequestsDaemon;
	private final Runnable returnMesagesLoopDaemon;
	private final ScheduledFuture<?> updateStateRequestsTimeoutChecker;
	private final ScheduledFuture<?> returnMessagesLoopRunner;
	private final MessagingEntityType entityType;
	
	// TODO Change onReceiveComplete to handle empty deliveries. Change onError to retry updateState requests.
	private CoreMessageReceiver(final MessagingFactory factory,
			final String name, 
			final String recvPath,
			final String sessionId,
			final int prefetchCount,
			final SettleModePair settleModePair,
			final MessagingEntityType entityType)
	{
		super(name);

		this.underlyingFactory = factory;
		this.operationTimeout = factory.getOperationTimeout();
		this.receivePath = recvPath;
		this.sasTokenAudienceURI = String.format(ClientConstants.SAS_TOKEN_AUDIENCE_FORMAT, factory.getHostName(), recvPath);
		this.sessionId = sessionId;
		this.isSessionReceiver = false;
		this.isBrowsableSession = false;
		this.prefetchCount = prefetchCount;
		this.settleModePair = settleModePair;
		this.prefetchedMessages = new ConcurrentLinkedQueue<MessageWithDeliveryTag>();
		this.linkClose = new CompletableFuture<Void>();
		this.lastKnownLinkError = null;
		this.factoryRceiveTimeout = factory.getOperationTimeout();
		this.prefetchCountSync = new Object();
		this.retryPolicy = factory.getRetryPolicy();
		this.pendingReceives = new ConcurrentLinkedQueue<ReceiveWorkItem>();
		 
		this.pendingUpdateStateRequests = new ConcurrentHashMap<>();
		this.tagsToDeliveriesMap = new ConcurrentHashMap<>();
		this.lastKnownErrorReportedAt = Instant.now();
		this.receiveLinkReopenFuture = null;
		this.creditToFlow = new AtomicInteger();
		this.creditNeededtoServePendingReceives = new AtomicInteger();
		this.currentPrefetechedMessagesCount = new AtomicInteger();
		this.entityType = entityType;
		
		this.timedOutUpdateStateRequestsDaemon = new Runnable() {
			@Override
			public void run() {
			    try
			    {
			        TRACE_LOGGER.trace("Starting '{}' core message receiver's internal loop to complete timed out update state requests.", CoreMessageReceiver.this.receivePath);
			        for(Map.Entry<String, UpdateStateWorkItem> entry : CoreMessageReceiver.this.pendingUpdateStateRequests.entrySet())
	                {
	                    Duration remainingTime = entry.getValue().getTimeoutTracker().remaining();
	                    if(remainingTime.isZero() || remainingTime.isNegative())
	                    {
	                        CoreMessageReceiver.this.pendingUpdateStateRequests.remove(entry.getKey());
	                        Exception exception = entry.getValue().getLastKnownException();
	                        if(exception == null)
	                        {
	                            exception = new TimeoutException("Request timed out.");
	                        }
	                        TRACE_LOGGER.error("UpdateState request timed out. Delivery:{}", entry.getKey(), exception);
	                        entry.getValue().getWork().completeExceptionally(exception);
	                    }
	                }
			        TRACE_LOGGER.trace("'{}' core message receiver's internal loop to complete timed out update state requests stopped.", CoreMessageReceiver.this.receivePath);
			    }
			    catch(Throwable e)
			    {
			        // Shouldn't throw any exception for the executor to run multiple times.. Should never come here
			    }
			}
		};
		
		// CONTRACT: message should be delivered to the caller of MessageReceiver.receive() only from prefetched messages
		this.returnMesagesLoopDaemon = new Runnable() {
            @Override
            public void run() {
                try
                {
                    TRACE_LOGGER.trace("Starting '{}' core message receiver's internal loop to return messages to waiting clients.", CoreMessageReceiver.this.receivePath);
                    while(!CoreMessageReceiver.this.prefetchedMessages.isEmpty())
                    {
                        ReceiveWorkItem currentReceive = CoreMessageReceiver.this.pendingReceives.poll();
                        if (currentReceive != null)
                        {
                            if(!currentReceive.getWork().isDone())
                            {
                                TRACE_LOGGER.debug("Returning the message received from '{}' to a pending receive request", CoreMessageReceiver.this.receivePath);
                                currentReceive.cancelTimeoutTask(false);
                                List<MessageWithDeliveryTag> messages = CoreMessageReceiver.this.receiveCore(currentReceive.getMaxMessageCount());
                                CoreMessageReceiver.this.reduceCreditForCompletedReceiveRequest(currentReceive.getMaxMessageCount());
                                AsyncUtil.completeFuture(currentReceive.getWork(), messages);
                            }
                        }
                        else
                        {
                            break;
                        }
                    }
                    TRACE_LOGGER.trace("'{}' core message receiver's internal loop to return messages to waiting clients stopped.", CoreMessageReceiver.this.receivePath);
                }
                catch(Throwable e)
                {
                    // Shouldn't throw any exception for the executor to run multiple times.. Should never come here
                }
            }
        };
		
		// As all update state requests have the same timeout, one timer is better than having one timer per request
		this.updateStateRequestsTimeoutChecker = Timer.schedule(timedOutUpdateStateRequestsDaemon, CoreMessageReceiver.UPDATE_STATE_REQUESTS_DAEMON_WAKE_UP_INTERVAL, TimerType.RepeatRun);
		// Scheduling it as a separate thread that wakes up at regular very short intervals.. Doesn't wait on incoming receive requests from callers or incoming deliveries from reactor 
		this.returnMessagesLoopRunner = Timer.schedule(returnMesagesLoopDaemon, CoreMessageReceiver.RETURN_MESSAGES_DAEMON_WAKE_UP_INTERVAL, TimerType.RepeatRun);
	}

	// Connection has to be associated with Reactor before Creating a receiver on it.
	@Deprecated
	public static CompletableFuture<CoreMessageReceiver> create(
			final MessagingFactory factory, 
			final String name, 
			final String recvPath,
			final int prefetchCount,
			final SettleModePair settleModePair)
	{
	    return create(factory, name, recvPath, prefetchCount, settleModePair, null);
	}
	
	@Deprecated
	public static CompletableFuture<CoreMessageReceiver> create(
			final MessagingFactory factory, 
			final String name, 
			final String recvPath,
			final String sessionId,
			final boolean isBrowsableSession,
			final int prefetchCount,
			final SettleModePair settleModePair)
	{
	    return create(factory, name, recvPath, sessionId, isBrowsableSession, prefetchCount, settleModePair, null);
	}
	
	public static CompletableFuture<CoreMessageReceiver> create(
			final MessagingFactory factory, 
			final String name, 
			final String recvPath,
			final int prefetchCount,
			final SettleModePair settleModePair,
			final MessagingEntityType entityType)
	{
	    TRACE_LOGGER.info("Creating core message receiver to '{}'", recvPath);
		CoreMessageReceiver msgReceiver = new CoreMessageReceiver(
				factory,
				name, 
				recvPath,
				null,
				prefetchCount,
				settleModePair,
				entityType);
		return msgReceiver.createLink();
	}
	
	public static CompletableFuture<CoreMessageReceiver> create(
			final MessagingFactory factory, 
			final String name, 
			final String recvPath,
			final String sessionId,
			final boolean isBrowsableSession,
			final int prefetchCount,
			final SettleModePair settleModePair,
			final MessagingEntityType entityType)
	{
	    TRACE_LOGGER.info("Creating core session receiver to '{}', sessionId '{}', browseonly session '{}'", recvPath, sessionId, isBrowsableSession);
		CoreMessageReceiver msgReceiver = new CoreMessageReceiver(
				factory,
				name, 
				recvPath,
				sessionId,
				prefetchCount,
				settleModePair,
				entityType);
		msgReceiver.isSessionReceiver = true;
		msgReceiver.isBrowsableSession = isBrowsableSession;
		return msgReceiver.createLink();
	}

	private CompletableFuture<CoreMessageReceiver> createLink()
	{
		this.linkOpen = new WorkItem<CoreMessageReceiver>(new CompletableFuture<CoreMessageReceiver>(), this.operationTimeout);
		this.scheduleLinkOpenTimeout(this.linkOpen.getTimeoutTracker());
		this.sendTokenAndSetRenewTimer(false).handleAsync((v, sasTokenEx) -> {
            if(sasTokenEx != null)
            {
                Throwable cause = ExceptionUtil.extractAsyncCompletionCause(sasTokenEx);
                TRACE_LOGGER.error("Sending SAS Token failed. ReceivePath:{}", this.receivePath, cause);
                AsyncUtil.completeFutureExceptionally(this.linkOpen.getWork(), cause);
            }
            else
            {
                try
                {
                    this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
                    {
                        @Override
                        public void onEvent()
                        {
                            CoreMessageReceiver.this.createReceiveLink();
                        }
                    });
                }
                catch (IOException ioException)
                {
                    this.cancelSASTokenRenewTimer();
                    AsyncUtil.completeFutureExceptionally(this.linkOpen.getWork(), new ServiceBusException(false, "Failed to create Receiver, see cause for more details.", ioException));
                }
            }
            
            return null;
        });
		
		return this.linkOpen.getWork();
	}
	
	private CompletableFuture<Void> createRequestResponseLinkAsync()
	{
	    synchronized (this.requestResonseLinkCreationLock) {
            if(this.requestResponseLinkCreationFuture == null)
            {
                this.requestResponseLinkCreationFuture = new CompletableFuture<Void>();
                this.underlyingFactory.obtainRequestResponseLinkAsync(this.receivePath, this.entityType).handleAsync((rrlink, ex) ->
                {
                    if(ex == null)
                    {
                        this.requestResponseLink = rrlink;
                        this.requestResponseLinkCreationFuture.complete(null);
                    }
                    else
                    {
                        Throwable cause = ExceptionUtil.extractAsyncCompletionCause(ex);
                        this.requestResponseLinkCreationFuture.completeExceptionally(cause);
                        // Set it to null so next call will retry rr link creation
                        synchronized (this.requestResonseLinkCreationLock)
                        {
                            this.requestResponseLinkCreationFuture = null;
                        }
                    }
                    return null;
                });
            }
            
            return this.requestResponseLinkCreationFuture;
        }
	}
	
	private void closeRequestResponseLink()
	{
	    synchronized (this.requestResonseLinkCreationLock)
        {
            if(this.requestResponseLinkCreationFuture != null)
            {
                this.requestResponseLinkCreationFuture.thenRun(() -> {
                    this.underlyingFactory.releaseRequestResponseLink(this.receivePath);
                    this.requestResponseLink = null;
                });
                this.requestResponseLinkCreationFuture = null;
            }
        }
	}
	
	private void createReceiveLink()
	{
	    TRACE_LOGGER.info("Creating receive link to '{}'", this.receivePath);
		Connection connection = this.underlyingFactory.getConnection();	

		final Session session = connection.session();
		session.setIncomingCapacity(Integer.MAX_VALUE);
		session.open();
		BaseHandler.setHandler(session, new SessionHandler(this.receivePath));

		final String receiveLinkNamePrefix = "Receiver".concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(StringUtil.getShortRandomString());
		final String receiveLinkName = !StringUtil.isNullOrEmpty(connection.getRemoteContainer()) ? 
				receiveLinkNamePrefix.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(connection.getRemoteContainer()) :
				receiveLinkNamePrefix;
		final Receiver receiver = session.receiver(receiveLinkName);
		
		Source source = new Source();
		source.setAddress(receivePath);
		Map<Symbol, Object> linkProperties = new HashMap<>();
		// ServiceBus expects timeout to be of type unsignedint
		linkProperties.put(ClientConstants.LINK_TIMEOUT_PROPERTY, UnsignedInteger.valueOf(Util.adjustServerTimeout(this.underlyingFactory.getOperationTimeout()).toMillis()));
		if(this.entityType != null)
		{
			linkProperties.put(ClientConstants.ENTITY_TYPE_PROPERTY, this.entityType.getIntValue());
		}
		
		if(this.isSessionReceiver)
		{
			HashMap filterMap = new HashMap();
			filterMap.put(ClientConstants.SESSION_FILTER, this.sessionId);
			source.setFilter(filterMap);
			
			linkProperties.put(ClientConstants.LINK_PEEKMODE_PROPERTY, this.isBrowsableSession);
		}
		
		receiver.setSource(source);
		receiver.setTarget(new Target());

		// Set settle modes
		TRACE_LOGGER.debug("Receive link settle mode '{}'", this.settleModePair);
		receiver.setSenderSettleMode(this.settleModePair.getSenderSettleMode());
		receiver.setReceiverSettleMode(this.settleModePair.getReceiverSettleMode());
		
		receiver.setProperties(linkProperties);

		final ReceiveLinkHandler handler = new ReceiveLinkHandler(this);
		BaseHandler.setHandler(receiver, handler);
		receiver.open();
		this.receiveLink = receiver;
	}
	
	CompletableFuture<Void> sendTokenAndSetRenewTimer(boolean retryOnFailure)
    {
        if(this.getIsClosingOrClosed())
        {
            return CompletableFuture.completedFuture(null);
        }
        else
        {            
            CompletableFuture<ScheduledFuture<?>> sendTokenFuture = this.underlyingFactory.sendSecurityTokenAndSetRenewTimer(this.sasTokenAudienceURI, retryOnFailure, () -> this.sendTokenAndSetRenewTimer(true));
            return sendTokenFuture.thenAccept((f) -> {this.sasTokenRenewTimerFuture = f;TRACE_LOGGER.debug("Sent SAS Token and set renew timer");});
        }
    }
	
	private void throwIfInUnusableState()
	{
	    if(this.isSessionReceiver && this.isSessionLockLost)
	    {
	        throw new IllegalStateException("Session lock lost and cannot be used. Close this session and accept another session.");
	    }
	    
	    this.throwIfClosed(this.lastKnownLinkError);
	}
    
    private void cancelSASTokenRenewTimer()
    {
        if(this.sasTokenRenewTimerFuture != null && !this.sasTokenRenewTimerFuture.isDone())
        {
            this.sasTokenRenewTimerFuture.cancel(true);
            TRACE_LOGGER.debug("Cancelled SAS Token renew timer");
        }
    }

	private List<MessageWithDeliveryTag> receiveCore(int messageCount)
	{
		List<MessageWithDeliveryTag> returnMessages = null;
		MessageWithDeliveryTag currentMessage = this.prefetchedMessages.poll();
		int returnedMessageCount = 0;
		while (currentMessage != null) 
		{
		    this.currentPrefetechedMessagesCount.decrementAndGet();
			if (returnMessages == null)
			{
				returnMessages = new LinkedList<MessageWithDeliveryTag>();
			}

			returnMessages.add(currentMessage);
			if (++returnedMessageCount >= messageCount)
			{
				break;
			}

			currentMessage = this.prefetchedMessages.poll();
		}
		
		return returnMessages;
	}

	public int getPrefetchCount()
	{
		synchronized (this.prefetchCountSync)
		{
			return this.prefetchCount;
		}
	}

	public String getSessionId()
	{
		return this.sessionId;
	}
	

	public Instant getSessionLockedUntilUtc()
	{
		if(this.isSessionReceiver)
		{
			return this.sessionLockedUntilUtc;
		}
		else
		{
			throw new RuntimeException("Object is not a session receiver");
		}		
	}

	public void setPrefetchCount(final int value) throws ServiceBusException
	{
	    if(value < 0)
	    {
	        throw new IllegalArgumentException("Prefetch count cannot be negative.");
	    }
	    this.throwIfInUnusableState();
		final int deltaPrefetchCount;
		synchronized (this.prefetchCountSync)
		{
			deltaPrefetchCount = value - this.prefetchCount;
			this.prefetchCount = value;
			TRACE_LOGGER.info("Setting prefetch count to '{}' on recieve link to '{}'", value, this.receivePath);
		}
		
		if(deltaPrefetchCount > 0)
		{
		    try
	        {
	            this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
	            {
	                @Override
	                public void onEvent()
	                {
	                    sendFlow(deltaPrefetchCount);
	                }
	            });
	        }
	        catch (IOException ioException)
	        {
	            throw new ServiceBusException(false, "Setting prefetch count failed, see cause for more details", ioException);
	        }
		}
	}

	public CompletableFuture<Collection<MessageWithDeliveryTag>> receiveAsync(final int maxMessageCount, Duration timeout)
	{
	    this.throwIfInUnusableState();
		
		if (maxMessageCount <= 0)
		{
			throw new IllegalArgumentException("parameter 'maxMessageCount' should be a positive number");
		}
		
		TRACE_LOGGER.debug("Receiving maximum of '{}' messages from '{}'", maxMessageCount, this.receivePath);
		CompletableFuture<Collection<MessageWithDeliveryTag>> onReceive = new CompletableFuture<Collection<MessageWithDeliveryTag>>();
		final ReceiveWorkItem receiveWorkItem = new ReceiveWorkItem(onReceive, timeout, maxMessageCount);
		this.creditNeededtoServePendingReceives.addAndGet(maxMessageCount);
		this.pendingReceives.add(receiveWorkItem);
		// ZERO timeout is special case in SBMP clients where the timeout is sent to the service along with request. It meant 'give me messages you already have, but don't wait'.
		// As we don't send timeout to service in AMQP, treating this as a special case and using a very short timeout
		if(timeout == Duration.ZERO)
        {
            timeout = ZERO_TIMEOUT_APPROXIMATION;
        }
		
        Timer.schedule(
                new Runnable()
                {
                    public void run()
                    {
                        if( CoreMessageReceiver.this.pendingReceives.remove(receiveWorkItem))
                        {
                            CoreMessageReceiver.this.reduceCreditForCompletedReceiveRequest(receiveWorkItem.getMaxMessageCount());
                            TRACE_LOGGER.warn("No messages received from '{}'. Pending receive request timed out. Returning null to the client.", CoreMessageReceiver.this.receivePath);
                            receiveWorkItem.getWork().complete(null);
                        }
                    }
                },
                timeout,
                TimerType.OneTimeRun);
        
        this.ensureLinkIsOpen().thenRunAsync(() -> {this.addCredit(receiveWorkItem);});
		return onReceive;
	}

	@Override
	public void onOpenComplete(Exception exception)
	{
		if (exception == null)
		{
		    TRACE_LOGGER.info("Receive link to '{}' opened.", this.receivePath);
			if(this.isSessionReceiver)
			{
				Map remoteSourceFilter = ((Source)this.receiveLink.getRemoteSource()).getFilter();
				if(remoteSourceFilter != null && remoteSourceFilter.containsKey(ClientConstants.SESSION_FILTER))
				{
					String remoteSessionId = (String)remoteSourceFilter.get(ClientConstants.SESSION_FILTER);
					this.sessionId = remoteSessionId;
					
					if(this.receiveLink.getRemoteProperties() != null && this.receiveLink.getRemoteProperties().containsKey(ClientConstants.LOCKED_UNTIL_UTC))
					{
						this.sessionLockedUntilUtc = Util.convertDotNetTicksToInstant((long)this.receiveLink.getRemoteProperties().get(ClientConstants.LOCKED_UNTIL_UTC));
					}
					else
					{
					    TRACE_LOGGER.warn("Accepted a session with id '{}', from '{}' which didn't set '{}' property on the receive link.", this.sessionId, this.receivePath, ClientConstants.LOCKED_UNTIL_UTC);
						this.sessionLockedUntilUtc = Instant.ofEpochMilli(0);
					}
					
					TRACE_LOGGER.info("Accepted session with id '{}', lockedUntilUtc '{}' from '{}'.", this.sessionId, this.sessionLockedUntilUtc, this.receivePath);
				}
				else
				{
					exception = new ServiceBusException(false, "SessionId filter not set on the remote source.");
				}
			}
		}
		
		if (exception == null)
		{
		    this.underlyingFactory.registerForConnectionError(this.receiveLink);
		    
			if (this.linkOpen != null && !this.linkOpen.getWork().isDone())
			{
				AsyncUtil.completeFuture(this.linkOpen.getWork(), this);
			}
			
			if(this.receiveLinkReopenFuture != null && !this.receiveLinkReopenFuture.isDone())
			{
			    AsyncUtil.completeFuture(this.receiveLinkReopenFuture, null);
			}

			this.lastKnownLinkError = null;
			
			this.underlyingFactory.getRetryPolicy().resetRetryCount(this.underlyingFactory.getClientId());

			this.sendFlow(this.prefetchCount - this.currentPrefetechedMessagesCount.get());
			
			TRACE_LOGGER.debug("receiverPath:{}, linkname:{}, updated-link-credit:{}, sentCredits:{}",
                    this.receivePath, this.receiveLink.getName(), this.receiveLink.getCredit(), this.prefetchCount);
		}
		else
		{
            this.cancelSASTokenRenewTimer();
            
			if (this.linkOpen != null && !this.linkOpen.getWork().isDone())
			{
			    TRACE_LOGGER.error("Opening receive link to '{}' failed.", this.receivePath, exception);
				this.setClosed();
				ExceptionUtil.completeExceptionally(this.linkOpen.getWork(), exception, this, true);
			}
			
			if(this.receiveLinkReopenFuture != null && !this.receiveLinkReopenFuture.isDone())
            {
			    TRACE_LOGGER.warn("Opening receive link to '{}' failed.", this.receivePath, exception);
			    AsyncUtil.completeFutureExceptionally(this.receiveLinkReopenFuture, exception);
			    if(this.isSessionReceiver && (exception instanceof SessionLockLostException || exception instanceof SessionCannotBeLockedException))
                {
                    // No point in retrying to establish a link.. SessionLock is lost
                    TRACE_LOGGER.warn("SessionId '{}' lock lost. Closing receiver.", this.sessionId);
                    this.isSessionLockLost = true;
                    this.closeAsync();
                }
            }
			
			this.lastKnownLinkError = exception;
		}
	}

	@Override
	public void onReceiveComplete(Delivery delivery)
	{
	    this.underlyingFactory.getRetryPolicy().resetRetryCount(this.getClientId());
		byte[] deliveryTag = delivery.getTag();
		String deliveryTagAsString  = StringUtil.convertBytesToString(delivery.getTag());
		TRACE_LOGGER.debug("Received a delivery '{}' from '{}'", deliveryTagAsString, this.receivePath);
		if(deliveryTag == null || deliveryTag.length == 0 || !this.tagsToDeliveriesMap.containsKey(deliveryTagAsString))
		{
		    TRACE_LOGGER.debug("Received a message from '{}'. Adding to prefecthed messages.", this.receivePath);
		    try
		    {
		        Message message = Util.readMessageFromDelivery(receiveLink, delivery);
	            
	            if(this.settleModePair.getSenderSettleMode() == SenderSettleMode.SETTLED)
	            {
	                // No op. Delivery comes settled from the sender
	                delivery.disposition(Accepted.getInstance());
	                delivery.settle();
	            }
	            else
	            {
	                this.tagsToDeliveriesMap.put(StringUtil.convertBytesToString(delivery.getTag()), delivery);
	                receiveLink.advance();
	            }
	            
	            // Accuracy of count is not that important. So not making those two operations atomic
                this.currentPrefetechedMessagesCount.incrementAndGet();
	            this.prefetchedMessages.add(new MessageWithDeliveryTag(message, delivery.getTag()));
		    }
		    catch(Exception e)
		    {
		        TRACE_LOGGER.warn("Reading message from delivery '{}' from '{}', session '{}' failed with unexpected exception.", deliveryTagAsString, this.receivePath, this.sessionId, e);
		        delivery.disposition(Released.getInstance());
                delivery.settle();
                return;
		    }
		}
		else
		{
			DeliveryState remoteState = delivery.getRemoteState();
			TRACE_LOGGER.debug("Received a delivery '{}' with state '{}' from '{}'", deliveryTagAsString, remoteState, this.receivePath);

			Outcome remoteOutcome = null;
			if (remoteState instanceof Outcome) {
				remoteOutcome = (Outcome)remoteState;
			} else if (remoteState instanceof TransactionalState) {
				remoteOutcome = ((TransactionalState)remoteState).getOutcome();
			}

			if(remoteOutcome != null)
			{
				UpdateStateWorkItem matchingUpdateStateWorkItem = this.pendingUpdateStateRequests.get(deliveryTagAsString);
				if(matchingUpdateStateWorkItem != null)
				{
					DeliveryState matchingUpdateWorkItemDeliveryState = matchingUpdateStateWorkItem.getDeliveryState();
					if (matchingUpdateWorkItemDeliveryState instanceof TransactionalState) {
						matchingUpdateWorkItemDeliveryState = (DeliveryState)((TransactionalState) matchingUpdateWorkItemDeliveryState).getOutcome();
					}

					// This comparison is ugly. Using it for the lack of equals operation on Outcome classes
					if(remoteOutcome.getClass().getName().equals(matchingUpdateWorkItemDeliveryState.getClass().getName()))
					{
					    TRACE_LOGGER.debug("Completing a pending updateState operation for delivery '{}' from '{}'", deliveryTagAsString, this.receivePath);
						this.completePendingUpdateStateWorkItem(delivery, deliveryTagAsString, matchingUpdateStateWorkItem, null);						
					}
					else
					{
//						if(matchingUpdateStateWorkItem.expectedOutcome instanceof Accepted)
//						{
					        TRACE_LOGGER.warn("Received delivery '{}' state '{}' doesn't match expected state '{}'", deliveryTagAsString, remoteState, matchingUpdateStateWorkItem.deliveryState);
							// Complete requests
							if(remoteOutcome instanceof Rejected)
							{
								Rejected rejected = (Rejected) remoteOutcome;
								ErrorCondition error = rejected.getError();
								Exception exception = ExceptionUtil.toException(error);

								if (ExceptionUtil.isGeneralError(error.getCondition()))
								{
									this.lastKnownLinkError = exception;
									this.lastKnownErrorReportedAt = Instant.now();
								}

								Duration retryInterval = this.retryPolicy.getNextRetryInterval(this.getClientId(), exception, matchingUpdateStateWorkItem.getTimeoutTracker().remaining());
								if (retryInterval == null)
								{
								    TRACE_LOGGER.error("Completing pending updateState operation for delivery '{}' with exception", deliveryTagAsString, exception);
									this.completePendingUpdateStateWorkItem(delivery, deliveryTagAsString, matchingUpdateStateWorkItem, exception);
								}
								else
								{
									matchingUpdateStateWorkItem.setLastKnownException(exception);
									// Retry after retry interval
									TRACE_LOGGER.debug("Pending updateState operation for delivery '{}' will be retried after '{}'", deliveryTagAsString, retryInterval);
									try
									{
										this.underlyingFactory.scheduleOnReactorThread((int) retryInterval.toMillis(),
												new DispatchHandler()
												{
													@Override
													public void onEvent()
													{
														delivery.disposition(matchingUpdateStateWorkItem.getDeliveryState());
													}
												});
									}
									catch (IOException ioException)
									{
										this.completePendingUpdateStateWorkItem(delivery, deliveryTagAsString, matchingUpdateStateWorkItem,
												new ServiceBusException(false, "Operation failed while scheduling a retry on Reactor, see cause for more details.", ioException));
									}
								}
							}
							else if (remoteOutcome instanceof Released)
							{
							    Exception exception = new OperationCancelledException(remoteOutcome.toString());
							    TRACE_LOGGER.error("Completing pending updateState operation for delivery '{}' with exception", deliveryTagAsString, exception);
								this.completePendingUpdateStateWorkItem(delivery, deliveryTagAsString, matchingUpdateStateWorkItem, exception);
							}
							else 
							{
							    Exception exception = new ServiceBusException(false, remoteOutcome.toString());
							    TRACE_LOGGER.error("Completing pending updateState operation for delivery '{}' with exception", deliveryTagAsString, exception);
								this.completePendingUpdateStateWorkItem(delivery, deliveryTagAsString, matchingUpdateStateWorkItem, exception);
							}
//						}
					}
				}
				else
				{
					// Should not happen. Ignore it
				}				
			}
			else
			{
				//Ignore it. we are only interested in terminal delivery states
			}
		}
	}

	public void onError(ErrorCondition error)
	{		
		Exception completionException = ExceptionUtil.toException(error);
		this.onError(completionException);
	}

	@Override
	public void onError(Exception exception)
	{
	    this.creditToFlow.set(0);
	    this.cancelSASTokenRenewTimer();
	    if(this.settleModePair.getSenderSettleMode() == SenderSettleMode.UNSETTLED)
	    {
	        this.prefetchedMessages.clear();
	        this.currentPrefetechedMessagesCount.set(0);
	        this.tagsToDeliveriesMap.clear();
	    }

		if (this.getIsClosingOrClosed())
		{
		    TRACE_LOGGER.info("Receive link to '{}', sessionId '{}' closed", this.receivePath, this.sessionId);
			AsyncUtil.completeFuture(this.linkClose, null);
			this.clearAllPendingWorkItems(exception);
		}
		else
		{
		    TRACE_LOGGER.warn("Receive link to '{}', sessionId '{}' closed with error.", this.receivePath, this.sessionId, exception);
			this.lastKnownLinkError = exception;
			if ((this.linkOpen != null && !this.linkOpen.getWork().isDone()) ||
			     (this.receiveLinkReopenFuture !=null && !receiveLinkReopenFuture.isDone()))
			{
			    this.onOpenComplete(exception);
			}
			else
			{
			    this.underlyingFactory.deregisterForConnectionError(this.receiveLink);
			    
			    if (exception != null &&
	                    (!(exception instanceof ServiceBusException) || !((ServiceBusException) exception).getIsTransient()))
	            {
	                this.clearAllPendingWorkItems(exception);
	                
	                if(this.isSessionReceiver && (exception instanceof SessionLockLostException || exception instanceof SessionCannotBeLockedException))
	                {
	                    // No point in retrying to establish a link.. SessionLock is lost
	                    TRACE_LOGGER.warn("SessionId '{}' lock lost. Closing receiver.", this.sessionId);
	                    this.isSessionLockLost = true;
	                    this.closeAsync();
	                }
	            }
	            else
	            {
	                // TODO Why recreating link needs to wait for retry interval of pending receive?
	                ReceiveWorkItem workItem = this.pendingReceives.peek();
	                if (workItem != null && workItem.getTimeoutTracker() != null)
	                {
	                    Duration nextRetryInterval = this.underlyingFactory.getRetryPolicy()
	                            .getNextRetryInterval(this.getClientId(), exception, workItem.getTimeoutTracker().remaining());
	                    if (nextRetryInterval != null)
	                    {
	                        TRACE_LOGGER.info("Receive link to '{}', sessionId '{}' will be reopened after '{}'", this.receivePath, this.sessionId, nextRetryInterval);
	                        Timer.schedule(() -> {CoreMessageReceiver.this.ensureLinkIsOpen();}, nextRetryInterval, TimerType.OneTimeRun);
	                    }
	                }
	            }
			}
		}
	}
	
	private void reduceCreditForCompletedReceiveRequest(int maxCreditCountOfReceiveRequest)
    {
        this.creditNeededtoServePendingReceives.updateAndGet((c) -> {
            int updatedCredit = c - maxCreditCountOfReceiveRequest;
            return (updatedCredit > 0) ? updatedCredit : 0;
        });
    }
	
	private void addCredit(ReceiveWorkItem receiveWorkItem)
	{
	    // Timed out receive requests and batch receive requests completed with less than maxCount messages might have sent more credit
	    // than consumed by the receiver resulting in excess credit at the service endpoint.
	    int creditToFlowForWorkItem = this.creditNeededtoServePendingReceives.get() - (this.receiveLink.getCredit() + this.currentPrefetechedMessagesCount.get() + this.creditToFlow.get()) + this.prefetchCount;
	    if(creditToFlowForWorkItem > 0)
	    {
	        int currentTotalCreditToSend = this.creditToFlow.addAndGet(creditToFlowForWorkItem);
	        if(currentTotalCreditToSend >= this.prefetchCount || currentTotalCreditToSend >= CREDIT_FLOW_BATCH_SIZE)
	        {
	            try
	            {
	                this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
	                {
	                    @Override
	                    public void onEvent()
	                    {
	                        // Send credit accumulated so far to make it less chat-ty
	                        int accumulatedCredit = CoreMessageReceiver.this.creditToFlow.getAndSet(0);
	                        sendFlow(accumulatedCredit);
	                    }
	                });
	            }
	            catch (IOException ioException)
	            {
	                this.pendingReceives.remove(receiveWorkItem);
	                this.reduceCreditForCompletedReceiveRequest(receiveWorkItem.getMaxMessageCount());
	                AsyncUtil.completeFutureExceptionally(receiveWorkItem.getWork(), generateDispatacherSchedulingFailedException("completeMessage", ioException));
	                receiveWorkItem.cancelTimeoutTask(false);
	            }
	        }
	    }
	}
	
	private void sendFlow(int credits)
	{
	    if(!this.isBrowsableSession && credits > 0)
	    {
	        this.receiveLink.flow(credits);
	        TRACE_LOGGER.debug("Sent flow to the service. receiverPath:{}, linkname:{}, updated-link-credit:{}, sentCredits:{}",
	                this.receivePath, this.receiveLink.getName(), this.receiveLink.getCredit(), credits);
	    }	    
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
						    CoreMessageReceiver.this.closeInternals(false);
						    CoreMessageReceiver.this.setClosed();
						    
							Exception operationTimedout = new TimeoutException(
									String.format(Locale.US, "%s operation on ReceiveLink(%s) to path(%s) timed out at %s.", "Open", CoreMessageReceiver.this.receiveLink.getName(), CoreMessageReceiver.this.receivePath, ZonedDateTime.now()),
									CoreMessageReceiver.this.lastKnownLinkError);
							TRACE_LOGGER.warn(operationTimedout.getMessage());
							ExceptionUtil.completeExceptionally(linkOpen.getWork(), operationTimedout, CoreMessageReceiver.this, false);
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
						if (!linkClose.isDone())
						{
							Exception operationTimedout = new TimeoutException(String.format(Locale.US, "%s operation on Receive Link(%s) timed out at %s", "Close", CoreMessageReceiver.this.receiveLink.getName(), ZonedDateTime.now()));
							TRACE_LOGGER.warn(operationTimedout.getMessage());

							ExceptionUtil.completeExceptionally(linkClose, operationTimedout, CoreMessageReceiver.this, false);
						}
					}
				}
				, timeout.remaining()
				, TimerType.OneTimeRun);
	}

	@Override
	public void onClose(ErrorCondition condition)
	{
		if (condition == null)
		{
			this.onError(new ServiceBusException(true, 
					String.format(Locale.US, "Closing the link. LinkName(%s), EntityPath(%s)", this.receiveLink.getName(), this.receivePath)));
		}
		else
		{
			this.onError(condition);
		}
	}

	@Override
	public ErrorContext getContext()
	{
		final boolean isLinkOpened = this.linkOpen != null && this.linkOpen.getWork().isDone();
		final String referenceId = this.receiveLink != null && this.receiveLink.getRemoteProperties() != null && this.receiveLink.getRemoteProperties().containsKey(ClientConstants.TRACKING_ID_PROPERTY)
				? this.receiveLink.getRemoteProperties().get(ClientConstants.TRACKING_ID_PROPERTY).toString()
						: ((this.receiveLink != null) ? this.receiveLink.getName(): null);

		ReceiverErrorContext errorContext = new ReceiverErrorContext(this.underlyingFactory != null ? this.underlyingFactory.getHostName() : null,
				this.receivePath,
				referenceId,				 
						isLinkOpened ? this.prefetchCount : null, 
								isLinkOpened && this.receiveLink != null ? this.receiveLink.getCredit(): null, 
										this.currentPrefetechedMessagesCount.get());

		return errorContext;
	}	

	@Override
	protected CompletableFuture<Void> onClose()
	{
		this.closeInternals(true);
		return this.linkClose;
	}
	
	private void closeInternals(boolean waitForCloseCompletion)
	{
	    if (!this.getIsClosed())
        {           
            if (this.receiveLink != null && this.receiveLink.getLocalState() != EndpointState.CLOSED)
            {
                try {
                    this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler() {
                        
                        @Override
                        public void onEvent() {
                            if (CoreMessageReceiver.this.receiveLink != null && CoreMessageReceiver.this.receiveLink.getLocalState() != EndpointState.CLOSED)
                            {
                                TRACE_LOGGER.info("Closing receive link to '{}'", CoreMessageReceiver.this.receivePath);
                                CoreMessageReceiver.this.receiveLink.close();
                                CoreMessageReceiver.this.underlyingFactory.deregisterForConnectionError(CoreMessageReceiver.this.receiveLink);
                                if(waitForCloseCompletion)
                                {
                                    CoreMessageReceiver.this.scheduleLinkCloseTimeout(TimeoutTracker.create(CoreMessageReceiver.this.operationTimeout));
                                }
                                else
                                {
                                    AsyncUtil.completeFuture(CoreMessageReceiver.this.linkClose, null);
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    AsyncUtil.completeFutureExceptionally(this.linkClose, e);
                }
            }
            else
            {
                AsyncUtil.completeFuture(this.linkClose, null);
            }
            
            this.cancelSASTokenRenewTimer();
            this.closeRequestResponseLink();
            this.updateStateRequestsTimeoutChecker.cancel(false);
            this.returnMessagesLoopRunner.cancel(false);
        }
	}

	/*
	This is to be used for messages which are received on receiveLink.
	 */
	public CompletableFuture<Void> completeMessageAsync(byte[] deliveryTag, TransactionContext transaction)
	{		
		Outcome outcome = Accepted.getInstance();
		return this.updateMessageStateAsync(deliveryTag, outcome, transaction);
	}

	/*
	This is to be used for messages which are received on RequestResponseLink
	 */
	public CompletableFuture<Void> completeMessageAsync(UUID lockToken, TransactionContext transaction)
	{		
		return this.updateDispositionAsync(
				new UUID[]{lockToken},
				ClientConstants.DISPOSITION_STATUS_COMPLETED,
				null,
				null,
				null,
				transaction);
	}
	
	public CompletableFuture<Void> abandonMessageAsync(byte[] deliveryTag, Map<String, Object> propertiesToModify, TransactionContext transaction)
	{		
		Modified outcome = new Modified();
		if(propertiesToModify != null)
		{
			outcome.setMessageAnnotations(propertiesToModify);
		}		
		return this.updateMessageStateAsync(deliveryTag, outcome, transaction);
	}
	
	public CompletableFuture<Void> abandonMessageAsync(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction)
	{
		return this.updateDispositionAsync(
				new UUID[]{lockToken},
				ClientConstants.DISPOSITION_STATUS_ABANDONED,
				null
				, null,
				propertiesToModify,
				transaction);
	}
	
	public CompletableFuture<Void> deferMessageAsync(byte[] deliveryTag, Map<String, Object> propertiesToModify, TransactionContext transaction)
	{		
		Modified outcome = new Modified();
		outcome.setUndeliverableHere(true);
		if(propertiesToModify != null)
		{
			outcome.setMessageAnnotations(propertiesToModify);
		}
		return this.updateMessageStateAsync(deliveryTag, outcome, transaction);
	}
	
	public CompletableFuture<Void> deferMessageAsync(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction)
	{		
		return this.updateDispositionAsync(
				new UUID[]{lockToken},
				ClientConstants.DISPOSITION_STATUS_DEFERED,
				null,
				null,
				propertiesToModify,
				transaction);
	}
	
	public CompletableFuture<Void> deadLetterMessageAsync(
			byte[] deliveryTag,
			String deadLetterReason,
			String deadLetterErrorDescription,
			Map<String, Object> propertiesToModify,
			TransactionContext transaction)
	{
		Rejected outcome = new Rejected();
		ErrorCondition error = new ErrorCondition(ClientConstants.DEADLETTERNAME, null);
		Map<String, Object> errorInfo = new HashMap<String, Object>();
		if(!StringUtil.isNullOrEmpty(deadLetterReason))
		{
			errorInfo.put(ClientConstants.DEADLETTER_REASON_HEADER, deadLetterReason);
		}
		if(!StringUtil.isNullOrEmpty(deadLetterErrorDescription))
		{
			errorInfo.put(ClientConstants.DEADLETTER_ERROR_DESCRIPTION_HEADER, deadLetterErrorDescription);
		}
		if(propertiesToModify != null)
		{
			errorInfo.putAll(propertiesToModify);
		}
		error.setInfo(errorInfo);
		outcome.setError(error);
		
		return this.updateMessageStateAsync(deliveryTag, outcome, transaction);
	}
	
	public CompletableFuture<Void> deadLetterMessageAsync(
			UUID lockToken,
			String deadLetterReason,
			String deadLetterErrorDescription,
			Map<String, Object> propertiesToModify,
			TransactionContext transaction)
	{
		return this.updateDispositionAsync(
				new UUID[]{lockToken},
				ClientConstants.DISPOSITION_STATUS_SUSPENDED,
				deadLetterReason,
				deadLetterErrorDescription,
				propertiesToModify,
				transaction);
	}
	
	private CompletableFuture<Void> updateMessageStateAsync(byte[] deliveryTag, Outcome outcome, TransactionContext transaction)
	{
	    this.throwIfInUnusableState();
		CompletableFuture<Void> completeMessageFuture = new CompletableFuture<Void>();
		
		String deliveryTagAsString = StringUtil.convertBytesToString(deliveryTag);
        TRACE_LOGGER.debug("Updating message state of delivery '{}' to '{}'", deliveryTagAsString, outcome);
        Delivery delivery = CoreMessageReceiver.this.tagsToDeliveriesMap.get(deliveryTagAsString);
        if(delivery == null)
        {
            TRACE_LOGGER.error("Delivery not found for delivery tag '{}'. Either receive link to '{}' closed with a transient error and reopened or the delivery was already settled by complete/abandon/defer/deadletter.", deliveryTagAsString, this.receivePath);
            AsyncUtil.completeFutureExceptionally(completeMessageFuture, generateDeliveryNotFoundException());
        }
        else
        {
        	DeliveryState state;
        	if (transaction != TransactionContext.NULL_TXN) {
        		state = new TransactionalState();
				((TransactionalState)state).setTxnId(new Binary(transaction.getTransactionId().array()));
				((TransactionalState)state).setOutcome(outcome);
			} else {
        		state = (DeliveryState) outcome;
			}

            final UpdateStateWorkItem workItem = new UpdateStateWorkItem(completeMessageFuture, state, CoreMessageReceiver.this.factoryRceiveTimeout);
            CoreMessageReceiver.this.pendingUpdateStateRequests.put(deliveryTagAsString, workItem);
            
            CoreMessageReceiver.this.ensureLinkIsOpen().thenRunAsync(() -> {
                try
                {
                    this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
                    {
                        @Override
                        public void onEvent()
                        {
                        	delivery.disposition(state);
                        }
                    });
                }
                catch (IOException ioException)
                {
                    completeMessageFuture.completeExceptionally(generateDispatacherSchedulingFailedException("completeMessage", ioException));
                }
            });
        }		

		return completeMessageFuture;
	}
	
	private synchronized CompletableFuture<Void> ensureLinkIsOpen()
	{
	    // Send SAS token before opening a link as connection might have been closed and reopened
		if (!(this.receiveLink.getLocalState() == EndpointState.ACTIVE && this.receiveLink.getRemoteState() == EndpointState.ACTIVE))
		{
		    if(this.receiveLinkReopenFuture == null || this.receiveLinkReopenFuture.isDone())
		    {
		        TRACE_LOGGER.info("Recreating receive link to '{}'", this.receivePath);
	            this.retryPolicy.incrementRetryCount(this.getClientId());
	            this.receiveLinkReopenFuture = new CompletableFuture<Void>();
	            // Variable just to be closed over by the scheduled runnable. The runnable should cancel only the closed over future, not the parent's instance variable which can change
	            final CompletableFuture<Void> linkReopenFutureThatCanBeCancelled = this.receiveLinkReopenFuture;
	            Timer.schedule(
	                    () -> {
	                        if (!linkReopenFutureThatCanBeCancelled.isDone())
                            {
                                CoreMessageReceiver.this.cancelSASTokenRenewTimer();
                                Exception operationTimedout = new TimeoutException(
                                        String.format(Locale.US, "%s operation on ReceiveLink(%s) to path(%s) timed out at %s.", "Open", CoreMessageReceiver.this.receiveLink.getName(), CoreMessageReceiver.this.receivePath, ZonedDateTime.now()));                           
                                
                                TRACE_LOGGER.warn(operationTimedout.getMessage());
                                linkReopenFutureThatCanBeCancelled.completeExceptionally(operationTimedout);
                            }
	                    }
	                    , CoreMessageReceiver.LINK_REOPEN_TIMEOUT
	                    , TimerType.OneTimeRun);
	            this.cancelSASTokenRenewTimer();
	            this.sendTokenAndSetRenewTimer(false).handleAsync((v, sendTokenEx) -> {
	                if(sendTokenEx != null)
	                {
	                	Throwable cause = ExceptionUtil.extractAsyncCompletionCause(sendTokenEx);
        				TRACE_LOGGER.error("Sending SAS Token to '{}' failed.", this.receivePath, cause);
	                    this.receiveLinkReopenFuture.completeExceptionally(sendTokenEx);
	                }
	                else
	                {
	                    try
	                    {
	                        this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
	                        {
	                            @Override
	                            public void onEvent()
	                            {
	                                CoreMessageReceiver.this.createReceiveLink();
	                            }
	                        });
	                    }
	                    catch (IOException ioEx)
	                    {
	                        this.receiveLinkReopenFuture.completeExceptionally(ioEx);
	                    }
	                }
	                return null;
	            });
		    }
		    
		    return this.receiveLinkReopenFuture;
		}
		else
		{
		    return CompletableFuture.completedFuture(null);
		}
	}
	
	private void completePendingUpdateStateWorkItem(Delivery delivery, String deliveryTagAsString, UpdateStateWorkItem workItem, Exception exception)
	{
		boolean isSettled = delivery.remotelySettled();
		if (isSettled) {
			delivery.settle();
		}

		if(exception == null)
		{  
			AsyncUtil.completeFuture(workItem.getWork(), null);
		}
		else
		{
			ExceptionUtil.completeExceptionally(workItem.getWork(), exception, this, true);
		}

		if (isSettled) {
			this.tagsToDeliveriesMap.remove(deliveryTagAsString);
			this.pendingUpdateStateRequests.remove(deliveryTagAsString);
		}
	}
	
	private void clearAllPendingWorkItems(Exception exception)
	{
	    TRACE_LOGGER.info("Completeing all pending receive and updateState operation on the receiver to '{}'", this.receivePath);
		final boolean isTransientException = exception == null ||
				(exception instanceof ServiceBusException && ((ServiceBusException) exception).getIsTransient());
		
		Iterator<ReceiveWorkItem> pendingRecivesIterator = this.pendingReceives.iterator();
		while(pendingRecivesIterator.hasNext())
		{
			ReceiveWorkItem workItem = pendingRecivesIterator.next();
			pendingRecivesIterator.remove();
			
			CompletableFuture<Collection<MessageWithDeliveryTag>> future = workItem.getWork();
			workItem.cancelTimeoutTask(false);
			this.reduceCreditForCompletedReceiveRequest(workItem.getMaxMessageCount());
			if (isTransientException)
			{				
				AsyncUtil.completeFuture(future, null);
			}
			else
			{
				ExceptionUtil.completeExceptionally(future, exception, this, true);
			}
		}
		
		for(Map.Entry<String, UpdateStateWorkItem> pendingUpdate : this.pendingUpdateStateRequests.entrySet())
		{
			pendingUpdateStateRequests.remove(pendingUpdate.getKey());			
			ExceptionUtil.completeExceptionally(pendingUpdate.getValue().getWork(), exception, this, true);
		}
	}
	
	private static IllegalArgumentException generateDeliveryNotFoundException()
	{
		return new IllegalArgumentException("Delivery not found on the receive link.");
	}
	
	private static ServiceBusException generateDispatacherSchedulingFailedException(String operation, Exception cause)
	{
		return new ServiceBusException(false, operation + " failed while dispatching to Reactor, see cause for more details.", cause);
	}
	
	public CompletableFuture<Collection<Instant>> renewMessageLocksAsync(UUID[] lockTokens)
	{
	    this.throwIfInUnusableState();
	    if(TRACE_LOGGER.isDebugEnabled())
	    {
	        TRACE_LOGGER.debug("Renewing message locks for lock tokens '{}' of entity '{}', sesion '{}'", Arrays.toString(lockTokens), this.receivePath, this.isSessionReceiver ? this.getSessionId() : "");
	    }
		return this.createRequestResponseLinkAsync().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_LOCKTOKENS, lockTokens);
			if(this.isSessionReceiver)
			{
				requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SESSIONID, this.getSessionId());
			}
			
			Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_RENEWLOCK_OPERATION, requestBodyMap, Util.adjustServerTimeout(this.operationTimeout), this.receiveLink.getName());
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, TransactionContext.NULL_TXN, this.operationTimeout);
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<Collection<Instant>> returningFuture = new CompletableFuture<Collection<Instant>>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
				    if(TRACE_LOGGER.isDebugEnabled())
				    {
				        TRACE_LOGGER.debug("Message locks for lock tokens '{}' renewed", Arrays.toString(lockTokens));
				    }
				    
					Date[] expirations = (Date[])RequestResponseUtils.getResponseBody(responseMessage).get(ClientConstants.REQUEST_RESPONSE_EXPIRATIONS);
					returningFuture.complete(Arrays.stream(expirations).map((d) -> d.toInstant()).collect(Collectors.toList()));
				}
				else
				{
					// error response
				    Exception failureException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
				    TRACE_LOGGER.error("Renewing message locks for lock tokens '{}' on entity '{}' failed", Arrays.toString(lockTokens), this.receivePath, failureException);
					returningFuture.completeExceptionally(failureException);
				}
				return returningFuture;
			});
		});					
	}
	
	public CompletableFuture<Collection<MessageWithLockToken>> receiveDeferredMessageBatchAsync(Long[] sequenceNumbers)
	{
	    this.throwIfInUnusableState();
	    if(TRACE_LOGGER.isDebugEnabled())
        {
            TRACE_LOGGER.debug("Receiving messages for sequence numbers '{}' from entity '{}', sesion '{}'", Arrays.toString(sequenceNumbers), this.receivePath, this.isSessionReceiver ? this.getSessionId() : "");
        }
		return this.createRequestResponseLinkAsync().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SEQUENCE_NUMBERS, sequenceNumbers);
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_RECEIVER_SETTLE_MODE, UnsignedInteger.valueOf(this.settleModePair.getReceiverSettleMode() == ReceiverSettleMode.FIRST ? 0 : 1));		
			if(this.isSessionReceiver)
			{
				requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SESSIONID, this.getSessionId());
			}
			
			Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_RECEIVE_BY_SEQUENCE_NUMBER, requestBodyMap, Util.adjustServerTimeout(this.operationTimeout), this.receiveLink.getName());
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, TransactionContext.NULL_TXN, this.operationTimeout);
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<Collection<MessageWithLockToken>> returningFuture = new CompletableFuture<Collection<MessageWithLockToken>>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
				    if(TRACE_LOGGER.isDebugEnabled())
			        {
			            TRACE_LOGGER.debug("Received messges for sequence numbers '{}' from entity '{}', sesion '{}'", Arrays.toString(sequenceNumbers), this.receivePath, this.isSessionReceiver ? this.getSessionId() : "");
			        }
					List<MessageWithLockToken> receivedMessages = new ArrayList<MessageWithLockToken>();
					Object responseBodyMap = ((AmqpValue)responseMessage.getBody()).getValue();
					if(responseBodyMap != null && responseBodyMap instanceof Map)
					{					
						Object messages = ((Map)responseBodyMap).get(ClientConstants.REQUEST_RESPONSE_MESSAGES);
						if(messages != null && messages instanceof Iterable)
						{
							for(Object message : (Iterable)messages)
							{
								if(message instanceof Map)
								{
									Message receivedMessage = Message.Factory.create();
									Binary messagePayLoad = (Binary)((Map)message).get(ClientConstants.REQUEST_RESPONSE_MESSAGE);
									receivedMessage.decode(messagePayLoad.getArray(), messagePayLoad.getArrayOffset(), messagePayLoad.getLength());
									UUID lockToken = ClientConstants.ZEROLOCKTOKEN;
									if(((Map)message).containsKey(ClientConstants.REQUEST_RESPONSE_LOCKTOKEN))
									{
										lockToken = (UUID)((Map)message).get(ClientConstants.REQUEST_RESPONSE_LOCKTOKEN);
									}
									
									receivedMessages.add(new MessageWithLockToken(receivedMessage, lockToken));
								}
							}
						}
					}				
					returningFuture.complete(receivedMessages);
				}
				else
				{
					// error response
				    Exception failureException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
                    TRACE_LOGGER.error("Receiving messages by sequence numbers '{}' from entity '{}' failed", Arrays.toString(sequenceNumbers), this.receivePath, failureException);
					returningFuture.completeExceptionally(failureException);
				}
				return returningFuture;
			});
		});		
	}
	
	public CompletableFuture<Void> updateDispositionAsync(
			UUID[] lockTokens,
			String dispositionStatus,
			String deadLetterReason,
			String deadLetterErrorDescription,
			Map<String, Object> propertiesToModify,
			TransactionContext transaction)
	{
	    this.throwIfInUnusableState();
	    if(TRACE_LOGGER.isDebugEnabled())
        {
            TRACE_LOGGER.debug("Update disposition of deliveries '{}' to '{}' on entity '{}', sesion '{}'", Arrays.toString(lockTokens), dispositionStatus, this.receivePath, this.isSessionReceiver ? this.getSessionId() : "");
        }
		return this.createRequestResponseLinkAsync().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_LOCKTOKENS, lockTokens);
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_DISPOSITION_STATUS, dispositionStatus);
			
			if(deadLetterReason != null)
			{
				requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_DEADLETTER_REASON, deadLetterReason);
			}
			
			if(deadLetterErrorDescription != null)
			{
				requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_DEADLETTER_DESCRIPTION, deadLetterErrorDescription);
			}
			
			if(propertiesToModify != null && propertiesToModify.size() > 0)
			{
				requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_PROPERTIES_TO_MODIFY, propertiesToModify);
			}
			
			if(this.isSessionReceiver)
			{
				requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SESSIONID, this.getSessionId());
			}
			
			Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_UPDATE_DISPOSTION_OPERATION, requestBodyMap, Util.adjustServerTimeout(this.operationTimeout), this.receiveLink.getName());
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, transaction, this.operationTimeout);
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<Void> returningFuture = new CompletableFuture<Void>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
				    if(TRACE_LOGGER.isDebugEnabled())
			        {
			            TRACE_LOGGER.debug("Update disposition of deliveries '{}' to '{}' on entity '{}', sesion '{}' succeeded.", Arrays.toString(lockTokens), dispositionStatus, this.receivePath, this.isSessionReceiver ? this.getSessionId() : "");
			        }
					returningFuture.complete(null);
				}
				else
				{
					// error response
				    Exception failureException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
                    TRACE_LOGGER.error("Update disposition on entity '{}' failed", this.receivePath, failureException);
					returningFuture.completeExceptionally(failureException);
				}
				return returningFuture;
			});
		});		
	}
	
	public CompletableFuture<Void> renewSessionLocksAsync()
	{
	    this.throwIfInUnusableState();
	    TRACE_LOGGER.debug("Renewing session lock on entity '{}' of sesion '{}'", this.receivePath, this.getSessionId());
		return this.createRequestResponseLinkAsync().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SESSIONID, this.getSessionId());
			
			Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_RENEW_SESSIONLOCK_OPERATION, requestBodyMap, Util.adjustServerTimeout(this.operationTimeout), this.receiveLink.getName());
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, TransactionContext.NULL_TXN, this.operationTimeout);
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<Void> returningFuture = new CompletableFuture<Void>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
					Date expiration = (Date)RequestResponseUtils.getResponseBody(responseMessage).get(ClientConstants.REQUEST_RESPONSE_EXPIRATION);
					this.sessionLockedUntilUtc = expiration.toInstant();
					TRACE_LOGGER.debug("Session lock on entity '{}' of sesion '{}' renewed until '{}'", this.receivePath, this.getSessionId(), this.sessionLockedUntilUtc);
					returningFuture.complete(null);
				}
				else
				{
					// error response
				    Exception failureException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
                    TRACE_LOGGER.error("Renewing session lock on entity '{}' of sesion '{}' failed", this.receivePath, this.getSessionId(), failureException);
					returningFuture.completeExceptionally(failureException);
				}
				return returningFuture;
			});
		});		
	}
	
	public CompletableFuture<byte[]> getSessionStateAsync()
	{
	    this.throwIfInUnusableState();
	    TRACE_LOGGER.debug("Getting session state of sesion '{}' from entity '{}'", this.getSessionId(), this.receivePath);
		return this.createRequestResponseLinkAsync().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SESSIONID, this.getSessionId());		
			
			Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_GET_SESSION_STATE_OPERATION, requestBodyMap, Util.adjustServerTimeout(this.operationTimeout), this.receiveLink.getName());
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, TransactionContext.NULL_TXN, this.operationTimeout);
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<byte[]> returningFuture = new CompletableFuture<byte[]>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
				    TRACE_LOGGER.debug("Got session state of sesion '{}' from entity '{}'", this.getSessionId(), this.receivePath);
					byte[] receivedState = null;
					Map bodyMap = RequestResponseUtils.getResponseBody(responseMessage);
					if(bodyMap.containsKey(ClientConstants.REQUEST_RESPONSE_SESSION_STATE))
					{
						Object sessionState = bodyMap.get(ClientConstants.REQUEST_RESPONSE_SESSION_STATE);
						if(sessionState != null)
						{
							receivedState = ((Binary)sessionState).getArray();
						}
					}
					
					returningFuture.complete(receivedState);
				}
				else
				{
					// error response
				    Exception failureException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
                    TRACE_LOGGER.error("Getting session state of sesion '{}' from entity '{}' failed", this.getSessionId(), this.receivePath, failureException);
					returningFuture.completeExceptionally(failureException);
				}
				return returningFuture;
			});
		});
	}
	
	// NULL session state is allowed
	public CompletableFuture<Void> setSessionStateAsync(byte[] sessionState)
	{
	    this.throwIfInUnusableState();
	    TRACE_LOGGER.debug("Setting session state of sesion '{}' on entity '{}'", this.getSessionId(), this.receivePath);
		return this.createRequestResponseLinkAsync().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SESSIONID, this.getSessionId());
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SESSION_STATE, sessionState == null ? null : new Binary(sessionState));
			
			Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_SET_SESSION_STATE_OPERATION, requestBodyMap, Util.adjustServerTimeout(this.operationTimeout), this.receiveLink.getName());
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, TransactionContext.NULL_TXN, this.operationTimeout);
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<Void> returningFuture = new CompletableFuture<Void>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
				    TRACE_LOGGER.debug("Setting session state of sesion '{}' on entity '{}' succeeded", this.getSessionId(), this.receivePath);
					returningFuture.complete(null);				
				}
				else
				{
					// error response
				    Exception failureException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
                    TRACE_LOGGER.error("Setting session state of sesion '{}' on entity '{}' failed", this.getSessionId(), this.receivePath, failureException);
					returningFuture.completeExceptionally(failureException);
				}
				return returningFuture;
			});
		});		
	}
	
	// A receiver can be used to peek messages from any session-id, useful for browsable sessions
	public CompletableFuture<Collection<Message>> peekMessagesAsync(long fromSequenceNumber, int messageCount, String sessionId)
	{
	    this.throwIfInUnusableState();
		return this.createRequestResponseLinkAsync().thenComposeAsync((v) -> {
			return CommonRequestResponseOperations.peekMessagesAsync(this.requestResponseLink, this.operationTimeout, fromSequenceNumber, messageCount, sessionId, this.receiveLink.getName());
		});
	}	
}
