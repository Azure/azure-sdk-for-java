/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.nio.BufferOverflowException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.StringType;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import com.microsoft.azure.servicebus.amqp.IAmqpSender;
import com.microsoft.azure.servicebus.amqp.SendLinkHandler;
import com.microsoft.azure.servicebus.amqp.SessionHandler;

/**
 * Abstracts all amqp related details
 * translates event-driven reactor model into async send Api
 */
public class MessageSender extends ClientEntity implements IAmqpSender, IErrorContextProvider
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	private static final String SEND_TIMED_OUT = "Send operation timed out";
	private static final int SEND_WORK_POLL_INTERVAL = 50;

	private final MessagingFactory underlyingFactory;
	private final String sendPath;
	private final Duration operationTimeout;
	private final RetryPolicy retryPolicy;
	private final CompletableFuture<Void> linkClose;
	private final Object pendingSendLock;
	private final ConcurrentHashMap<String, ReplayableWorkItem<Void>> pendingSendsData;
	private final PriorityQueue<WeightedDeliveryTag> pendingSends;
	private final BaseHandler sendPump;

	private Sender sendLink;
	private CompletableFuture<MessageSender> linkFirstOpen; 
	private AtomicInteger linkCredit;
	private TimeoutTracker openLinkTracker;
	private boolean linkCreateScheduled;
	private Object linkCreateLock;
	private Exception lastKnownLinkError;
	private Instant lastKnownErrorReportedAt;

	public static CompletableFuture<MessageSender> create(
			final MessagingFactory factory,
			final String sendLinkName,
			final String senderPath)
	{
		final MessageSender msgSender = new MessageSender(factory, sendLinkName, senderPath);
		msgSender.openLinkTracker = TimeoutTracker.create(factory.getOperationTimeout());
		msgSender.initializeLinkOpen(msgSender.openLinkTracker);
		msgSender.linkCreateScheduled = true;

		Timer.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				msgSender.sendLink = msgSender.createSendLink();
			}
		}, Duration.ofSeconds(0), TimerType.OneTimeRun);
		return msgSender.linkFirstOpen;
	}

	private MessageSender(final MessagingFactory factory, final String sendLinkName, final String senderPath)
	{
		super(sendLinkName, factory);

		this.sendPath = senderPath;
		this.underlyingFactory = factory;
		this.operationTimeout = factory.getOperationTimeout();
		
		this.lastKnownLinkError = null;
		this.lastKnownErrorReportedAt = Instant.EPOCH;
		
		this.retryPolicy = factory.getRetryPolicy();

		this.pendingSendLock = new Object();
		this.pendingSendsData = new ConcurrentHashMap<String, ReplayableWorkItem<Void>>();
		this.pendingSends = new PriorityQueue<WeightedDeliveryTag>(1000, new DeliveryTagComparator());
		this.linkCredit = new AtomicInteger(0);

		this.linkCreateLock = new Object();
		this.linkClose = new CompletableFuture<Void>();
		
		this.sendPump = new BaseHandler()
		{ 
			@Override
			public void onTimerTask(Event e) 
			{
				MessageSender.this.processSendWork();

				if (MessageSender.this.linkCredit.get() > 0 && !MessageSender.this.getIsClosingOrClosed())
				{
					MessageSender.this.underlyingFactory.scheduleOnReactorThread(SEND_WORK_POLL_INTERVAL, this);
				}
			}
		};
	}

	public String getSendPath()
	{
		return this.sendPath;
	}

	private CompletableFuture<Void> send(byte[] bytes, int arrayOffset, int messageFormat)
	{
		return this.send(bytes, arrayOffset, messageFormat, null, null);
	}

	private CompletableFuture<Void> sendCore(
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
		
		final ReplayableWorkItem<Void> sendWaiterData = (tracker == null) ?
				new ReplayableWorkItem<Void>(bytes, arrayOffset, messageFormat, onSendFuture, this.operationTimeout) : 
				new ReplayableWorkItem<Void>(bytes, arrayOffset, messageFormat, onSendFuture, tracker);

		if (lastKnownError != null)
		{
			sendWaiterData.setLastKnownException(lastKnownError);
		}

		synchronized (this.pendingSendLock)
		{
			this.pendingSendsData.put(tag, sendWaiterData);
			this.pendingSends.offer(new WeightedDeliveryTag(tag, isRetrySend ? 1 : 0));
		}
		
		return onSendFuture;
	}

	private CompletableFuture<Void> send(
			final byte[] bytes,
			final int arrayOffset,
			final int messageFormat,
			final CompletableFuture<Void> onSend,
			final TimeoutTracker tracker)
	{
		return this.sendCore(bytes, arrayOffset, messageFormat, onSend, tracker, null, null, null);
	}

	private int getPayloadSize(Message msg)
	{
		if (msg == null || msg.getBody() == null)
		{
			return 0;
		}

		Data payloadSection = (Data) msg.getBody();
		if (payloadSection == null)
		{
			return 0;
		}

		Binary payloadBytes = payloadSection.getValue();
		if (payloadBytes == null)
		{
			return 0;
		}

		return payloadBytes.getLength();
	}

	private int getDataSerializedSize(Message amqpMessage)
	{
		if (amqpMessage == null)
		{
			return 0;
		}

		int payloadSize = this.getPayloadSize(amqpMessage);

		// EventData - accepts only PartitionKey - which is a String & stuffed into MessageAnnotation
		MessageAnnotations messageAnnotations = amqpMessage.getMessageAnnotations();
		ApplicationProperties applicationProperties = amqpMessage.getApplicationProperties();
		
		int annotationsSize = 0;
		int applicationPropertiesSize = 0;

		if (messageAnnotations != null)
		{
			for(Symbol value: messageAnnotations.getValue().keySet())
			{
				annotationsSize += Util.sizeof(value);
			}
			
			for(Object value: messageAnnotations.getValue().values())
			{
				annotationsSize += Util.sizeof(value);
			}
		}
		
		if (applicationProperties != null)
		{
			for(Object value: applicationProperties.getValue().keySet())
			{
				applicationPropertiesSize += Util.sizeof(value);
			}
			
			for(Object value: applicationProperties.getValue().values())
			{
				applicationPropertiesSize += Util.sizeof(value);
			}
		}
		
		return annotationsSize + applicationPropertiesSize + payloadSize;
	}

	public CompletableFuture<Void> send(final Iterable<Message> messages)
	{
		if (messages == null || IteratorUtil.sizeEquals(messages, 0))
		{
			throw new IllegalArgumentException("Sending Empty batch of messages is not allowed.");
		}

		Message firstMessage = messages.iterator().next();			
		if (IteratorUtil.sizeEquals(messages, 1))
		{
			return this.send(firstMessage);
		}

		// proton-j doesn't support multiple dataSections to be part of AmqpMessage
		// here's the alternate approach provided by them: https://github.com/apache/qpid-proton/pull/54
		Message batchMessage = Proton.message();
		batchMessage.setMessageAnnotations(firstMessage.getMessageAnnotations());

		byte[] bytes = new byte[ClientConstants.MAX_MESSAGE_LENGTH_BYTES];
		int encodedSize = batchMessage.encode(bytes, 0, ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
		int byteArrayOffset = encodedSize;

		for(Message amqpMessage: messages)
		{
			Message messageWrappedByData = Proton.message();

			int payloadSize = this.getDataSerializedSize(amqpMessage);
			int allocationSize = Math.min(payloadSize + ClientConstants.MAX_EVENTHUB_AMQP_HEADER_SIZE_BYTES, ClientConstants.MAX_MESSAGE_LENGTH_BYTES);

			byte[] messageBytes = new byte[allocationSize];
			int messageSizeBytes = amqpMessage.encode(messageBytes, 0, allocationSize);
			messageWrappedByData.setBody(new Data(new Binary(messageBytes, 0, messageSizeBytes)));

			try
			{
				encodedSize = messageWrappedByData.encode(bytes, byteArrayOffset, ClientConstants.MAX_MESSAGE_LENGTH_BYTES - byteArrayOffset - 1);
			}
			catch(BufferOverflowException exception)
			{
				final CompletableFuture<Void> sendTask = new CompletableFuture<Void>();
				sendTask.completeExceptionally(new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s kb", ClientConstants.MAX_MESSAGE_LENGTH_BYTES / 1024), exception));
				return sendTask;
			}

			byteArrayOffset = byteArrayOffset + encodedSize;
		}

		return this.send(bytes, byteArrayOffset, AmqpConstants.AMQP_BATCH_MESSAGE_FORMAT);
	}

	public CompletableFuture<Void> send(Message msg)
	{
		int payloadSize = this.getDataSerializedSize(msg);
		int allocationSize = Math.min(payloadSize + ClientConstants.MAX_EVENTHUB_AMQP_HEADER_SIZE_BYTES, ClientConstants.MAX_MESSAGE_LENGTH_BYTES);

		byte[] bytes = new byte[allocationSize];
		int encodedSize = 0;
		try
		{
			encodedSize = msg.encode(bytes, 0, allocationSize);
		}
		catch(BufferOverflowException exception)
		{
			final CompletableFuture<Void> sendTask = new CompletableFuture<Void>();
			sendTask.completeExceptionally(new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s kb", ClientConstants.MAX_MESSAGE_LENGTH_BYTES / 1024), exception));
			return sendTask;
		}

		return this.send(bytes, encodedSize, DeliveryImpl.DEFAULT_MESSAGE_FORMAT);
	}

	@Override
	public void onOpenComplete(Exception completionException)
	{
		synchronized(this.linkCreateLock)
		{
			this.linkCreateScheduled = false;
		}

		if (completionException == null)
		{
			this.openLinkTracker = null;

			this.lastKnownLinkError = null;
			this.retryPolicy.resetRetryCount(this.getClientId());

			if (!this.linkFirstOpen.isDone())
			{
				this.linkFirstOpen.complete(this);
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
								if (!this.pendingSends.contains(unacknowledgedSend))
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
				ExceptionUtil.completeExceptionally(this.linkFirstOpen, completionException, this);
			}
		}
	}

	@Override
	public void onClose(ErrorCondition condition)
	{
		Exception completionException = condition != null ? ExceptionUtil.toException(condition) 
				: new ServiceBusException(ClientConstants.DEFAULT_IS_TRANSIENT,
						"The entity has been close due to transient failures (underlying link closed), please retry the operation.");
		this.onError(completionException);
	}

	@Override
	public void onError(Exception completionException)
	{
		if (this.getIsClosingOrClosed())
		{
			synchronized (this.pendingSendLock)
			{
				for (Map.Entry<String, ReplayableWorkItem<Void>> pendingSend: this.pendingSendsData.entrySet())
				{
					ExceptionUtil.completeExceptionally(pendingSend.getValue().getWork(),
							completionException == null
								? new OperationCancelledException("Send cancelled as the Sender instance is Closed before the sendOperation completed.")
								: completionException,
							this);					
				}
	
				this.pendingSendsData.clear();
				this.pendingSends.clear();
			}
			
			this.linkClose.complete(null);
			return;
		}
		else
		{
			this.lastKnownLinkError = completionException;
			this.lastKnownErrorReportedAt = Instant.now();

			if (this.sendLink.getLocalState() != EndpointState.CLOSED)
			{
				this.sendLink.close();
			}

			this.onOpenComplete(completionException);

			if (!this.getIsClosingOrClosed())
				this.scheduleRecreate(Duration.ofSeconds(0));
		}
	}

	private void scheduleRecreate(Duration runAfter)
	{
		synchronized(this.linkCreateLock)
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
							if (MessageSender.this.sendLink.getLocalState() != EndpointState.CLOSED)
							{
								return;
							}

							Sender oldSender = MessageSender.this.sendLink;
							Sender sender = MessageSender.this.createSendLink();

							if (sender != null)
							{
								MessageSender.this.underlyingFactory.deregisterForConnectionError(oldSender);
								MessageSender.this.sendLink = sender;
							}
							else
							{
								synchronized (MessageSender.this.linkCreateLock) 
								{
									MessageSender.this.linkCreateScheduled = false;
								}
							}

							MessageSender.this.retryPolicy.incrementRetryCount(MessageSender.this.getClientId());
						}
					},
					runAfter,
					TimerType.OneTimeRun);
		}
	}

	@Override
	public void onSendComplete(final Delivery delivery)
	{
		final DeliveryState outcome = delivery.getRemoteState();
		final String deliveryTag = new String(delivery.getTag());

		if (TRACE_LOGGER.isLoggable(Level.FINEST))
			TRACE_LOGGER.log(Level.FINEST, String.format(Locale.US, "path[%s], linkName[%s], deliveryTag[%s]", MessageSender.this.sendPath, this.sendLink.getName(), deliveryTag));

		final ReplayableWorkItem<Void> pendingSendWorkItem = this.pendingSendsData.remove(deliveryTag);

		if (pendingSendWorkItem != null)
		{
			final CompletableFuture<Void> pendingSend = pendingSendWorkItem.getWork();
			if (outcome instanceof Accepted)
			{
				this.lastKnownLinkError = null;
				this.retryPolicy.resetRetryCount(this.getClientId());

				pendingSendWorkItem.getTimeoutTask().cancel(false);
				pendingSend.complete(null);
			}
			else if (outcome instanceof Rejected)
			{
				Rejected rejected = (Rejected) outcome;
				ErrorCondition error = rejected.getError();
				Exception exception = ExceptionUtil.toException(error);

				if (ExceptionUtil.isGeneralSendError(error.getCondition()))
				{
					this.lastKnownLinkError = exception;
					this.lastKnownErrorReportedAt = Instant.now();
				}

				Duration retryInterval = this.retryPolicy.getNextRetryInterval(
						this.getClientId(), exception, pendingSendWorkItem.getTimeoutTracker().remaining());
				if (retryInterval == null)
				{
					pendingSendWorkItem.getTimeoutTask().cancel(false);
					ExceptionUtil.completeExceptionally(pendingSend, exception, this);
				}
				else
				{
					pendingSendWorkItem.setLastKnownException(exception);
					Timer.schedule(new Runnable()
					{
						@Override
						public void run()
						{
							MessageSender.this.reSend(deliveryTag, pendingSendWorkItem, false);
						}
					}, retryInterval, TimerType.OneTimeRun);
				}
			}
			else if (outcome instanceof Released)
			{
				pendingSendWorkItem.getTimeoutTask().cancel(false);
				ExceptionUtil.completeExceptionally(pendingSend, new OperationCancelledException(outcome.toString()), this);
			}
			else 
			{
				pendingSendWorkItem.getTimeoutTask().cancel(false);
				ExceptionUtil.completeExceptionally(pendingSend, new ServiceBusException(false, outcome.toString()), this);
			}
		}
		else
		{
			if (TRACE_LOGGER.isLoggable(Level.WARNING))
				TRACE_LOGGER.log(Level.WARNING, 
						String.format(Locale.US, "path[%s], linkName[%s], delivery[%s] - mismatch", this.sendPath, this.sendLink.getName(), deliveryTag));
		}
	}

	private void reSend(final String deliveryTag, final ReplayableWorkItem<Void> pendingSend, boolean reuseDeliveryTag)
	{
		if (pendingSend != null)
		{
			this.sendCore(pendingSend.getMessage(), 
					pendingSend.getEncodedMessageSize(), 
					pendingSend.getMessageFormat(),
					pendingSend.getWork(),
					pendingSend.getTimeoutTracker(),
					reuseDeliveryTag ? deliveryTag : null,
					pendingSend.getLastKnownException(),
					pendingSend.getTimeoutTask());
		}
	}

	private Sender createSendLink()
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
				this.onError((Exception) throwable);
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

		Session session = connection.session();
		session.setOutgoingWindow(Integer.MAX_VALUE);
		session.open();
		BaseHandler.setHandler(session, new SessionHandler(this.sendPath));

		String sendLinkName = StringUtil.getRandomString();
		sendLinkName = sendLinkName.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(connection.getRemoteContainer());
		Sender sender = session.sender(sendLinkName);

		Target target = new Target();
		target.setAddress(this.sendPath);
		sender.setTarget(target);

		Source source = new Source();
		sender.setSource(source);

		sender.setSenderSettleMode(SenderSettleMode.UNSETTLED);

		SendLinkHandler handler = new SendLinkHandler(this);
		BaseHandler.setHandler(sender, handler);

		this.underlyingFactory.registerForConnectionError(sender);

		sender.open();
		return sender;
	}

	// TODO: consolidate common-code written for timeouts in Sender/Receiver
	private void initializeLinkOpen(TimeoutTracker timeout)
	{
		this.linkFirstOpen = new CompletableFuture<MessageSender>();

		// timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
		Timer.schedule(
				new Runnable()
				{
					public void run()
					{
						if (!MessageSender.this.linkFirstOpen.isDone())
						{
							Exception operationTimedout = new TimeoutException(
									String.format(Locale.US, "Open operation on SendLink(%s) on Entity(%s) timed out at %s.",	MessageSender.this.sendLink.getName(), MessageSender.this.getSendPath(), ZonedDateTime.now().toString()),
									MessageSender.this.lastKnownErrorReportedAt.isAfter(Instant.now().minusSeconds(ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS)) ? MessageSender.this.lastKnownLinkError : null);

							if (TRACE_LOGGER.isLoggable(Level.WARNING))
							{
								TRACE_LOGGER.log(Level.WARNING, 
										String.format(Locale.US, "path[%s], linkName[%s], open call timedout", MessageSender.this.sendPath, MessageSender.this.sendLink.getName()), 
										operationTimedout);
							}

							ExceptionUtil.completeExceptionally(MessageSender.this.linkFirstOpen, operationTimedout, MessageSender.this);
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
						: ((this.sendLink != null) ? this.sendLink.getName(): null);

		SenderContext errorContext = new SenderContext(
				this.underlyingFactory!=null ? this.underlyingFactory.getHostName() : null,
						this.sendPath,
						referenceId,
						isLinkOpened ? this.sendLink.getCredit() : null);
		return errorContext;
	}

	@Override
	public void onFlow()
	{
		this.lastKnownLinkError = null;

		int updatedCredit = this.sendLink.getRemoteCredit();
		
		if (updatedCredit <= 0)
			return;

		if (TRACE_LOGGER.isLoggable(Level.FINE))
		{
			int numberOfSendsWaitingforCredit = this.pendingSends.size();
			TRACE_LOGGER.log(Level.FINE, String.format(Locale.US, "path[%s], linkName[%s], remoteLinkCredit[%s], pendingSendsWaitingForCredit[%s], pendingSendsWaitingDelivery[%s]",
					this.sendPath, this.sendLink.getName(), updatedCredit, numberOfSendsWaitingforCredit, this.pendingSendsData.size() - numberOfSendsWaitingforCredit));
		}
		
		if (this.linkCredit.get() <= 0)
		{
			this.underlyingFactory.scheduleOnReactorThread(0, this.sendPump);
		}

		this.linkCredit.addAndGet(updatedCredit);
	}
	
	// actual send on the SenderLink should happen only in this method
	private void processSendWork()
	{
		final Sender sendLinkCurrent = this.sendLink;
		
		while (sendLinkCurrent != null
				&& sendLinkCurrent.getLocalState() == EndpointState.ACTIVE && sendLinkCurrent.getRemoteState() == EndpointState.ACTIVE
				&& this.linkCredit.get() > 0)
		{
			final WeightedDeliveryTag deliveryTag;
			final ReplayableWorkItem<Void> sendData;
			synchronized (this.pendingSendLock)
			{
				deliveryTag = this.pendingSends.poll();
				sendData = deliveryTag != null 
						? this.pendingSendsData.get(deliveryTag.getDeliveryTag())
						: null;
			}
			
			if (sendData != null)
			{
				Delivery delivery = null;
				boolean linkAdvance = false;
				int sentMsgSize = 0;
				Exception sendException = null;
				
				try
				{
					delivery = this.sendLink.delivery(deliveryTag.getDeliveryTag().getBytes());
					delivery.setMessageFormat(sendData.getMessageFormat());
					
					sentMsgSize = this.sendLink.send(sendData.getMessage(), 0, sendData.getEncodedMessageSize());
					assert sentMsgSize == sendData.getEncodedMessageSize() : "Contract of the ProtonJ library for Sender.Send API changed";
	
					linkAdvance = this.sendLink.advance();
				}
				catch(Exception exception)
				{
					sendException = exception;
				}
				
				if (linkAdvance)
				{
					this.linkCredit.decrementAndGet();
					
					ScheduledFuture<?> timeoutTask = Timer.schedule(new Runnable()
					{
						@Override
						public void run()
						{
							if (!sendData.getWork().isDone())
							{
								MessageSender.this.throwSenderTimeout(sendData.getWork(), sendData.getLastKnownException());
								MessageSender.this.pendingSendsData.remove(deliveryTag);
							}
						}
					}, this.operationTimeout, TimerType.OneTimeRun);
					
					sendData.setTimeoutTask(timeoutTask);
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
					
					sendData.getWork().completeExceptionally(
						sendException != null
							? new OperationCancelledException("Send operation failed. Please see cause for more details", sendException)
							: new OperationCancelledException(
									String.format(Locale.US, "Send operation failed while advancing delivery(tag: %s) on SendLink(path: %s).", this.sendPath, deliveryTag)));
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
			boolean isServerBusy = ((this.lastKnownLinkError instanceof ServerBusyException) 
					&& (this.lastKnownErrorReportedAt.isAfter(Instant.now().minusSeconds(ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS))));  
			cause = isServerBusy || (this.lastKnownErrorReportedAt.isAfter(Instant.now().minusMillis(this.operationTimeout.toMillis()))) 
					? this.lastKnownLinkError 
							: null;
		}

		boolean isClientSideTimeout = (cause == null || !(cause instanceof ServiceBusException));
		ServiceBusException exception = isClientSideTimeout
				? new TimeoutException(String.format(Locale.US, "%s %s %s.", MessageSender.SEND_TIMED_OUT, " at ", ZonedDateTime.now(), cause)) 
						: (ServiceBusException) cause;

		ExceptionUtil.completeExceptionally(pendingSendWork, exception, this);
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
							Exception operationTimedout = new TimeoutException(String.format(Locale.US, "%s operation on Receive Link(%s) timed out at %s", "Close", MessageSender.this.sendLink.getName(), ZonedDateTime.now()));
							if (TRACE_LOGGER.isLoggable(Level.WARNING))
							{
								TRACE_LOGGER.log(Level.WARNING, 
										String.format(Locale.US, "message recever(linkName: %s, path: %s) %s call timedout", MessageSender.this.sendLink.getName(), MessageSender.this.sendPath, "Close"), 
										operationTimedout);
							}

							ExceptionUtil.completeExceptionally(linkClose, operationTimedout, MessageSender.this);
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
				this.sendLink.close();
				this.scheduleLinkCloseTimeout(TimeoutTracker.create(this.operationTimeout));
			}
			else if (this.sendLink == null || this.sendLink.getRemoteState() == EndpointState.CLOSED)
			{
				this.linkClose.complete(null);
			}
		}

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
}
