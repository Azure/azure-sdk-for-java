/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.ws.handler.MessageContext;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
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

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import com.microsoft.azure.servicebus.amqp.DispatchHandler;
import com.microsoft.azure.servicebus.amqp.IAmqpSender;
import com.microsoft.azure.servicebus.amqp.SendLinkHandler;
import com.microsoft.azure.servicebus.amqp.SessionHandler;

/**
 * Abstracts all amqp related details
 * translates event-driven reactor model into async send Api
 */
public class CoreMessageSender extends ClientEntity implements IAmqpSender, IErrorContextProvider
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	private static final String SEND_TIMED_OUT = "Send operation timed out";

	private final Object requestResonseLinkCreationLock = new Object();
	private final MessagingFactory underlyingFactory;
	private final String sendPath;
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
	private TimeoutTracker openLinkTracker;
	private Exception lastKnownLinkError;
	private Instant lastKnownErrorReportedAt;

	public static CompletableFuture<CoreMessageSender> create(
			final MessagingFactory factory,
			final String sendLinkName,
			final String senderPath)
	{
		final CoreMessageSender msgSender = new CoreMessageSender(factory, sendLinkName, senderPath);
		msgSender.openLinkTracker = TimeoutTracker.create(factory.getOperationTimeout());
		msgSender.initializeLinkOpen(msgSender.openLinkTracker);
		
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
			msgSender.linkFirstOpen.completeExceptionally(new ServiceBusException(false, "Failed to create Sender, see cause for more details.", ioException));
		}
		
		return msgSender.linkFirstOpen;		
	}
	
	private CompletableFuture<Void> createRequestResponseLink()
	{
		synchronized (this.requestResonseLinkCreationLock) {
			if(this.requestResponseLink == null)
			{
				String requestResponseLinkPath = RequestResponseLink.getRequestResponseLinkPath(this.sendPath);
				CompletableFuture<Void> crateAndAssignRequestResponseLink =
								RequestResponseLink.createAsync(this.underlyingFactory, this.getClientId() + "-RequestResponse", requestResponseLinkPath).thenAccept((rrlink) -> {this.requestResponseLink = rrlink;});
				return crateAndAssignRequestResponseLink;
			}
			else
			{
				return CompletableFuture.completedFuture(null);
			}
		}				
	}

	private CoreMessageSender(final MessagingFactory factory, final String sendLinkName, final String senderPath)
	{
		super(sendLinkName, factory);

		this.sendPath = senderPath;
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

		if (tracker != null && onSend != null && (tracker.remaining().isNegative() || tracker.remaining().isZero()))
		{
			if (TRACE_LOGGER.isLoggable(Level.FINE))
			{
				TRACE_LOGGER.log(Level.FINE,
						String.format(Locale.US, 
						"path[%s], linkName[%s], deliveryTag[%s] - timed out at sendCore", this.sendPath, this.sendLink.getName(), deliveryTag));
			}

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
			this.openLinkTracker = null;

			this.lastKnownLinkError = null;
			this.retryPolicy.resetRetryCount(this.getClientId());

			if (!this.linkFirstOpen.isDone())
			{
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
			if (!this.linkFirstOpen.isDone())
			{
				this.setClosed();
				ExceptionUtil.completeExceptionally(this.linkFirstOpen, completionException, this, true);
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
							try
							{
								this.underlyingFactory.scheduleOnReactorThread((int) nextRetryInterval.toMillis(), new DispatchHandler()
								{
									@Override
									public void onEvent()
									{
										if (sendLink.getLocalState() == EndpointState.CLOSED || sendLink.getRemoteState() == EndpointState.CLOSED)
										{
											createSendLink();
										}
									}
								});
							}
							catch (IOException ignore)
							{
							}
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

		if (TRACE_LOGGER.isLoggable(Level.FINEST))
			TRACE_LOGGER.log(Level.FINEST,
				String.format(Locale.US, "path[%s], linkName[%s], deliveryTag[%s]", CoreMessageSender.this.sendPath, this.sendLink.getName(), deliveryTag));

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
			if (TRACE_LOGGER.isLoggable(Level.WARNING))
				TRACE_LOGGER.log(Level.WARNING, 
						String.format(Locale.US, "path[%s], linkName[%s], delivery[%s] - mismatch", this.sendPath, this.sendLink.getName(), deliveryTag));
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

		sender.setSenderSettleMode(SenderSettleMode.UNSETTLED);

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
							Exception operationTimedout = new TimeoutException(
									String.format(Locale.US, "Open operation on SendLink(%s) on Entity(%s) timed out at %s.",	CoreMessageSender.this.sendLink.getName(), CoreMessageSender.this.getSendPath(), ZonedDateTime.now().toString()),
									CoreMessageSender.this.lastKnownErrorReportedAt.isAfter(Instant.now().minusSeconds(ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS)) ? CoreMessageSender.this.lastKnownLinkError : null);

							if (TRACE_LOGGER.isLoggable(Level.WARNING))
							{
								TRACE_LOGGER.log(Level.WARNING, 
										String.format(Locale.US, "path[%s], linkName[%s], open call timedout", CoreMessageSender.this.sendPath, CoreMessageSender.this.sendLink.getName()), 
										operationTimedout);
							}

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

		if (TRACE_LOGGER.isLoggable(Level.FINE))
		{
			int numberOfSendsWaitingforCredit = this.pendingSends.size();
			TRACE_LOGGER.log(Level.FINE, String.format(Locale.US, "path[%s], linkName[%s], remoteLinkCredit[%s], pendingSendsWaitingForCredit[%s], pendingSendsWaitingDelivery[%s]",
					this.sendPath, this.sendLink.getName(), creditIssued, numberOfSendsWaitingforCredit, this.pendingSendsData.size() - numberOfSendsWaitingforCredit));
		}

		this.linkCredit = this.linkCredit + creditIssued;
		this.sendWork.onEvent();
	}

	private void recreateSendLink()
	{
		this.createSendLink();
		this.retryPolicy.incrementRetryCount(CoreMessageSender.this.getClientId());
	}
	
	// actual send on the SenderLink should happen only in this method & should run on Reactor Thread
	private void processSendWork()
	{
		final Sender sendLinkCurrent = this.sendLink;
		
		if (sendLinkCurrent.getLocalState() == EndpointState.CLOSED || sendLinkCurrent.getRemoteState() == EndpointState.CLOSED)
		{
			this.recreateSendLink();
			return;
		}
		
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
					// CoreSend could enque Sends into PendingSends Queue and can fail the SendCompletableFuture
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
					if (TRACE_LOGGER.isLoggable(Level.FINE))
					{
						TRACE_LOGGER.log(Level.FINE,
								String.format(Locale.US, "path[%s], linkName[%s], deliveryTag[%s], sentMessageSize[%s], payloadActualSize[%s] - sendlink advance failed",
								this.sendPath, this.sendLink.getName(), deliveryTag, sentMsgSize, sendData.getEncodedMessageSize()));
					}

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
					if (TRACE_LOGGER.isLoggable(Level.SEVERE))
					{
						TRACE_LOGGER.log(Level.SEVERE,
								String.format(Locale.US, "path[%s], linkName[%s], deliveryTag[%s] - sendData not found for this delivery.",
								this.sendPath, this.sendLink.getName(), deliveryTag));
					}
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
							if (TRACE_LOGGER.isLoggable(Level.WARNING))
							{
								TRACE_LOGGER.log(Level.WARNING, 
										String.format(Locale.US, "message recever(linkName: %s, path: %s) %s call timedout", CoreMessageSender.this.sendLink.getName(), CoreMessageSender.this.sendPath, "Close"), 
										operationTimedout);
							}

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
				this.underlyingFactory.deregisterForConnectionError(sendLink);
				this.sendLink.close();
				this.scheduleLinkCloseTimeout(TimeoutTracker.create(this.operationTimeout));
			}
			else if (this.sendLink == null || this.sendLink.getRemoteState() == EndpointState.CLOSED)
			{
				AsyncUtil.completeFuture(this.linkClose, null);
			}
		}
		
		return this.linkClose.thenCompose((v) -> {
			return this.requestResponseLink == null ? CompletableFuture.completedFuture(null) : this.requestResponseLink.closeAsync();});
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
			Message requestMessage = RequestResponseUtils.createRequestMessage(ClientConstants.REQUEST_RESPONSE_SCHEDULE_MESSAGE_OPERATION, requestBodyMap, Util.adjustServerTimeout(timeout));
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, timeout);
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<long[]> returningFuture = new CompletableFuture<long[]>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
					long[] sequenceNumbers = (long[])RequestResponseUtils.getResponseBody(responseMessage).get(ClientConstants.REQUEST_RESPONSE_SEQUENCE_NUMBERS);
					returningFuture.complete(sequenceNumbers);
				}
				else
				{
					// error response
					returningFuture.completeExceptionally(RequestResponseUtils.genereateExceptionFromResponse(responseMessage));
				}
				return returningFuture;
			});
		});		
	}
	
	public CompletableFuture<Void> cancelScheduledMessageAsync(Long[] sequenceNumbers, Duration timeout)
	{
		return this.createRequestResponseLink().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SEQUENCE_NUMBERS, sequenceNumbers);
			
			Message requestMessage = RequestResponseUtils.createRequestMessage(ClientConstants.REQUEST_RESPONSE_CANCEL_CHEDULE_MESSAGE_OPERATION, requestBodyMap, Util.adjustServerTimeout(timeout));
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, timeout);
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<Void> returningFuture = new CompletableFuture<Void>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
					returningFuture.complete(null);
				}
				else
				{
					// error response
					returningFuture.completeExceptionally(RequestResponseUtils.genereateExceptionFromResponse(responseMessage));
				}
				return returningFuture;
			});
		});
	}
	
	// In case we need to support peek on a topic
	public CompletableFuture<Collection<Message>> peekMessagesAsync(long fromSequenceNumber, int messageCount)
	{
		return this.createRequestResponseLink().thenComposeAsync((v) ->
		{
			return MessageBrowserUtil.peekMessagesAsync(this.requestResponseLink, this.operationTimeout, fromSequenceNumber, messageCount, null);
		});		
	}
}
