/*
; * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import com.microsoft.azure.servicebus.amqp.DispatchHandler;
import com.microsoft.azure.servicebus.amqp.IAmqpSender;
import com.microsoft.azure.servicebus.amqp.SendLinkHandler;
import com.microsoft.azure.servicebus.amqp.SessionHandler;

/*
 * Abstracts all amqp related details
 * translates event-driven reactor model into async send Api
 */
public class CoreMessageSender extends ClientEntity implements IAmqpSender, IErrorContextProvider
{
	private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(CoreMessageSender.class);
	private static final String SEND_TIMED_OUT = "Send operation timed out";
	private static final Duration LINK_REOPEN_TIMEOUT = Duration.ofMinutes(5); // service closes link long before this timeout expires

	private final Object requestResonseLinkCreationLock = new Object();
	private final MessagingFactory underlyingFactory;
	private final String sendPath;
	private final String sasTokenAudienceURI;
	private final Duration operationTimeout;
	private final RetryPolicy retryPolicy;
	private final CompletableFuture<Void> linkClose;
	private final Object pendingSendLock;
	private final ConcurrentHashMap<String, SendWorkItem<Void>> pendingSendsData;
	private final PriorityQueue<WeightedDeliveryTag> pendingSends;
	private final DispatchHandler sendWork;

	private Sender sendLink;
	private RequestResponseLink requestResponseLink;
	private CompletableFuture<CoreMessageSender> linkFirstOpen; 
	private int linkCredit;
	private Exception lastKnownLinkError;
	private Instant lastKnownErrorReportedAt;
	private ScheduledFuture<?> sasTokenRenewTimerFuture;
	private CompletableFuture<Void> requestResponseLinkCreationFuture;
	private CompletableFuture<Void> sendLinkReopenFuture;

	public static CompletableFuture<CoreMessageSender> create(
			final MessagingFactory factory,
			final String sendLinkName,
			final String senderPath)
	{
	    TRACE_LOGGER.info("Creating core message sender to '{}'", senderPath);
		final CoreMessageSender msgSender = new CoreMessageSender(factory, sendLinkName, senderPath);
		TimeoutTracker openLinkTracker = TimeoutTracker.create(factory.getOperationTimeout());
		msgSender.initializeLinkOpen(openLinkTracker);
		
		msgSender.sendSASTokenAndSetRenewTimer(false).handleAsync((v, sasTokenEx) -> {
		    if(sasTokenEx != null)
		    {
		        Throwable cause = ExceptionUtil.extractAsyncCompletionCause(sasTokenEx);
		        TRACE_LOGGER.error("Sending SAS Token to '{}' failed.", msgSender.sendPath, cause);
		        msgSender.linkFirstOpen.completeExceptionally(cause);
		    }
		    else
		    {
		        try
	            {
	                msgSender.underlyingFactory.scheduleOnReactorThread(new DispatchHandler()
	                {
	                    @Override
	                    public void onEvent()
	                    {
	                        msgSender.createSendLink();
	                    }
	                });
	            }
	            catch (IOException ioException)
	            {
	                msgSender.cancelSASTokenRenewTimer();
	                msgSender.linkFirstOpen.completeExceptionally(new ServiceBusException(false, "Failed to create Sender, see cause for more details.", ioException));
	            }
		    }
		    
		    return null;
        });
		
		
		return msgSender.linkFirstOpen;		
	}
	
	private CompletableFuture<Void> createRequestResponseLink()
	{		
		synchronized (this.requestResonseLinkCreationLock) {
            if(this.requestResponseLinkCreationFuture == null)
            {
                this.requestResponseLinkCreationFuture = new CompletableFuture<Void>();                
                this.underlyingFactory.obtainRequestResponseLinkAsync(this.sendPath).handleAsync((rrlink, ex) ->
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

	private CoreMessageSender(final MessagingFactory factory, final String sendLinkName, final String senderPath)
	{
		super(sendLinkName, factory);

		this.sendPath = senderPath;
		this.sasTokenAudienceURI = String.format(ClientConstants.SAS_TOKEN_AUDIENCE_FORMAT, factory.getHostName(), senderPath);
		this.underlyingFactory = factory;
		this.operationTimeout = factory.getOperationTimeout();
		
		this.lastKnownLinkError = null;
		this.lastKnownErrorReportedAt = Instant.EPOCH;
		
		this.retryPolicy = factory.getRetryPolicy();

		this.pendingSendLock = new Object();
		this.pendingSendsData = new ConcurrentHashMap<String, SendWorkItem<Void>>();
		this.pendingSends = new PriorityQueue<WeightedDeliveryTag>(1000, new DeliveryTagComparator());
		this.linkCredit = 0;

		this.linkClose = new CompletableFuture<Void>();
		this.sendLinkReopenFuture = null;
		
		this.sendWork = new DispatchHandler()
		{ 
			@Override
			public void onEvent() 
			{
				CoreMessageSender.this.processSendWork();
			}
		};
	}

	public String getSendPath()
	{
		return this.sendPath;
	}

	private CompletableFuture<Void> sendAsync(byte[] bytes, int arrayOffset, int messageFormat)
	{
		return this.sendAsync(bytes, arrayOffset, messageFormat, null, null);
	}
	
	private CompletableFuture<Void> sendAsync(
			final byte[] bytes,
			final int arrayOffset,
			final int messageFormat,
			final CompletableFuture<Void> onSend,
			final TimeoutTracker tracker)
	{
		return this.sendCoreAsync(bytes, arrayOffset, messageFormat, onSend, tracker, null, null, null);
	}

	private CompletableFuture<Void> sendCoreAsync(
			final byte[] bytes,
			final int arrayOffset,
			final int messageFormat,
			final CompletableFuture<Void> onSend,
			final TimeoutTracker tracker,
			final String deliveryTag,
			final Exception lastKnownError,
			final ScheduledFuture<?> timeoutTask)
	{
		this.throwIfClosed(this.lastKnownLinkError);
		TRACE_LOGGER.debug("Sending message to '{}'", this.sendPath);
		if (tracker != null && onSend != null && (tracker.remaining().isNegative() || tracker.remaining().isZero()))
		{			
			TRACE_LOGGER.warn("path:{}, linkName:{}, deliveryTag:{} - timed out at sendCore", this.sendPath, this.sendLink.getName(), deliveryTag);

			if (timeoutTask != null)
			{
				timeoutTask.cancel(false);
			}
			
			this.throwSenderTimeout(onSend, null);
			return onSend;
		}

		final boolean isRetrySend = (onSend != null);
		final String tag = (deliveryTag == null) ? UUID.randomUUID().toString().replace("-", StringUtil.EMPTY) : deliveryTag;
		
		final CompletableFuture<Void> onSendFuture = (onSend == null) ? new CompletableFuture<Void>() : onSend;
		
		final SendWorkItem<Void> sendWaiterData = (tracker == null) ?
				new SendWorkItem<Void>(bytes, arrayOffset, messageFormat, onSendFuture, this.operationTimeout) : 
				new SendWorkItem<Void>(bytes, arrayOffset, messageFormat, onSendFuture, tracker);

		if (lastKnownError != null)
		{
			sendWaiterData.setLastKnownException(lastKnownError);
		}

		synchronized (this.pendingSendLock)
		{
			this.pendingSendsData.put(tag, sendWaiterData);
			this.pendingSends.offer(new WeightedDeliveryTag(tag, isRetrySend ? 1 : 0));
		}
		
		try
		{
			this.underlyingFactory.scheduleOnReactorThread(this.sendWork);
		}
		catch (IOException ioException)
		{			
			AsyncUtil.completeFutureExceptionally(onSendFuture, new ServiceBusException(false, "Send failed while dispatching to Reactor, see cause for more details.", ioException));
		}

		return onSendFuture;
	}	

	public CompletableFuture<Void> sendAsync(final Iterable<Message> messages)
	{
		if (messages == null || IteratorUtil.sizeEquals(messages, 0))
		{
			throw new IllegalArgumentException("Sending Empty batch of messages is not allowed.");
		}
		
		TRACE_LOGGER.debug("Sending a batch of messages to '{}'", this.sendPath);

		Message firstMessage = messages.iterator().next();			
		if (IteratorUtil.sizeEquals(messages, 1))
		{
			return this.sendAsync(firstMessage);
		}

		// proton-j doesn't support multiple dataSections to be part of AmqpMessage
		// here's the alternate approach provided by them: https://github.com/apache/qpid-proton/pull/54
		Message batchMessage = Proton.message();
		batchMessage.setMessageAnnotations(firstMessage.getMessageAnnotations());

		byte[] bytes = null;
		int byteArrayOffset = 0;
		try
		{
			Pair<byte[], Integer> encodedPair = Util.encodeMessageToMaxSizeArray(batchMessage);
			bytes = encodedPair.getFirstItem();
			byteArrayOffset = encodedPair.getSecondItem();
			
			for(Message amqpMessage: messages)
			{
				Message messageWrappedByData = Proton.message();	
				encodedPair = Util.encodeMessageToOptimalSizeArray(amqpMessage);
				messageWrappedByData.setBody(new Data(new Binary(encodedPair.getFirstItem(), 0, encodedPair.getSecondItem())));

				int encodedSize = Util.encodeMessageToCustomArray(messageWrappedByData, bytes, byteArrayOffset, ClientConstants.MAX_MESSAGE_LENGTH_BYTES - byteArrayOffset - 1);
				byteArrayOffset = byteArrayOffset + encodedSize;
			}
		}
		catch(PayloadSizeExceededException ex)
		{
		    TRACE_LOGGER.error("Payload size of batch of messages exceeded limit", ex);
			final CompletableFuture<Void> sendTask = new CompletableFuture<Void>();
			sendTask.completeExceptionally(ex);
			return sendTask;
		}

		return this.sendAsync(bytes, byteArrayOffset, AmqpConstants.AMQP_BATCH_MESSAGE_FORMAT);
	}

	public CompletableFuture<Void> sendAsync(Message msg)
	{
		try
		{
			Pair<byte[], Integer> encodedPair = Util.encodeMessageToOptimalSizeArray(msg);
			return this.sendAsync(encodedPair.getFirstItem(), encodedPair.getSecondItem(), DeliveryImpl.DEFAULT_MESSAGE_FORMAT);
		}
		catch(PayloadSizeExceededException exception)
		{
		    TRACE_LOGGER.error("Payload size of message exceeded limit", exception);
			final CompletableFuture<Void> sendTask = new CompletableFuture<Void>();
			sendTask.completeExceptionally(exception);
			return sendTask;
		}
	}

	@Override
	public void onOpenComplete(Exception completionException)
	{
		if (completionException == null)
		{
			this.lastKnownLinkError = null;
			this.retryPolicy.resetRetryCount(this.getClientId());

			if(this.sendLinkReopenFuture != null && !this.sendLinkReopenFuture.isDone())
            {
                AsyncUtil.completeFuture(this.sendLinkReopenFuture, null);
                this.sendLinkReopenFuture = null;
            }
			
			if (!this.linkFirstOpen.isDone())
			{
			    TRACE_LOGGER.info("Opened send link to '{}'", this.sendPath);
				AsyncUtil.completeFuture(this.linkFirstOpen, this);				
			}
			else
			{ 
				synchronized (this.pendingSendLock)
				{
					if (!this.pendingSendsData.isEmpty())
					{
						LinkedList<String> unacknowledgedSends = new LinkedList<String>();
						unacknowledgedSends.addAll(this.pendingSendsData.keySet());
		
						if (unacknowledgedSends.size() > 0)
						{
							Iterator<String> reverseReader = unacknowledgedSends.iterator();
							while (reverseReader.hasNext())
							{
								String unacknowledgedSend = reverseReader.next();
								if (this.pendingSendsData.get(unacknowledgedSend).isWaitingForAck())
								{
									this.pendingSends.offer(new WeightedDeliveryTag(unacknowledgedSend, 1));
								}
							}
						}
	
						unacknowledgedSends.clear();
					}
				}
			}
		}
		else
		{
		    TRACE_LOGGER.error("Opending send link to '{}' failed", this.sendPath, completionException);
		    this.cancelSASTokenRenewTimer();
			if (!this.linkFirstOpen.isDone())
			{			    
				this.setClosed();				
				ExceptionUtil.completeExceptionally(this.linkFirstOpen, completionException, this, true);
			}
			
			if(this.sendLinkReopenFuture != null && !this.sendLinkReopenFuture.isDone())
            {
                AsyncUtil.completeFutureExceptionally(this.sendLinkReopenFuture, completionException);
                this.sendLinkReopenFuture = null;
            }
		}
	}

	@Override
	public void onClose(ErrorCondition condition)
	{	    
		Exception completionException = condition != null ? ExceptionUtil.toException(condition) 
				: new ServiceBusException(ClientConstants.DEFAULT_IS_TRANSIENT,
						"The entity has been closed due to transient failures (underlying link closed), please retry the operation.");		
		this.onError(completionException);
	}

	@Override
	public void onError(Exception completionException)
	{
		this.linkCredit = 0;
		if (this.getIsClosingOrClosed())
		{
			synchronized (this.pendingSendLock)
			{
				for (Map.Entry<String, SendWorkItem<Void>> pendingSend: this.pendingSendsData.entrySet())
				{
					ExceptionUtil.completeExceptionally(pendingSend.getValue().getWork(),
							completionException == null
								? new OperationCancelledException("Send cancelled as the Sender instance is Closed before the sendOperation completed.")
								: completionException,
							this, true);					
				}
	
				this.pendingSendsData.clear();
				this.pendingSends.clear();
			}
			
			TRACE_LOGGER.info("Send link to '{}' closed", this.sendPath);
			AsyncUtil.completeFuture(this.linkClose, null);
			return;
		}
		else
		{
			this.lastKnownLinkError = completionException;
			this.lastKnownErrorReportedAt = Instant.now();

			this.onOpenComplete(completionException);

			if (completionException != null &&
					(!(completionException instanceof ServiceBusException) || !((ServiceBusException) completionException).getIsTransient()))
			{
			    TRACE_LOGGER.warn("Send link to '{}' closed. Failing all pending send requests.", this.sendPath);
				synchronized (this.pendingSendLock)
				{
					for (Map.Entry<String, SendWorkItem<Void>> pendingSend: this.pendingSendsData.entrySet())
					{
						this.cleanupFailedSend(pendingSend.getValue(), completionException);					
					}
		
					this.pendingSendsData.clear();
					this.pendingSends.clear();
				}
			}
			else
			{			    
				final Map.Entry<String, SendWorkItem<Void>> pendingSendEntry = IteratorUtil.getFirst(this.pendingSendsData.entrySet());
				if (pendingSendEntry != null && pendingSendEntry.getValue() != null)
				{
					final TimeoutTracker tracker = pendingSendEntry.getValue().getTimeoutTracker();
					if (tracker != null)
					{
						final Duration nextRetryInterval = this.retryPolicy.getNextRetryInterval(this.getClientId(), completionException, tracker.remaining());
						if (nextRetryInterval != null)
						{
						    TRACE_LOGGER.warn("Send link to '{}' closed. Will retry link creation after '{}'.", this.sendPath, nextRetryInterval);
						    Timer.schedule(() -> {CoreMessageSender.this.ensureLinkIsOpen();}, nextRetryInterval, TimerType.OneTimeRun);							
						}
					}
				}
			}
		}
	}

	@Override
	public void onSendComplete(final Delivery delivery)
	{
		final DeliveryState outcome = delivery.getRemoteState();
		final String deliveryTag = new String(delivery.getTag());
		
		TRACE_LOGGER.debug("Received ack for delivery. path:{}, linkName:{}, deliveryTag:{}, outcome:{}", CoreMessageSender.this.sendPath, this.sendLink.getName(), deliveryTag, outcome);
		final SendWorkItem<Void> pendingSendWorkItem = this.pendingSendsData.remove(deliveryTag);

		if (pendingSendWorkItem != null)
		{
			if (outcome instanceof Accepted)
			{
				this.lastKnownLinkError = null;
				this.retryPolicy.resetRetryCount(this.getClientId());

				pendingSendWorkItem.cancelTimeoutTask(false);				
				AsyncUtil.completeFuture(pendingSendWorkItem.getWork(), null);
			}
			else if (outcome instanceof Rejected)
			{
				Rejected rejected = (Rejected) outcome;
				ErrorCondition error = rejected.getError();
				Exception exception = ExceptionUtil.toException(error);

				if (ExceptionUtil.isGeneralError(error.getCondition()))
				{
					this.lastKnownLinkError = exception;
					this.lastKnownErrorReportedAt = Instant.now();
				}

				Duration retryInterval = this.retryPolicy.getNextRetryInterval(
						this.getClientId(), exception, pendingSendWorkItem.getTimeoutTracker().remaining());
				if (retryInterval == null)
				{
					this.cleanupFailedSend(pendingSendWorkItem, exception);
				}
				else
				{
				    TRACE_LOGGER.warn("Send failed for delivery '{}'. Will retry after '{}'", deliveryTag, retryInterval);
					pendingSendWorkItem.setLastKnownException(exception);
					try
					{
						this.underlyingFactory.scheduleOnReactorThread((int) retryInterval.toMillis(),
								new DispatchHandler()
								{
									@Override
									public void onEvent()
									{
										CoreMessageSender.this.reSendAsync(deliveryTag, pendingSendWorkItem, false);
									}
								});
					}
					catch (IOException ioException)
					{
						exception.initCause(ioException);
						this.cleanupFailedSend(
								pendingSendWorkItem,
								new ServiceBusException(false, "Send operation failed while scheduling a retry on Reactor, see cause for more details.", ioException));
					}
				}
			}
			else if (outcome instanceof Released)
			{
				this.cleanupFailedSend(pendingSendWorkItem, new OperationCancelledException(outcome.toString()));
			}
			else 
			{
				this.cleanupFailedSend(pendingSendWorkItem, new ServiceBusException(false, outcome.toString()));
			}
		}
		else
		{
			TRACE_LOGGER.warn("Delivery mismatch. path:{}, linkName:{}, delivery:{}", this.sendPath, this.sendLink.getName(), deliveryTag);
		}
	}

	private void reSendAsync(final String deliveryTag, final SendWorkItem<Void> pendingSend, boolean reuseDeliveryTag)
	{
		if (pendingSend != null)
		{
			this.sendCoreAsync(pendingSend.getMessage(), 
					pendingSend.getEncodedMessageSize(), 
					pendingSend.getMessageFormat(),
					pendingSend.getWork(),
					pendingSend.getTimeoutTracker(),
					reuseDeliveryTag ? deliveryTag : null,
					pendingSend.getLastKnownException(),
					pendingSend.getTimeoutTask());
		}
	}
	
	private void cleanupFailedSend(final SendWorkItem<Void> failedSend, final Exception exception)
	{
		failedSend.cancelTimeoutTask(false);		
		ExceptionUtil.completeExceptionally(failedSend.getWork(), exception, this, true);
	}

	private void createSendLink()
	{
	    TRACE_LOGGER.info("Creating send link to '{}'", this.sendPath);
		final Connection connection = this.underlyingFactory.getConnection();
		final Session session = connection.session();
		session.setOutgoingWindow(Integer.MAX_VALUE);
		session.open();
		BaseHandler.setHandler(session, new SessionHandler(sendPath));

		final String sendLinkNamePrefix = StringUtil.getShortRandomString();
		final String sendLinkName = !StringUtil.isNullOrEmpty(connection.getRemoteContainer()) ?
				sendLinkNamePrefix.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(connection.getRemoteContainer()) :
				sendLinkNamePrefix;
		
		final Sender sender = session.sender(sendLinkName);
		final Target target = new Target();
		target.setAddress(sendPath);
		sender.setTarget(target);

		final Source source = new Source();
		sender.setSource(source);

		SenderSettleMode settleMode = SenderSettleMode.UNSETTLED;
		TRACE_LOGGER.debug("Send link settle mode '{}'", settleMode);
		sender.setSenderSettleMode(settleMode);

		Map linkProperties = new HashMap();
		linkProperties.put(ClientConstants.LINK_TIMEOUT_PROPERTY, Util.adjustServerTimeout(this.underlyingFactory.getOperationTimeout()).toMillis());
		sender.setProperties(linkProperties);
		
		SendLinkHandler handler = new SendLinkHandler(CoreMessageSender.this);
		BaseHandler.setHandler(sender, handler);

		this.underlyingFactory.registerForConnectionError(sender);
		sender.open();
		
		if (this.sendLink != null)
		{
			final Sender oldSender = this.sendLink;
			this.underlyingFactory.deregisterForConnectionError(oldSender);
		}
		
		this.sendLink = sender;
	}
	
	CompletableFuture<Void> sendSASTokenAndSetRenewTimer(boolean retryOnFailure)
	{
	    if(this.getIsClosingOrClosed())
        {
            return CompletableFuture.completedFuture(null);
        }
        else
        {            
            CompletableFuture<ScheduledFuture<?>> sendTokenFuture = this.underlyingFactory.sendSASTokenAndSetRenewTimer(this.sasTokenAudienceURI, retryOnFailure, () -> this.sendSASTokenAndSetRenewTimer(true));
            return sendTokenFuture.thenAccept((f) -> {this.sasTokenRenewTimerFuture = f; TRACE_LOGGER.debug("Sent SAS Token and set renew timer");});
        }
	}
	
	private void cancelSASTokenRenewTimer()
    {
        if(this.sasTokenRenewTimerFuture != null && !this.sasTokenRenewTimerFuture.isDone())
        {            
            this.sasTokenRenewTimerFuture.cancel(true);
            TRACE_LOGGER.debug("Cancelled SAS Token renew timer");
        }
    }
	
	// TODO: consolidate common-code written for timeouts in Sender/Receiver
	private void initializeLinkOpen(TimeoutTracker timeout)
	{
		this.linkFirstOpen = new CompletableFuture<CoreMessageSender>();

		// timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
		Timer.schedule(
				new Runnable()
				{
					public void run()
					{
						if (!CoreMessageSender.this.linkFirstOpen.isDone())
						{
						    CoreMessageSender.this.cancelSASTokenRenewTimer();
							Exception operationTimedout = new TimeoutException(
									String.format(Locale.US, "Open operation on SendLink(%s) on Entity(%s) timed out at %s.",	CoreMessageSender.this.sendLink.getName(), CoreMessageSender.this.getSendPath(), ZonedDateTime.now().toString()),
									CoreMessageSender.this.lastKnownErrorReportedAt.isAfter(Instant.now().minusSeconds(ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS)) ? CoreMessageSender.this.lastKnownLinkError : null);
							
							TRACE_LOGGER.warn(operationTimedout.getMessage());

							ExceptionUtil.completeExceptionally(CoreMessageSender.this.linkFirstOpen, operationTimedout, CoreMessageSender.this, false);
						}
					}
				}
				, timeout.remaining()
				, TimerType.OneTimeRun);
	}

	@Override
	public ErrorContext getContext()
	{
		final boolean isLinkOpened = this.linkFirstOpen != null && this.linkFirstOpen.isDone();
		final String referenceId = this.sendLink != null && this.sendLink.getRemoteProperties() != null && this.sendLink.getRemoteProperties().containsKey(ClientConstants.TRACKING_ID_PROPERTY)
				? this.sendLink.getRemoteProperties().get(ClientConstants.TRACKING_ID_PROPERTY).toString()
						: ((this.sendLink != null) ? this.sendLink.getName() : null);

		SenderErrorContext errorContext = new SenderErrorContext(
				this.underlyingFactory!=null ? this.underlyingFactory.getHostName() : null,
						this.sendPath,
						referenceId,
						isLinkOpened && this.sendLink != null ? this.sendLink.getCredit() : null);
		return errorContext;
	}

	@Override
	public void onFlow(final int creditIssued)
	{
		this.lastKnownLinkError = null;

		if (creditIssued <= 0)
			return;	
		
		TRACE_LOGGER.debug("Received flow frame. path:{}, linkName:{}, remoteLinkCredit:{}, pendingSendsWaitingForCredit:{}, pendingSendsWaitingDelivery:{}",
                    this.sendPath, this.sendLink.getName(), creditIssued, this.pendingSends.size(), this.pendingSendsData.size() - this.pendingSends.size());

		this.linkCredit = this.linkCredit + creditIssued;
		this.sendWork.onEvent();
	}
	
	private synchronized CompletableFuture<Void> ensureLinkIsOpen()
    {
        // Send SAS token before opening a link as connection might have been closed and reopened
        if (this.sendLink.getLocalState() == EndpointState.CLOSED || this.sendLink.getRemoteState() == EndpointState.CLOSED)
        {
            if(this.sendLinkReopenFuture == null)
            {
                TRACE_LOGGER.info("Recreating send link to '{}'", this.sendPath);
                this.retryPolicy.incrementRetryCount(CoreMessageSender.this.getClientId());
                this.sendLinkReopenFuture = new CompletableFuture<Void>();
                // Variable just to closed over by the scheduled runnable. The runnable should cancel only the closed over future, not the parent's instance variable which can change
                final CompletableFuture<Void> linkReopenFutureThatCanBeCancelled = this.sendLinkReopenFuture;
                Timer.schedule(
                        () -> {
                            if (!linkReopenFutureThatCanBeCancelled.isDone())
                            {
                                CoreMessageSender.this.cancelSASTokenRenewTimer();
                                Exception operationTimedout = new TimeoutException(
                                        String.format(Locale.US, "%s operation on SendLink(%s) to path(%s) timed out at %s.", "Open", CoreMessageSender.this.sendLink.getName(), CoreMessageSender.this.sendPath, ZonedDateTime.now()));                           
                                
                                TRACE_LOGGER.warn(operationTimedout.getMessage());
                                linkReopenFutureThatCanBeCancelled.completeExceptionally(operationTimedout);
                            }
                        }                       
                        , CoreMessageSender.LINK_REOPEN_TIMEOUT
                        , TimerType.OneTimeRun);
                this.cancelSASTokenRenewTimer();
                this.sendSASTokenAndSetRenewTimer(false).handleAsync((v, sendTokenEx) -> {
                    if(sendTokenEx != null)
                    {
                        this.sendLinkReopenFuture.completeExceptionally(sendTokenEx);
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
                                    CoreMessageSender.this.createSendLink();
                                }
                            });
                        }
                        catch (IOException ioEx)
                        {
                            this.sendLinkReopenFuture.completeExceptionally(ioEx);
                        }
                    }
                    return null;
                });
            }
            
            return this.sendLinkReopenFuture;
        }
        else
        {
            return CompletableFuture.completedFuture(null);
        }
    }
	
	// actual send on the SenderLink should happen only in this method & should run on Reactor Thread
	private void processSendWork()
	{
	    TRACE_LOGGER.debug("Processing pending sends to '{}'. Available link credit '{}'", this.sendPath, this.linkCredit);
	    if(!this.ensureLinkIsOpen().isDone())
	    {
	        // Link recreation is pending
	        return;
	    }
	    
		final Sender sendLinkCurrent = this.sendLink;		
		while (sendLinkCurrent != null
				&& sendLinkCurrent.getLocalState() == EndpointState.ACTIVE && sendLinkCurrent.getRemoteState() == EndpointState.ACTIVE
				&& this.linkCredit > 0)
		{
			final WeightedDeliveryTag deliveryTag;
			final SendWorkItem<Void> sendData;
			synchronized (this.pendingSendLock)
			{
				deliveryTag = this.pendingSends.poll();
				sendData = deliveryTag != null 
						? this.pendingSendsData.get(deliveryTag.getDeliveryTag())
						: null;
			}
			
			if (sendData != null)
			{
				if (sendData.getWork() != null && sendData.getWork().isDone())
				{
					// CoreSend could enqueue Sends into PendingSends Queue and can fail the SendCompletableFuture
					// (when It fails to schedule the ProcessSendWork on reactor Thread)
					this.pendingSendsData.remove(sendData);
					continue;
				}
				
				Delivery delivery = null;
				boolean linkAdvance = false;
				int sentMsgSize = 0;
				Exception sendException = null;
				
				try
				{
					delivery = sendLinkCurrent.delivery(deliveryTag.getDeliveryTag().getBytes());
					delivery.setMessageFormat(sendData.getMessageFormat());
					TRACE_LOGGER.debug("Sending message delivery '{}' to '{}'", deliveryTag.getDeliveryTag(), this.sendPath);
					sentMsgSize = sendLinkCurrent.send(sendData.getMessage(), 0, sendData.getEncodedMessageSize());
					assert sentMsgSize == sendData.getEncodedMessageSize() : "Contract of the ProtonJ library for Sender.Send API changed";
	
					linkAdvance = sendLinkCurrent.advance();
				}
				catch(Exception exception)
				{
					sendException = exception;
				}
				
				if (linkAdvance)
				{
					this.linkCredit--;
					
					ScheduledFuture<?> timeoutTask = Timer.schedule(new Runnable()
					{
						@Override
						public void run()
						{
							if (!sendData.getWork().isDone())
							{
							    TRACE_LOGGER.error("Delivery '{}' to '{}' did not receive ack from service. Throwing timeout.", deliveryTag.getDeliveryTag(), CoreMessageSender.this.sendPath);
								CoreMessageSender.this.pendingSendsData.remove(deliveryTag);
								CoreMessageSender.this.throwSenderTimeout(sendData.getWork(), sendData.getLastKnownException());
							}
						}
					},
					this.operationTimeout, 
					TimerType.OneTimeRun);
					
					sendData.setTimeoutTask(timeoutTask);
					sendData.setWaitingForAck();
				}
				else
				{					
					TRACE_LOGGER.warn("Sendlink advance failed. path:{}, linkName:{}, deliveryTag:{}, sentMessageSize:{}, payloadActualSiz:{}",
					        this.sendPath, this.sendLink.getName(), deliveryTag, sentMsgSize, sendData.getEncodedMessageSize());

					if (delivery != null)
					{
						delivery.free();
					}
					
					Exception completionException = sendException != null ? new OperationCancelledException("Send operation failed. Please see cause for more details", sendException)
							: new OperationCancelledException(String.format(Locale.US, "Send operation failed while advancing delivery(tag: %s) on SendLink(path: %s).", this.sendPath, deliveryTag));
					AsyncUtil.completeFutureExceptionally(sendData.getWork(), completionException);					
				}
			}
			else
			{
				if (deliveryTag != null)
				{					
					TRACE_LOGGER.error("SendData not found for this delivery. path:{}, linkName:{}, deliveryTag:{}", this.sendPath, this.sendLink.getName(), deliveryTag);
				}
				else
				{
				    TRACE_LOGGER.debug("There are no pending sends to '{}'.", this.sendPath);
				}

				break;
			}
		}
	}

	private void throwSenderTimeout(CompletableFuture<Void> pendingSendWork, Exception lastKnownException)
	{
		Exception cause = lastKnownException;
		if (lastKnownException == null && this.lastKnownLinkError != null)
		{		  
			cause = this.lastKnownErrorReportedAt.isAfter(Instant.now().minusMillis(this.operationTimeout.toMillis())) ? this.lastKnownLinkError	: null;
		}

		boolean isClientSideTimeout = (cause == null || !(cause instanceof ServiceBusException));
		ServiceBusException exception = isClientSideTimeout
				? new TimeoutException(String.format(Locale.US, "%s %s %s.", CoreMessageSender.SEND_TIMED_OUT, " at ", ZonedDateTime.now(), cause)) 
						: (ServiceBusException) cause;

		TRACE_LOGGER.error("Send timed out", exception);
		ExceptionUtil.completeExceptionally(pendingSendWork, exception, this, true);
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
							Exception operationTimedout = new TimeoutException(String.format(Locale.US, "%s operation on Send Link(%s) timed out at %s", "Close", CoreMessageSender.this.sendLink.getName(), ZonedDateTime.now()));							
							TRACE_LOGGER.warn(operationTimedout.getMessage());

							ExceptionUtil.completeExceptionally(linkClose, operationTimedout, CoreMessageSender.this, false);
						}
					}
				}
				, timeout.remaining()
				, TimerType.OneTimeRun);
	}

	@Override
	protected CompletableFuture<Void> onClose()
	{
		if (!this.getIsClosed())
		{
			if (this.sendLink != null && this.sendLink.getLocalState() != EndpointState.CLOSED)
			{
				try {
					this.underlyingFactory.scheduleOnReactorThread(new DispatchHandler() {
						
						@Override
						public void onEvent() {
							if (CoreMessageSender.this.sendLink != null && CoreMessageSender.this.sendLink.getLocalState() != EndpointState.CLOSED)
							{
							    TRACE_LOGGER.info("Closing send link to '{}'", CoreMessageSender.this.sendPath);
								CoreMessageSender.this.underlyingFactory.deregisterForConnectionError(CoreMessageSender.this.sendLink);
								CoreMessageSender.this.sendLink.close();
								CoreMessageSender.this.scheduleLinkCloseTimeout(TimeoutTracker.create(CoreMessageSender.this.operationTimeout));
							}						
						}
					});
				} catch (IOException e) {
					AsyncUtil.completeFutureExceptionally(this.linkClose, e);
				}				
			}
			else if (this.sendLink == null || this.sendLink.getRemoteState() == EndpointState.CLOSED)
			{
				AsyncUtil.completeFuture(this.linkClose, null);
			}
		}
		
		this.cancelSASTokenRenewTimer();
		this.underlyingFactory.releaseRequestResponseLink(this.sendPath);
		return this.linkClose;
	}
	
	private static class WeightedDeliveryTag
	{
		private final String deliveryTag;
		private final int priority;
		
		WeightedDeliveryTag(final String deliveryTag, final int priority)
		{
			this.deliveryTag = deliveryTag;
			this.priority = priority;
		}
		
		public String getDeliveryTag()
		{
			return this.deliveryTag;
		}
		
		public int getPriority()
		{
			return this.priority;
		}
	}
	
	private static class DeliveryTagComparator implements Comparator<WeightedDeliveryTag>
	{
		@Override
		public int compare(WeightedDeliveryTag deliveryTag0, WeightedDeliveryTag deliveryTag1)
		{
			return deliveryTag1.getPriority() - deliveryTag0.getPriority();
		}	
	}
	
	public CompletableFuture<long[]> scheduleMessageAsync(Message[] messages, Duration timeout)
	{
	    TRACE_LOGGER.debug("Sending '{}' scheduled message(s) to '{}'", messages.length, this.sendPath);
		return this.createRequestResponseLink().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			Collection<HashMap> messageList = new LinkedList<HashMap>();
			for(Message message : messages)
			{
				HashMap messageEntry = new HashMap();
				
				Pair<byte[], Integer> encodedPair = null;
				try
				{
					encodedPair = Util.encodeMessageToOptimalSizeArray(message);
				}
				catch(PayloadSizeExceededException exception)
				{
					final CompletableFuture<long[]> scheduleMessagesTask = new CompletableFuture<long[]>();
					scheduleMessagesTask.completeExceptionally(exception);
					return scheduleMessagesTask;
				}
				
				messageEntry.put(ClientConstants.REQUEST_RESPONSE_MESSAGE, new Binary(encodedPair.getFirstItem(), 0, encodedPair.getSecondItem()));
				messageEntry.put(ClientConstants.REQUEST_RESPONSE_MESSAGE_ID, message.getMessageId());
				
				String sessionId = message.getGroupId();
				if(!StringUtil.isNullOrEmpty(sessionId))
				{
					messageEntry.put(ClientConstants.REQUEST_RESPONSE_SESSION_ID, sessionId);
				}
				
				Object partitionKey = message.getMessageAnnotations().getValue().get(Symbol.valueOf(ClientConstants.PARTITIONKEYNAME));
				if(partitionKey != null && !((String)partitionKey).isEmpty())
				{
					messageEntry.put(ClientConstants.REQUEST_RESPONSE_PARTITION_KEY, (String)partitionKey);
				}
				
				messageList.add(messageEntry);
			}
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_MESSAGES, messageList);
			Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_SCHEDULE_MESSAGE_OPERATION, requestBodyMap, Util.adjustServerTimeout(timeout));
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, timeout);
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<long[]> returningFuture = new CompletableFuture<long[]>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
					long[] sequenceNumbers = (long[])RequestResponseUtils.getResponseBody(responseMessage).get(ClientConstants.REQUEST_RESPONSE_SEQUENCE_NUMBERS);
					if(TRACE_LOGGER.isDebugEnabled())
					{
					    TRACE_LOGGER.debug("Scheduled messages sent. Received sequence numbers '{}'", Arrays.toString(sequenceNumbers));
					}
									    
					returningFuture.complete(sequenceNumbers);
				}
				else
				{
					// error response
				    Exception scheduleException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
				    TRACE_LOGGER.error("Sending scheduled messages to '{}' failed.", this.sendPath, scheduleException);				    
					returningFuture.completeExceptionally(scheduleException);
				}
				return returningFuture;
			});
		});		
	}
	
	public CompletableFuture<Void> cancelScheduledMessageAsync(Long[] sequenceNumbers, Duration timeout)
	{
	    if(TRACE_LOGGER.isDebugEnabled())
	    {
	        TRACE_LOGGER.debug("Cancelling scheduled message(s) '{}' to '{}'", Arrays.toString(sequenceNumbers), this.sendPath);
	    }
	    
		return this.createRequestResponseLink().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SEQUENCE_NUMBERS, sequenceNumbers);
			
			Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_CANCEL_CHEDULE_MESSAGE_OPERATION, requestBodyMap, Util.adjustServerTimeout(timeout));
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, timeout);
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<Void> returningFuture = new CompletableFuture<Void>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
				    TRACE_LOGGER.debug("Cancelled scheduled messages in '{}'", this.sendPath);
					returningFuture.complete(null);
				}
				else
				{
					// error response
				    Exception failureException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
				    TRACE_LOGGER.error("Cancelling scheduled messages in '{}' failed.", this.sendPath, failureException);
					returningFuture.completeExceptionally(failureException);
				}
				return returningFuture;
			});
		});
	}
	
	// In case we need to support peek on a topic
	public CompletableFuture<Collection<Message>> peekMessagesAsync(long fromSequenceNumber, int messageCount)
	{
	    TRACE_LOGGER.debug("Peeking '{}' messages in '{}' from sequence number '{}'", messageCount, this.sendPath, fromSequenceNumber);
		return this.createRequestResponseLink().thenComposeAsync((v) ->
		{
			return CommonRequestResponseOperations.peekMessagesAsync(this.requestResponseLink, this.operationTimeout, fromSequenceNumber, messageCount, null);
		});		
	}
}
